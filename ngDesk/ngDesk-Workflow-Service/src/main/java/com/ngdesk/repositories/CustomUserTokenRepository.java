package com.ngdesk.repositories;

import com.ngdesk.workflow.notify.dao.UserToken;

public interface CustomUserTokenRepository {
	
	public UserToken findByUserUuid(String Useruuid,String collectionName);

}
