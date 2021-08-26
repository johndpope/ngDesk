package com.ngdesk.commons.managers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionManager {
	
	private Map<String, Object> sessionInfo;
	
	public SessionManager() {
		setSessionInfo( new HashMap<String, Object>());
	}

	public Map<String, Object> getSessionInfo() {
		return sessionInfo;
	}

	public void setSessionInfo(Map<String, Object> sessionInfo) {
		this.sessionInfo = sessionInfo;
	}
	
	
}
