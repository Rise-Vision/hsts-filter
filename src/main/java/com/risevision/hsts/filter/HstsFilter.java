package com.risevision.hsts.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class HstsFilter implements Filter {

  static final String HTTPS_SCHEME = "https";
  static final String HSTS_HEADER = "Strict-Transport-Security";
  static final String HSTS_ONE_YEAR = "max-age=31536000";

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
