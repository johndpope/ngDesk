package com.ngdesk.repositories.catalogue;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.catalogue.dao.Catalogue;

public interface CustomCatalogueRepository {

	Optional<Catalogue> findCatalogueById(String companyId, String catalogueId, String collectionName);
	
	Optional<Catalogue> findCatalogueByIdAndTeams(String companyId, String catalogueId, String collectionName, List<String> teamIds);

	List<Catalogue> findAllCatalogues(String companyId, Pageable pageable, String collectionName);
	
	List<Catalogue> findAllCataloguesWithTeams(String companyId, Pageable pageable, String collectionName, List<String>  teamIds);

	Optional<Long> catalogueCountWithTeams(String companyId, List<String> teamIds,String collectionName);
}
