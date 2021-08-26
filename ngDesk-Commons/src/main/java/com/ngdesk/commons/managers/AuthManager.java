package com.ngdesk.commons.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.ngdesk.commons.models.User;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthManager {

	@Autowired
	private AuthProxy authProxy;

	private User user;

	private void setUser(User newUser) {
		this.user = newUser;
	}

	public User getUserDetails() {
		return this.user;
	}

	public void loadUserDetails(String authToken) {
		this.user = authProxy.getUserDetails(authToken);
	}
	
	public void loadUserDetailsForInternalCalls(String userUuid, String companyId) {
		this.user = authProxy.getUserDetailsForInternalCalls(userUuid, companyId);
	}

}
