package com.risevision.hsts.filter;

import static com.risevision.hsts.filter.Globals.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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

  @Override
  public void init(FilterConfig config) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
  throws ServletException, IOException {
    String scheme = request.getScheme();

    if( HTTPS_SCHEME.equals(scheme) ) {
      HttpServletResponse httpResponse = ( HttpServletResponse )response;

      httpResponse.addHeader( HSTS_HEADER, HSTS_ONE_YEAR );
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}

}
