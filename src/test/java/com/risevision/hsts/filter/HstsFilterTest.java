package com.risevision.hsts.filter;

import static com.risevision.hsts.filter.Globals.HSTS_HEADER;
import static com.risevision.hsts.filter.Globals.HSTS_ONE_YEAR;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HstsFilterTest {  
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain chain;

  @Before public void setUp() {
    MockitoAnnotations.initMocks(this);
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

}
