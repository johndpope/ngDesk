package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.company.dao.CustomLogin;

public interface CustomLoginRepository {
	
	public Optional<CustomLogin> findLoginTemplate(String collectionName);
	
	

}
