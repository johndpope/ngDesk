package com.ngdesk.data.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.mail.EmailService;
import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.sam.dao.DiscoveryMap;
import com.ngdesk.data.sam.dao.SamSoftwareListener;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.discoverymap.DiscoveryMapRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

//@Component
public class DiscoveryMapJob {

	@Autowired
	DiscoveryMapRepository discoveryMapRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	SamSoftwareListener samSoftwareListener;

	@Autowired
	EmailService emailService;

	@Scheduled(initialDelay = 60000,fixedRate = 24 * 60 * 60 * 1000)
	public void run() {
		List<Company> companies = companyRepository.findAll(new Query(), "companies");
		companies.forEach(company -> {
			String companyId = company.getCompanyId();

			Optional<List<Module>> optionalModules = modulesRepository.findAllModules("modules_" + companyId);
			List<Module> allModules = optionalModules.get();

			Module standardizedSoftwareInstallationModule = allModules.stream()
					.filter(module -> module.getName().equals("Standardized Software Installation")).findFirst()
					.orElse(null);
			Module softwareModelmodule = allModules.stream().filter(module -> module.getName().equals("Software Model"))
					.findFirst().orElse(null);
			Module softwareProductsModule = allModules.stream()
					.filter(module -> module.getName().equals("Software Products")).findFirst().orElse(null);

			List<DiscoveryMap> discoveryMaps = discoveryMapRepository.findAllDiscoveryMaps();
			discoveryMaps.forEach(discoveryMap -> {
				String discoveryMapId = discoveryMap.getId();

				Optional<Map<String, Object>> optionalSoftwareModel = entryRepository.findEntryByFieldName(
						"DISCOVERY_MAP", discoveryMapId,
						moduleService.getCollectionName(softwareModelmodule.getName(), companyId)); 

				if (optionalSoftwareModel.isPresent()) {
					Optional<List<Map<String, Object>>> optionalStandardizedSoftwares = entryRepository
							.findAllEntriesByFieldName(discoveryMap.getProducts(), "PRODUCT",
									"Standardized_Software_Installation_" + companyId);

					if (optionalStandardizedSoftwares.isPresent()) {
						List<Map<String, Object>> standardizedSoftwareInstallations = optionalStandardizedSoftwares
								.get();

						Map<String, Object> softwareModel = optionalSoftwareModel.get();
						String softwareModelId = softwareModel.get("_id").toString();

						standardizedSoftwareInstallations.forEach(standardizedSoftwareInstallation -> {
							Map<String, Object> user = entryRepository
									.findById(standardizedSoftwareInstallation.get("CREATED_BY").toString(),
											"Users_" + companyId)
									.get();

							String userId = user.get("_id").toString();
							String roleId = user.get("ROLE").toString();
							String userUuid = user.get("USER_UUID").toString();

							standardizedSoftwareInstallation.put("SOFTWARE_MODEL", softwareModelId);
							samSoftwareListener
									.saveEntry(standardizedSoftwareInstallation,
											standardizedSoftwareInstallationModule.getModuleId(),
											moduleService.getModuleFamily(
													standardizedSoftwareInstallationModule.getModuleId(), companyId),
											companyId, userId, roleId, userUuid);
						});
					}
				}
			});

		});

	}

}
