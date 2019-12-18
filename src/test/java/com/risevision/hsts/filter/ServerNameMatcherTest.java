package com.risevision.hsts.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ServerNameMatcherTest {

  @Test
  public void testMatchOnFixedServerName() {
    ServerNameMatcher matcher = ServerNameMatcher.create("rvauser.appspot.com");

    assertTrue(matcher.test("rvauser.appspot.com"));
  }

  @Test
  public void testNoMatchOnFixedServerName() {
    ServerNameMatcher matcher = ServerNameMatcher.create("rvauser.appspot.com");

    assertFalse(matcher.test("rvauser-appspot.com"));
    assertFalse(matcher.test("rvauser2.appspot.com"));
    assertFalse(matcher.test("www.appspot.com"));
  }

  @Test
  public void testDynamicMatch() {
    ServerNameMatcher matcher = ServerNameMatcher.create("*.risevision.com");

    assertTrue(matcher.test("apps.risevision.com"));
    assertTrue(matcher.test("rva.risevision.com"));
    assertTrue(matcher.test("apps-stage-7.risevision.com"));
    assertTrue(matcher.test("store-stage-0.risevision.com"));
  }

  @Test
  public void testDynamicNoMatch() {
    ServerNameMatcher matcher = ServerNameMatcher.create("*.risevision.com");

    assertFalse(matcher.test("rvarisevision.com"));
    assertFalse(matcher.test("apps#stage-7.risevision.com"));
  }

  @Test
  public void testDynamicRvaUser2Match() {
    ServerNameMatcher matcher = ServerNameMatcher.create("*rvauser2.appspot.com");

    assertTrue(matcher.test("rvauser2.appspot.com"));
    assertTrue(matcher.test("1-07-021.rvauser2.appspot.com"));
    assertTrue(matcher.test("in-app-test-dot-rvauser2.appspot.com"));
  }

}
