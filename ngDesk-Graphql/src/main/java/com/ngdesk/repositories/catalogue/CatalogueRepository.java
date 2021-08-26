package com.ngdesk.repositories.catalogue;

import com.ngdesk.graphql.catalogue.dao.Catalogue;

import com.ngdesk.repositories.CustomNgdeskRepository;

public interface CatalogueRepository extends CustomCatalogueRepository, CustomNgdeskRepository<Catalogue, String> {

}
