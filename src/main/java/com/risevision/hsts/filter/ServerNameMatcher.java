package com.risevision.hsts.filter;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ServerNameMatcher implements Predicate<String> {

  private static final String LITERAL_DOT = "[.]";
  private static final Pattern DOT_REGEX = Pattern.compile(LITERAL_DOT);

  private static String escapeDots(String text) {
    return DOT_REGEX.matcher(text).replaceAll(LITERAL_DOT);
  }

  public static ServerNameMatcher create(String pattern) {
    String regex;

    if(pattern.startsWith("*"))
      regex = "[\\w.-]*" + escapeDots(pattern.substring(1));
    else
      regex = escapeDots(pattern);

    return new ServerNameMatcher(regex);
  }

  private final Pattern pattern;

  public ServerNameMatcher(String patternString) {
    pattern = Pattern.compile(patternString);
  }

  @Override
  public boolean test(String origin) {
    return pattern.matcher(origin).matches();
  }

}
