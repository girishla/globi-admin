package com.globi.security.user;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.globi.security.auth.AuthenticationResponse;
 
@Controller
public class CurrentUserController {
	
	  @Value("${security.token.header}")
	  private String tokenHeader;
 
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    public AuthenticationResponse currentUserName(HttpServletRequest request,Authentication authentication) {
    	
    	String token = request.getHeader(this.tokenHeader);
        UserDetails usrDetails= (UserDetails) authentication.getPrincipal();
        
        return new AuthenticationResponse(token,(GlobiUser) usrDetails);
        
    }
}