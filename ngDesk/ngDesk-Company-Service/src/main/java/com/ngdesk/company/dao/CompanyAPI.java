package com.ngdesk.company.dao;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.company.currency.dao.CurrencyService;
import com.ngdesk.company.dns.dao.DNSService;
import com.ngdesk.company.elastic.ElasticService;
import com.ngdesk.company.knowledgebase.dao.DefaultKnowledgeBaseService;
import com.ngdesk.company.module.dao.ModuleService;
import com.ngdesk.company.module.data.DataService;
import com.ngdesk.company.role.dao.RoleService;
import com.ngdesk.company.security.dao.CompanySecutiriesService;
import com.ngdesk.company.sidebar.dao.SidebarService;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;

@RestController
@RefreshScope
public class CompanyAPI {

	private final Logger log = LoggerFactory.getLogger(CompanyAPI.class);

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private SidebarService sidebarService;

	@Autowired
	private DNSService dnsService;

	@Autowired
	private CurrencyService currencyService;

	@Autowired
	private CompanySecutiriesService companySecurityService;

	@Autowired
	DefaultKnowledgeBaseService knowledgeBaseService;

	@Autowired
	RoleService roleService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ElasticService elasticService;

	@Autowired
	DataService dataService;

	@Autowired
	CompanyAPI companyAPI;

	@Autowired
	Global global;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	private AuthManager authManager;

	@PostMapping("/company")
	@Transactional
	public Company postCompany(@RequestBody @Valid Company company,
			@RequestParam(value = "utm_source", required = false) String utmSource,
			@RequestParam(value = "utm_term", required = false) String utmTerm,
			@RequestParam(value = "utm_medium", required = false) String utmMedium,
			@RequestParam(value = "utm_content", required = false) String utmContent,
			@RequestParam(value = "utm_campaign", required = false) String utmCampaign) {

		if (company.getHiddenField() != null && !company.getHiddenField().isBlank()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		company = companyService.setTracking(company, utmSource, utmTerm, utmMedium, utmContent, utmCampaign);

		companyAPI.postCompanyDetails(company);

		publishToElastic(company.getCompanyId());

		if (!company.getPricing().equalsIgnoreCase("free")) {
			companyService.notifyEmailToSpencerAndSandra(company);
		}

		companyService.sendEmailFromSendGrid(company);

		// SEND VERIFICATION EMAIL
		companyService.sendVerificationEmail(company);

		return company;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void postCompanyDetails(Company company) {

		postPlugins();
		postPredefinedTemplates();
		company.setEmailAddress(company.getEmailAddress().toLowerCase());
		company.setCompanySubdomain(company.getCompanySubdomain().toLowerCase());
		company.setInviteMessage(companyService.getInviteMessage(company));
		company.setSignupMessage(companyService.getSignupMessage(company));
		company.setForgotPasswordMessage(companyService.getForgotPasswordMessage(company));
		company.setCompanyId(companyService.getNewObjectId().toString());
		company.setVersion("v2");

		companyRepository.save(company, "companies");
		Map<String, String> rolesMap = roleService.postDefaultRoles(company);

		// INSERT SIDEBAR
		sidebarService.postDefaultSidebar(company, rolesMap);

		// INSERT INTO CUSTOM LOGIN
		companyService.setCustomLogin(company);

		// POST DEFAULT CURRENCIES
		currencyService.postCurrency(company.getCompanyId());

		// POST DEFAULT ROLE LAYOUT
		roleService.postDefaultRoleLayouts(company, rolesMap);

		// POST A DNS RECORD
		dnsService.postIntoDnsRecords(company);

		if (company.getDomain() != null && !company.getDomain().isBlank()) {
			dnsService.setCnameRecordForOnPrem(company);
		}

		ObjectId globalTeamId = new ObjectId();
		ObjectId adminTeamId = new ObjectId();
		ObjectId salesTeamId = new ObjectId();
		ObjectId spenderId = new ObjectId();
		ObjectId accountingManagerId = new ObjectId();
		ObjectId accountantId = new ObjectId();
		ObjectId agentTeamId = new ObjectId();
		ObjectId customersTeamId = new ObjectId();

		// OTHER DEFAULTS
		companySecurityService.postCompanySecurity(company);
		companyService.setUserTracker(company);
		companyService.setDefaultNgdeskLogo(company);

		moduleService.postCoreModules(company, rolesMap, globalTeamId.toString(), adminTeamId.toString(),
				agentTeamId.toString(), customersTeamId.toString());
		moduleService.postPluginModules(company, rolesMap, globalTeamId.toString(), adminTeamId.toString(),
				agentTeamId.toString(), customersTeamId.toString());

		dataService.postDefaultModuleEntries(company, rolesMap, globalTeamId, adminTeamId, salesTeamId, agentTeamId,
				customersTeamId, spenderId, accountingManagerId, accountantId);

		moduleService.postTicketChannel(company, globalTeamId.toString());
		moduleService.postDefaultDashboards(company.getCompanyId(), rolesMap);

		elasticService.loadModuleDataIntoFieldLookUp(company.getCompanyId());

	}


	public void publishToElastic(String companyId) {
		try {
			log.debug("Publishing to Elastic queue");
			rabbitTemplate.convertAndSend("post-module-data-to-elastic", companyId);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postPlugins() {
		try {
			Query query = new Query(Criteria.where("NAME").is("Ticketing"));
			List<Map<String, Object>> existingPlugins = entryRepository.findAll(query, "plugins");
			if (existingPlugins.size() == 0) {
				String pluginsJson = global.getFile("plugins.json");
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> pluginsObj = mapper.readValue(pluginsJson, Map.class);
				List<Map<String, Object>> plugins = (List<Map<String, Object>>) pluginsObj.get("PLUGINS");
				for (Map<String, Object> plugin : plugins) {
					entryRepository.save(plugin, "plugins");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postPredefinedTemplates() {
		try {
			Query query = new Query(Criteria.where("MODULE_NAME").is("Tickets"));
			List<Map<String, Object>> existingTemplates = entryRepository.findAll(query, "predefined_templates");
			if (existingTemplates.size() == 0) {
				ObjectMapper mapper = new ObjectMapper();
				String templatesJson = global.getFile("predefined_template.json");
				Map<String, Object> templatesObj = mapper.readValue(templatesJson, Map.class);
				List<Map<String, Object>> templates = (List<Map<String, Object>>) templatesObj
						.get("PREDEFINED_TEMPLATE");
				for (Map<String, Object> template : templates) {
					entryRepository.save(template, "predefined_templates");
				}
			}
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

}
