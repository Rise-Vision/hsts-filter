package com.risevision.hsts.filter;

import static com.risevision.hsts.filter.Globals.HSTS_HEADER;
import static com.risevision.hsts.filter.Globals.HSTS_ONE_YEAR;
import static com.risevision.hsts.filter.Globals.ORIGIN_HEADER;
import static com.risevision.hsts.filter.Globals.REFERER_HEADER;
import static com.risevision.hsts.filter.Globals.SKIP_REFERRERS_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HstsFilterTest {

  private static final Predicate<ServerNameMatcher> MATCHES_APPS =
    matcher -> matcher.test("apps.risevision.com");
  private static final Predicate<ServerNameMatcher> MATCHES_APPS_STAGE =
    matcher -> matcher.test("apps-stage-7.risevision.com");
  private static final Predicate<ServerNameMatcher> MATCHES_RVA_USER =
    matcher -> matcher.test("rvauser.risevision.com");
  private static final Predicate<ServerNameMatcher> MATCHES_RVA_USER2 =
    matcher -> matcher.test("rvauser2.appspot.com");
  private static final Predicate<ServerNameMatcher> MATCHES_RVA_USER2_TEST =
    matcher -> matcher.test("1-07-021.rvauser2.appspot.com");
  private static final Predicate<ServerNameMatcher> MATCHES_RVA_USER2_STAGE =
    matcher -> matcher.test("in-app-test-dot-rvauser2.appspot.com");
  private static final Predicate<ServerNameMatcher> MATCHES_OTHER =
    matcher -> matcher.test("www.apache.org");

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain chain;
  @Mock private FilterConfig filterConfig;

  @Before public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void createsSimpleServerNameMatcher() {
    String param = "\n   apps.risevision.com\n    ";
    List<ServerNameMatcher> origins = HstsFilter.toServerNameMatcherList(param);

    assertEquals(1, origins.size());
    assertTrue(origins.stream().anyMatch(MATCHES_APPS));
    assertFalse(origins.stream().anyMatch(MATCHES_APPS_STAGE));
    assertFalse(origins.stream().anyMatch(MATCHES_RVA_USER));
    assertFalse(origins.stream().anyMatch(MATCHES_RVA_USER2_TEST));
    assertFalse(origins.stream().anyMatch(MATCHES_RVA_USER2_STAGE));
    assertFalse(origins.stream().anyMatch(MATCHES_RVA_USER2));
    assertFalse(origins.stream().anyMatch(MATCHES_OTHER));
  }

  @Test
  public void createsMultipleServerNameMatcher() {
    String param = "\n   *.risevision.com\n   *rvauser.appspot.com\n   *rvauser2.appspot.com\n    ";
    List<ServerNameMatcher> origins = HstsFilter.toServerNameMatcherList(param);

    assertEquals(3, origins.size());
    assertTrue(origins.stream().anyMatch(MATCHES_APPS));
    assertTrue(origins.stream().anyMatch(MATCHES_APPS_STAGE));
    assertTrue(origins.stream().anyMatch(MATCHES_RVA_USER));
    assertTrue(origins.stream().anyMatch(MATCHES_RVA_USER2_TEST));
    assertTrue(origins.stream().anyMatch(MATCHES_RVA_USER2_STAGE));
    assertTrue(origins.stream().anyMatch(MATCHES_RVA_USER2));
    assertFalse(origins.stream().anyMatch(MATCHES_OTHER));
  }

  @Test
  public void extractsHttpServerName() {
    String referrer = "http://storage.rvaserver2.appspot.com/servertask";
    String serverName = HstsFilter.extractServerNameFrom(referrer);

    assertEquals("storage.rvaserver2.appspot.com", serverName);
  }

  @Test
  public void extractsHttpsServerName() {
    String referrer = "https://storage.rvaserver2.appspot.com/servertask";
    String serverName = HstsFilter.extractServerNameFrom(referrer);

    assertEquals("storage.rvaserver2.appspot.com", serverName);
  }

  @Test
  public void extractsSimpleServerName() {
    String referrer = "http://rvaserver2.appspot.com";
    String serverName = HstsFilter.extractServerNameFrom(referrer);

    assertEquals("rvaserver2.appspot.com", serverName);
  }

  @Test
  public void extractsLocalHostServerName() {
    String referrer = "http://localhost/onboarding";
    String serverName = HstsFilter.extractServerNameFrom(referrer);

    assertEquals("localhost", serverName);
  }

  @Test
  public void extractsLocalHostServerNameWithPort() {
    String referrer = "http://localhost:8000/onboarding";
    String serverName = HstsFilter.extractServerNameFrom(referrer);

    assertEquals("localhost", serverName);
  }

  @Test
  public void extractsServerNameWithLongPath() {
    String referrer = "https://storage-dot-rvaserver2.appspot.com/_ah/api/storage/v0.02/filesDuplicate";
    String serverName = HstsFilter.extractServerNameFrom(referrer);

    assertEquals("storage-dot-rvaserver2.appspot.com", serverName);
  }

  @Test
  public void dontExtractServerNameFromInvalidUrls() {
    String referrer = "storage-dot-rvaserver2.appspot.com/_ah/api/storage/v0.02/filesDuplicate";
    String serverName = HstsFilter.extractServerNameFrom(referrer);

    assertNull(serverName);
  }

  @Test
  public void addsHstsHeaderToHttpsRequest()
  throws IOException, ServletException {
    given(request.getScheme()).willReturn("https");

    HstsFilter filter = new HstsFilter();
    filter.doFilter(request, response, chain);

    verify(response).addHeader(HSTS_HEADER, HSTS_ONE_YEAR);
    verify(chain).doFilter(request, response);
  }

  @Test
  public void doesntAddHstsHeaderToHttpRequest()
  throws IOException, ServletException {
    given(request.getScheme()).willReturn("http");

    HstsFilter filter = new HstsFilter();
    filter.doFilter(request, response, chain);

    verifyZeroInteractions(response);
    verify(chain).doFilter(request, response);
  }

  @Test
  public void doesntAddHstsHeaderToHttpsRequestIfReferrerIsSkipped()
  throws IOException, ServletException {
    String referrer = "http://apps.risevision.com";

    given(request.getScheme()).willReturn("https");
    given(request.getHeader(REFERER_HEADER)).willReturn(referrer);
    given(filterConfig.getInitParameter(SKIP_REFERRERS_PARAM)).willReturn("apps.risevision.com");

    HstsFilter filter = new HstsFilter();
    filter.init(filterConfig);
    filter.doFilter(request, response, chain);

    verifyZeroInteractions(response);
    verify(chain).doFilter(request, response);
  }

  @Test
  public void doesntAddHstsHeaderToHttpsRequestIfOriginIsSkipped()
  throws IOException, ServletException {
    String origin = "http://apps.risevision.com";

    given(request.getScheme()).willReturn("https");
    given(request.getHeader(ORIGIN_HEADER)).willReturn(origin);
    given(filterConfig.getInitParameter(SKIP_REFERRERS_PARAM)).willReturn("apps.risevision.com");

    HstsFilter filter = new HstsFilter();
    filter.init(filterConfig);
    filter.doFilter(request, response, chain);

    verifyZeroInteractions(response);
    verify(chain).doFilter(request, response);
  }

  @Test
  public void addsHstsHeaderToHttpsRequestIfReferrerIsNotSkipped()
  throws IOException, ServletException {
    String referrer = "http://apps.risevision.com";

    given(request.getScheme()).willReturn("https");
    given(request.getHeader(REFERER_HEADER)).willReturn(referrer);
    given(filterConfig.getInitParameter(SKIP_REFERRERS_PARAM)).willReturn("www.risevision.com");

    HstsFilter filter = new HstsFilter();
    filter.init(filterConfig);
    filter.doFilter(request, response, chain);

    verify(response).addHeader(HSTS_HEADER, HSTS_ONE_YEAR);
    verify(chain).doFilter(request, response);
  }

}
