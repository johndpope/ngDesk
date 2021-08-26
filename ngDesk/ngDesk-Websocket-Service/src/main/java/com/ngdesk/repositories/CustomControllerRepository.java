package com.ngdesk.repositories;

public interface CustomControllerRepository {
	
	public void updateControllerLastSeen(String controllerId, String companyId, String collectionName);
	
	public void updateSubAppLastSeen(String controllerId, String applicationName, String companyId, String collectionName);
	
}
