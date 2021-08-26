package com.ngdesk.data.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.ngdesk.data.sam.dao.NormalizationRule;
import com.ngdesk.data.sam.dao.SamSoftwareListener;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.normalization.rule.NormalizationRuleRepository;

//@Component
public class NormalizationJob { 

	@Autowired
	NormalizationRuleRepository normalizationRulesRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	SamSoftwareListener samSoftwareListener;
	
	@Autowired
	EmailService emailService;

	@Scheduled(initialDelay = 60000,fixedRate = 24 * 60 * 60 * 1000)
	public void run() {
		List<Company> companies = companyRepository.findAll(new Query(), "companies");
		List<NormalizationRule> globalNormalizationRules = normalizationRulesRepository.findAllNormalizedRules();

		companies.forEach(company -> {
			String companyId = company.getCompanyId();

			Optional<List<Module>> optionalModules = modulesRepository.findAllModules("modules_" + companyId);
			List<Module> allModules = optionalModules.get();

			Module accountsModule = allModules.stream().filter(module -> module.getName().equals("Accounts"))
					.findFirst().orElse(null);
			Module productsModule = allModules.stream().filter(module -> module.getName().equals("Software Products"))
					.findFirst().orElse(null);
			Module standardizedSoftwareInstallationModule = allModules.stream()
					.filter(module -> module.getName().equals("Standardized Software Installation")).findFirst()
					.orElse(null);

			List<NormalizationRule> localNormalizationRules = normalizationRulesRepository
					.findAllNormalizedRulesForCompany(companyId);

			long count = entryRepository.findCountOfEntries("Standardized_Software_Installation_" + companyId);

			int pageNumber = 0;
			for (int i = 0; i < count; i += 1000) {
				Pageable pageable = PageRequest.of(pageNumber, 1000);

				List<Map<String, Object>> entriesToNormalize = entryRepository
						.findAllEntries("Standardized_Software_Installation_" + companyId, pageable);

				for (Map<String, Object> entryToNormalize : entriesToNormalize) {
					for (NormalizationRule rule : globalNormalizationRules) {
						evaluateRule(rule, entryToNormalize, accountsModule, productsModule,
								standardizedSoftwareInstallationModule, companyId);
					}
					for (NormalizationRule rule : localNormalizationRules) {
						evaluateRule(rule, entryToNormalize, accountsModule, productsModule,
								standardizedSoftwareInstallationModule, companyId);
					}
				}
				pageNumber++;
			}
		});
	}

	public void evaluateRule(NormalizationRule rule, Map<String, Object> entryToNormalize, Module accountsModule,
			Module productsModule, Module standardizedSoftwareInstallationModule, String companyId) {

		try {
			Map<String, Object> user = entryRepository
					.findById(entryToNormalize.get("CREATED_BY").toString(), "Users_" + companyId).get();

			String userId = user.get("_id").toString();
			String roleId = user.get("ROLE").toString();
			String userUuid = user.get("USER_UUID").toString();

			if (entryToNormalize.get("DISCOVERED_PUBLISHER") != null && entryToNormalize.get("PUBLISHER") == null) {
				String rawPublisher = entryToNormalize.get("DISCOVERED_PUBLISHER").toString();
				if (rule.getPublisher() != null) {

					String normalizedPublisher = null;
					switch (rule.getPublisher().getOperator()) {
					case "Is":
						if (rawPublisher.equals(rule.getPublisher().getKey())) {
							postNormalizedAccount(rule, accountsModule.getModuleId(), companyId, userId, roleId, userUuid);
							normalizedPublisher = rule.getPublisher().getValue();
						}
						break;
					case "Starts With":
						if (rawPublisher.startsWith(rule.getPublisher().getKey())) {
							postNormalizedAccount(rule, accountsModule.getModuleId(), companyId, userId, roleId, userUuid);
							normalizedPublisher = rule.getPublisher().getValue();
						}
						break;
					case "Ends With":
						if (rawPublisher.endsWith(rule.getPublisher().getKey())) {
							postNormalizedAccount(rule, accountsModule.getModuleId(), companyId, userId, roleId, userUuid);
							normalizedPublisher = rule.getPublisher().getValue();
						}
						break;
					case "Contains":
						if (rawPublisher.contains(rule.getPublisher().getKey())) {
							postNormalizedAccount(rule, accountsModule.getModuleId(), companyId, userId, roleId, userUuid);
							normalizedPublisher = rule.getPublisher().getValue();
						}
						break;
					default:
						break;
					}

					if (normalizedPublisher != null) {
						entryToNormalize.put("PUBLISHER", normalizedPublisher);
						entryToNormalize.put("NORMALIZATION_STATUS", "Normalized");
					}
				}
			}
			
			if (entryToNormalize.get("DISCOVERED_VERSION") != null && entryToNormalize.get("VERSION") == null) {
				String rawVersion = entryToNormalize.get("DISCOVERED_VERSION").toString();
				if (rule.getVersion() != null) {

					String normalizedVersion = null;
					switch (rule.getVersion().getOperator()) {  
					case "Is":
						if (rawVersion.equals(rule.getVersion().getKey())) {
							normalizedVersion = rule.getVersion().getValue();
						}
						break;
					case "Starts With":
						if (rawVersion.startsWith(rule.getVersion().getKey())) {
							normalizedVersion = rule.getVersion().getValue();
						}
						break;
					case "Ends With":
						if (rawVersion.endsWith(rule.getVersion().getKey())) {
							normalizedVersion = rule.getVersion().getValue();
						}
						break;
					case "Contains":
						if (rawVersion.contains(rule.getVersion().getKey())) {
							normalizedVersion = rule.getVersion().getValue();
						}
						break;
					default:
						break;
					}

					if (normalizedVersion != null) {
						entryToNormalize.put("VERSION", normalizedVersion);
						entryToNormalize.put("NORMALIZATION_STATUS", "Normalized");
					}
				}
			}

			String discoveredProductId = entryToNormalize.get("DISCOVERED_PRODUCT").toString();
			Optional<Map<String, Object>> optionalDiscoveredProduct = entryRepository.findById(discoveredProductId,
					"Software_Products_" + companyId);
			if (optionalDiscoveredProduct.isPresent()) {
				Map<String, Object> discoveredProduct = optionalDiscoveredProduct.get();
				String rawProductName = discoveredProduct.get("NAME").toString();

				if (entryToNormalize.get("DISCOVERED_PRODUCT") != null && entryToNormalize.get("PRODUCT") == null) {
					if (rule.getProduct() != null) {
						String normalizedProductName = null;
						switch (rule.getProduct().getOperator()) {
						case "Is":
							if (rawProductName.equals(rule.getProduct().getKey())) {
								normalizedProductName = rule.getProduct().getValue();
							}
							break;
						case "Starts With":
							if (rawProductName.startsWith(rule.getProduct().getKey())) {
								normalizedProductName = rule.getProduct().getValue();
							}
							break;
						case "Ends With":
							if (rawProductName.endsWith(rule.getProduct().getKey())) {
								normalizedProductName = rule.getProduct().getValue();
							}
							break;
						case "Contains":
							if (rawProductName.contains(rule.getProduct().getKey())) {
								normalizedProductName = rule.getProduct().getValue();
							}
							break;
						default:
							break;
						}
						if (normalizedProductName != null) {

							String accountName = null;
							if (entryToNormalize.get("PUBLISHER") != null) {
								accountName = entryToNormalize.get("PUBLISHER").toString();
							}
							if (accountName == null) {
								accountName = entryToNormalize.get("DISCOVERED_PUBLISHER").toString();
							}

							Map<String, Object> account = new HashMap<String, Object>();
							account.put("ACCOUNT_NAME", accountName);

							account = samSoftwareListener.saveEntry(account, accountsModule.getModuleId(),
									moduleService.getModuleFamily(accountsModule.getModuleId(), companyId), companyId,
									userId, roleId, userUuid);
							String accountId = account.get("DATA_ID").toString();

							Map<String, Object> product = new HashMap<String, Object>();
							product.put("NAME", normalizedProductName);
							product.put("ACCOUNT", accountId);
							
							product = samSoftwareListener.saveEntry(product, productsModule.getModuleId(),
									moduleService.getModuleFamily(productsModule.getModuleId(), companyId), companyId, userId, roleId, userUuid);
							String productId = product.get("DATA_ID").toString();

							entryToNormalize.put("PRODUCT", productId);
							entryToNormalize.put("NORMALIZATION_STATUS", "Normalized");
						}
					}
				}
			}
			entryRepository.updateEntry(entryToNormalize, "Standardized_Software_Installation_" + companyId);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();
			emailService.notifyShashankAndSpencerOnError(sStackTrace);
		}
	}

	public String postNormalizedAccount(NormalizationRule rule, String accountsModuleId, String companyId,
			String userId, String roleId, String userUuid) {
		Map<String, Object> normalizedAccount = new HashMap<String, Object>();
		normalizedAccount.put("ACCOUNT_NAME", rule.getPublisher().getValue());
		normalizedAccount = samSoftwareListener.saveEntry(normalizedAccount, accountsModuleId,
				moduleService.getModuleFamily(accountsModuleId, companyId), companyId, userId, roleId, userUuid);
		return normalizedAccount.get("DATA_ID").toString();
	}

}
