package com.ngdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.sam.controllers.dao.Log;

public interface CustomLogsRepository {
	
	public Page<Log> findAllApplicationLogs(String controllerId, String applicationName, Pageable pageable, String collectionName);
}
