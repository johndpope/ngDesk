package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.integration.zoom.dao.ZoomIntegrationData;

public interface CustomZoomRepository {

	public Optional<ZoomIntegrationData> findZoomDataByCompany(String companyId);
	
	public void removeZoomData(String accountId, String userId);
}
