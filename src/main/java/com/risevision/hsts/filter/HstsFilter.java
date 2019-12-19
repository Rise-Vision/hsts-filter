package com.risevision.hsts.filter;

import static com.risevision.hsts.filter.Globals.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HstsFilter implements Filter {

  private static final Pattern SEPARATOR = Pattern.compile("[\\n\\s]+");

  private List<ServerNameMatcher> skipReferrers = null;

  static List<ServerNameMatcher> toServerNameMatcherList(String text) {
    return SEPARATOR
      .splitAsStream(text)
      .filter(pattern -> pattern.length() > 0)
      .map(ServerNameMatcher::create)
      .collect(Collectors.toList());
  }

  static String extractServerNameFrom(String referrer) {
    try {
      URL url = new URL(referrer);
 
      return url.getHost();
    } catch (MalformedURLException e) {
      return null; // invalid URL provided, ignore error
    }
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    String skipReferrersString = config.getInitParameter(SKIP_REFERRERS_PARAM);

    if(skipReferrersString != null)
      skipReferrers = toServerNameMatcherList(skipReferrersString.trim());
  }

  private boolean shouldSkipReferrers(ServletRequest request) {
    if(skipReferrers == null)
      return false;

    HttpServletRequest httpRequest = (HttpServletRequest)request;
 
    String referrer = httpRequest.getHeader(REFERER_HEADER);
    if(referrer == null)
      referrer = httpRequest.getHeader(ORIGIN_HEADER);

    if(referrer == null)
      return false;

    String serverName = extractServerNameFrom(referrer);

    return skipReferrers.stream().anyMatch(matcher -> matcher.test(serverName));
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
  throws ServletException, IOException {
    String scheme = request.getScheme();

    if( HTTPS_SCHEME.equals(scheme) && !shouldSkipReferrers(request) ) {
      HttpServletResponse httpResponse = ( HttpServletResponse )response;

      httpResponse.addHeader( HSTS_HEADER, HSTS_ONE_YEAR );
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}

}
