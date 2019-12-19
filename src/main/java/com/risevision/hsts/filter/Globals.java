package com.risevision.hsts.filter;

public interface Globals
{

  String HTTPS_SCHEME = "https";
  String HSTS_HEADER = "Strict-Transport-Security";
  String HSTS_ONE_YEAR = "max-age=31536000";
  String ORIGIN_HEADER = "origin";
  String REFERER_HEADER = "referer";

  String SKIP_REFERRERS_PARAM = "skip-referrers";

}
