package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.escalation.notify.UserToken;

public interface CustomUserTokenRepository {
	
	public Optional<UserToken> getUserTokenByUserUUID(String userUuid, String companyId);
}
