package com.globi.security;

import com.globi.security.auth.AuthenticationRequest;

public class SecurityTestApiConfig {

  public static final String HOSTNAME = "localhost";
  public static final String SERVER_CONTEXT = "";
  public static final Integer PORT = 9704;

  public static final AuthenticationRequest USER_AUTHENTICATION_REQUEST = new AuthenticationRequest("username", "password");
  public static final AuthenticationRequest ADMIN_AUTHENTICATION_REQUEST = new AuthenticationRequest("admin", "admin");
  public static final AuthenticationRequest EXPIRED_AUTHENTICATION_REQUEST = new AuthenticationRequest("expired", "expired");
  public static final AuthenticationRequest INVALID_AUTHENTICATION_REQUEST = new AuthenticationRequest("user", "abc123");

  public static String getAbsolutePath(String relativePath) {
    return String.format("http://%s:%d/%s/%s", HOSTNAME, PORT, SERVER_CONTEXT, relativePath);
  }

}
