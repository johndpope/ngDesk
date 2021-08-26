package com.ngdesk.module.catalogue.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.form.dao.Form;
import com.ngdesk.repositories.CatalogueRepository;
import com.ngdesk.repositories.FormRepository;

@Component
public class CatalogueBeforeSaveListener extends AbstractMongoEventListener<Catalogue> {

	@Autowired
	CatalogueRepository catalogueRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	FormRepository formRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Catalogue> event) {
		Catalogue catalogue = event.getSource();

		if (catalogue.getCatalogueId() == null) {
			String  catalogueName = catalogue.getName();
			Optional<Catalogue> optional = catalogueRepository.findCatalogueByName(catalogueName,
					catalogue.getCompanyId(), event.getCollectionName());
			if (optional.isPresent()) {
				String[] variables = { "CATALOGUE", "NAME", catalogueName};
				throw new BadRequestException("CATALOGUE_NAME_ALREADY_EXISTS", variables);
			}

		} else {
			String  catalogueName = catalogue.getName();
			Optional<Catalogue> optional = catalogueRepository.findOtherCatalogueWithDuplicateName(catalogueName,
					catalogue.getCatalogueId(), catalogue.getCompanyId(), event.getCollectionName());
			if (optional.isPresent()) {
				String[] variables = { "CATALOGUE", "NAME", catalogueName};
				throw new BadRequestException("CATALOGUE_NAME_ALREADY_EXISTS", variables);
			}
		}
		// Validating Module Id's
		if (catalogue != null && catalogue.getCatalogueForms() != null && catalogue.getCatalogueForms().size() > 0) {
			for (CatalogueForm catalogueForms : catalogue.getCatalogueForms()) {
				Optional<Module> module = catalogueRepository.findByModuleId(catalogueForms.getModuleId(),
						"modules_" + catalogue.getCompanyId());
				if (module.isEmpty()) {
					String[] variables = { "CATALOGUE_MODULE" };
					throw new BadRequestException("CATALOGUE_INVALID_MODULE", variables);
				}
			}
		}
		if (catalogue != null && catalogue.getCatalogueForms() != null && catalogue.getCatalogueForms().size() > 0) {
			for (CatalogueForm catalogueForms : catalogue.getCatalogueForms()) {
				Optional<Form> form = formRepository.findFormById(catalogueForms.getFormId(),
						authManager.getUserDetails().getCompanyId(), catalogueForms.getModuleId(), "forms");
				if (form.isEmpty()) {
					throw new BadRequestException("INVALID_FORM_ID", null);
				}
			}
		}

	}
}
