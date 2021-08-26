package com.ngdesk.websocket.sam.dao;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ControllerRepository;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class ProbeController {

	@Autowired
	ControllerRepository controllerRepository;

	@Autowired
	CompaniesRepository companiesRepository;

	public void updateLastSeen(String controllerId, String subdomain, String applicationName) {

		Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			Optional<Controller> optionalController = controllerRepository.findByIdAndCompanyId(controllerId,
					company.getId(), "controllers");
			if (optionalController.isPresent()) {
				
				if (applicationName.equals("ngDesk-Controller")) {
					controllerRepository.updateControllerLastSeen(controllerId, company.getId(), "controllers");
				} else {
					controllerRepository.updateSubAppLastSeen(controllerId, applicationName, company.getId(),
							"controllers");
				}
			}
		}

	}
}
