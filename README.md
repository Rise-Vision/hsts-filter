# HSTS Filter [![Circle CI](https://circleci.com/gh/Rise-Vision/hsts-filter.svg?style=svg)](https://circleci.com/gh/Rise-Vision/hsts-filter)

## Introduction

The HSTS Filter is a servlet filter used to add the [Strict-Transport-Security header](https://tools.ietf.org/html/draft-ietf-websec-strict-transport-sec-14#section-6.1) to HTTPS requests.

## Built With
- Java (1.8)
- Maven
- [Wagon-Git](https://github.com/synergian/wagon-git)
- [Mockito](https://github.com/mockito/mockito)

## Development

### Local Development Environment Setup and Installation

* Maven 3 is required.

* Local build / test
``` bash
mvn clean install
mvn verify
```

### Dependencies
* Junit for testing
* Mockito for mocking and testing
* Wagon-Git for releasing the artifacts to [Rise Vision Maven Repository](https://github.com/Rise-Vision/mvn-repo)

### Usage
* Add CORS filter as dependency to your project

```xml

<!-- Inside pom.xml of your project -->

<repositories>
  <repository>
    <id>mvn-repo-releases</id>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <name>Maven Repository - Releases</name>
    <url>https://raw.github.com/Rise-Vision/mvn-repo/releases</url>
  </repository>
  <repository>
    <id>mvn-repo-snapshots</id>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
    <name>Maven Repository - Snapshots</name>
    <url>https://raw.github.com/Rise-Vision/mvn-repo/snapshots</url>
  </repository>
</repositories>

<!-- ... -->

<!-- In the <dependencies> section of your project's pom.xml -->
<dependency>
  <!-- From our private repo -->
  <groupId>com.risevision.hsts</groupId>
  <artifactId>hsts-filter</artifactId>
  <version>1.1.0</version>
</dependency>

<!-- ... -->
<!-- In your $HOME/.m2/settings.xml -->
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
  http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository/>
  <interactiveMode/>
  <usePluginRegistry/>
  <offline/>
  <pluginGroups/>
  <servers>
    <server>
      <id>mvn-repo-releases</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Authorization</name>
            <value>token {github auth token}</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>

    <server>
      <id>mvn-repo-snapshots</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Authorization</name>
            <value>token {github auth token}</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
  </servers>
  <mirrors/>
  <proxies/>
  <profiles/>
  <activeProfiles/>
</settings>

```

* You may need to update the dependency list in your project
```
mvn clean test -U
```

* Add the filter to your WEB-INF/web.xml file, and map the URLs that should point to it:

```xml
  <filter>
    <filter-name>HstsFilter</filter-name>
    <filter-class>com.risevision.hsts.filter.HstsFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>HstsFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```

In this basic configuration, the filter adds the HSTS header to any HTTPS 
request, and never adds headers to HTTP requests.

If there are referrers for which the HSTS header doesn't have to be added, it
can be specified using the *skip-referrers* init param as follows:

```xml
  <filter>
    <filter-name>HstsFilter</filter-name>
    <filter-class>com.risevision.hsts.filter.HstsFilter</filter-class>
  </filter>
    <init-param>
      <param-name>skip-referrers</param-name>
      <param-value>
        rva.risevision.com
        rva-test.risevision.com
        *rvaserver2.risevision.com
        *rvauser.appspot.com
        *rvauser2.appspot.com
      </param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>HstsFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```

URLs such as 'rva.risevision.com' are exact HTTP or HTTPS matches;
while URLs that start with '*' such as '*rvaserver2.appspot.com' can match any HTTP 
or HTTPS requests that end with rvaserver2.appspot.com ( rvaserver2.appspot.com,
storage-dot-rvaserver2.appspot.com, storage.rvaserver2.appspot.com, etc. ).
