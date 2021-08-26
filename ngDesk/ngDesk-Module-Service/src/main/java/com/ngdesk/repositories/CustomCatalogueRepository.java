package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.module.catalogue.dao.Catalogue;
import com.ngdesk.module.dao.Module;


public interface CustomCatalogueRepository {

	public Optional<Catalogue> findCatalogueByName(String name, String companyId, String collectionName);

	public Optional<Catalogue> findOtherCatalogueWithDuplicateName(String name, String catalogueId, String companyId,
			String collectionName);

	public Optional<Catalogue> findCatalogueByIdAndCompanyId(String id, String companyId, String collectionName);

	public void deleteById(String id, String companyId, String string);

	Optional<Module> findByModuleId(String moduleId, String collectionName);

}
