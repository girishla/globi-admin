package com.globi.security;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.globi.AbstractWebIntegrationTest;
import com.globi.security.auth.AuthenticationRequest;
import com.globi.security.auth.AuthenticationResponse;
import com.globi.security.user.UserRepository;

public class AuthenticationControllerTest extends AbstractWebIntegrationTest {

	private static boolean setUpIsDone = false;

	private RestTemplate client;
	private AuthenticationRequest authenticationRequest;
	private String authenticationToken;

	@Value("${security.route.authentication}")
	private String authenticationRoute;

	@Value("${security.route.authentication.refresh}")
	private String refreshRoute;

	@Autowired
	private TokenUtils tokenUtils;

	@Autowired
	private UserRepository userRepo;

	@Before
	public void setUp() throws Exception {
		client = new RestTemplate();

		if (setUpIsDone) {
			return;
		}

		userRepo.save(UserObjectMother.getNormalUserFor("username", "password"));
		userRepo.save(UserObjectMother.getAdminUserFor("admin", "admin"));
		userRepo.save(UserObjectMother.getExpiredUserFor("expired", "expired"));

		setUpIsDone = true;

	}

	@After
	public void tearDown() throws Exception {
		client = null;
	}

	@Test
	public void requestingAuthenticationWithNoCredentialsReturnsBadRequest() throws Exception {

		// this.initializeStateForMakingValidAuthenticationRequest();

		mvc.perform(post("/auth")//
				.content("")//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isBadRequest());//

	}

	@Test
	public void requestingAuthenticationWithInvalidCredentialsReturnsUnauthorized() throws Exception {

		mvc.perform(post("/auth")//
				.content(asJsonString(SecurityTestApiConfig.INVALID_AUTHENTICATION_REQUEST))
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isUnauthorized());//

	}

	@Test
	public void requestingProtectedWithValidCredentialsReturnsExpected() throws Exception {

		mvc.perform(post("/auth")//
				.content(asJsonString(SecurityTestApiConfig.USER_AUTHENTICATION_REQUEST))
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isOk())//
				.andExpect(jsonPath("$.token", userNameFromToken(is("username"))));

	}

	@Test
	public void requestingAuthenticationRefreshWithNoAuthorizationTokenReturnsUnauthorized() throws Exception {

		mvc.perform(get("/auth/refresh")//
				.contentType(MediaType.APPLICATION_JSON_VALUE)//
				.accept(MediaType.APPLICATION_JSON_VALUE))//
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(status().isUnauthorized());

	}

	/*
	 * @Test public void
	 * requestingAuthenticationRefreshWithUnauthorizedCredentialsReturnsBadRequest
	 * () throws Exception {
	 * this.initializeStateForMakingInvalidAuthenticationRefreshRequest();
	 * 
	 * try { client.exchange(
	 * TestApiConfig.getAbsolutePath(String.format("%s/%s", authenticationRoute,
	 * refreshRoute)), HttpMethod.GET,
	 * buildAuthenticationRefreshRequestEntity(), Void.class );
	 * fail("Should have returned an HTTP 400: Bad Request status code"); }
	 * catch (HttpClientErrorException e) { assertThat(e.getStatusCode(),
	 * is(HttpStatus.BAD_REQUEST)); } catch (Exception e) {
	 * fail("Should have returned an HTTP 400: Bad Request status code"); } }
	 */

	@Test
	@Ignore
	public void requestingAuthenticationRefreshTokenWithTokenCreatedBeforeLastPasswordResetReturnsBadRequest()
			throws Exception {
		this.initializeStateForMakingExpiredAuthenticationRefreshRequest();

		try {
			client.exchange(
					SecurityTestApiConfig.getAbsolutePath(String.format("%s/%s", authenticationRoute, refreshRoute)),
					HttpMethod.GET, buildAuthenticationRefreshRequestEntity(), Void.class);
			fail("Should have returned an HTTP 400: Bad Request status code");
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		} catch (Exception e) {
			fail("Should have returned an HTTP 400: Bad Request status code");
		}
	}

	private FeatureMatcher<String, String> userNameFromToken(Matcher<String> matcher) {
		return new FeatureMatcher<String, String>(matcher, "User Name Parsed from Token", "userNameFromToken") {
			@Override
			protected String featureValueOf(String actual) {
				return tokenUtils.getUsernameFromToken(actual);
			}
		};
	}

	private void initializeStateForMakingValidAuthenticationRequest() {
		authenticationRequest = SecurityTestApiConfig.USER_AUTHENTICATION_REQUEST;
	}

	private void initializeStateForMakingInvalidAuthenticationRequest() {
		authenticationRequest = SecurityTestApiConfig.INVALID_AUTHENTICATION_REQUEST;
	}

	private void initializeStateForMakingValidAuthenticationRefreshRequest() {
		authenticationRequest = SecurityTestApiConfig.USER_AUTHENTICATION_REQUEST;

		ResponseEntity<AuthenticationResponse> authenticationResponse = client.postForEntity(
				SecurityTestApiConfig.getAbsolutePath(authenticationRoute), authenticationRequest,
				AuthenticationResponse.class);

		authenticationToken = authenticationResponse.getBody().getToken();
	}

	private void initializeStateForMakingInvalidAuthenticationRefreshRequest() {
		authenticationRequest = SecurityTestApiConfig.INVALID_AUTHENTICATION_REQUEST;

		ResponseEntity<AuthenticationResponse> authenticationResponse = client.postForEntity(
				SecurityTestApiConfig.getAbsolutePath(authenticationRoute), authenticationRequest,
				AuthenticationResponse.class);

		authenticationToken = authenticationResponse.getBody().getToken();
	}

	private void initializeStateForMakingExpiredAuthenticationRefreshRequest() {
		authenticationRequest = SecurityTestApiConfig.EXPIRED_AUTHENTICATION_REQUEST;

		ResponseEntity<AuthenticationResponse> authenticationResponse = client.postForEntity(
				SecurityTestApiConfig.getAbsolutePath(authenticationRoute), authenticationRequest,
				AuthenticationResponse.class);

		authenticationToken = authenticationResponse.getBody().getToken();
	}

	private HttpEntity<Object> buildAuthenticationRequestEntity() {
		return SecurityRequestEntityBuilder.buildRequestEntityWithoutAuthenticationToken(authenticationRequest);
	}

	private HttpEntity<Object> buildAuthenticationRequestEntityWithoutCredentials() {
		return SecurityRequestEntityBuilder.buildRequestEntityWithoutBodyOrAuthenticationToken();
	}

	private HttpEntity<Object> buildAuthenticationRefreshRequestEntity() {
		return SecurityRequestEntityBuilder.buildRequestEntityWithoutBody(authenticationToken);
	}

	private HttpEntity<Object> buildAuthenticationRefreshRequestEntityWithoutAuthorizationToken() {
		return SecurityRequestEntityBuilder.buildRequestEntityWithoutBodyOrAuthenticationToken();
	}

}
