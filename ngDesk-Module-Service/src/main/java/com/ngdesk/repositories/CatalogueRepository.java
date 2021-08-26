package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.module.catalogue.dao.Catalogue;

@Repository
public interface CatalogueRepository extends CustomCatalogueRepository,CustomNgdeskRepository<Catalogue, String>{


}
