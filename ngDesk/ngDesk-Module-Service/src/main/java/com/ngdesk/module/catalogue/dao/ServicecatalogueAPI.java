package com.ngdesk.module.catalogue.dao;

import java.util.Date;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.CatalogueRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class ServicecatalogueAPI {

	@Autowired
	CatalogueRepository catalogueRepository;

	@Autowired
	AuthManager authManager;

	@PostMapping("/catalogue")
	@Operation(summary = "Post Catalogue", description = "Post a single catalogue")
	public Catalogue postCatalogue(@Valid @RequestBody Catalogue catalogue) {

		catalogue.setCompanyId(authManager.getUserDetails().getCompanyId());
		catalogue.setDateCreated(new Date());
		catalogue.setDateUpdated(new Date());
		catalogue.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		catalogue.setCreatedBy(authManager.getUserDetails().getUserId());

		return catalogueRepository.save(catalogue, "catalogues");
	}

	@PutMapping("/catalogue")
	@Operation(summary = "Put Catalogue", description = "Update a Catalogue")
	public Catalogue putCatalogue(@Valid @RequestBody Catalogue catalogue) {

		Optional<Catalogue> optional = catalogueRepository.findById(catalogue.getCatalogueId(), "catalogues");
		if (optional.isEmpty()) {
			String vars[] = { "CATALOGUE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
        
		catalogue.setCompanyId(authManager.getUserDetails().getCompanyId());
		catalogue.setDateUpdated(new Date());
		catalogue.setLastUpdatedBy(authManager.getUserDetails().getUserId());

		return catalogueRepository.save(catalogue, "catalogues");

	}

	@DeleteMapping("/catalogue/{id}")
	@Operation(summary = "Delete Catalogue", description = "Delete a Catalogue by ID")
	public void deleteCatalogue(@Parameter(description = "Catalogue ID", required = true) @PathVariable String id) {
		Optional<Catalogue> optional = catalogueRepository.findCatalogueByIdAndCompanyId(id,
				authManager.getUserDetails().getCompanyId(), "catalogues");
		if (optional.isEmpty()) {
			String vars[] = { "CATALOGUE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		catalogueRepository.deleteById(id, authManager.getUserDetails().getCompanyId(), "catalogues");

	}
}
