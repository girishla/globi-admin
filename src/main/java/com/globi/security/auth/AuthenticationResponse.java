package com.globi.security.auth;

import com.globi.security.user.GlobiUser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {

	private String token;
	private GlobiUser user;


}
