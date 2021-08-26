package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.sam.controllers.dao.Controller;

public interface CustomControllerRepository {

	public Optional<Controller> findByControllerName(String name, String collection, String companyId);

	public Page<Controller> findByControllerIdsAndCompanyId(List<ObjectId> controllerIds, Pageable pageable,
			String companyId, String collectionName);
}
