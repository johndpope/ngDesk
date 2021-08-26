package com.ngdesk.company.module.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.plugin.dao.Plugin;
import com.ngdesk.company.plugin.dao.Tier;
import com.ngdesk.company.plugin.dao.TierModule;
import com.ngdesk.company.rolelayout.dao.RoleLayout;
import com.ngdesk.company.rolelayout.dao.Tab;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.PluginRepository;
import com.ngdesk.repositories.RoleLayoutRepository;
import com.ngdesk.repositories.WorkflowRepository;
import com.ngdesk.workflow.dao.Workflow;

@Component
public class ModuleService {

	@Autowired
	PluginRepository pluginRepository;

	@Autowired
	ModuleService moduleService;

	public Map<String, ObjectId> modulesMap;

	public Map<String, List<String>> ignoreModulesMap;

	@Autowired
	Global global;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	RoleLayoutRepository roleLayoutRepository;

	@PostConstruct
	public void init() {

		moduleService.ignoreModulesMap = new HashMap<String, List<String>>();
		moduleService.ignoreModulesMap.put("Software Installation", Arrays.asList("Software Installation", "Products"));
		moduleService.ignoreModulesMap.put("Standardized Software Installation", Arrays.asList("Products"));
		moduleService.ignoreModulesMap.put("Software Model", Arrays.asList("Products", "Software Installation"));

		moduleService.modulesMap = new HashMap<String, ObjectId>();

//		A
		moduleService.modulesMap.put("Accounts", new ObjectId("5f68d2d296151b30f85b4f0f"));
		moduleService.modulesMap.put("Assets", new ObjectId("5f68d2d296151b30f85b4f14"));

//		B
//		moduleService.modulesMap.put("Bonus Issuances", new ObjectId("5f68d2d296151b30f85b4f15"));

//		C
		moduleService.modulesMap.put("Chat", new ObjectId("5f68d2d296151b30f85b4f13"));
		moduleService.modulesMap.put("Contacts", new ObjectId("5f68d2d296151b30f85b4f16"));
		moduleService.modulesMap.put("Contracts", new ObjectId("5f68d2d296151b30f85b4f17"));
		moduleService.modulesMap.put("Change Request", new ObjectId("60584b21b849a94aabb0bd08"));
		moduleService.modulesMap.put("Categories", new ObjectId("5ffd5e4066f15f52f9d734c2"));

//		D
//		moduleService.modulesMap.put("Document Checklist", new ObjectId("5f68d6146346cc4b969ac07c"));

//		E
//		moduleService.modulesMap.put("Emails", new ObjectId("5f68d6146346cc4b969ac07d"));
//		moduleService.modulesMap.put("Employees", new ObjectId("5f68d6146346cc4b969ac07e"));
//		moduleService.modulesMap.put("Equipment Checkouts", new ObjectId("5f68d6146346cc4b969ac07f"));
//		moduleService.modulesMap.put("Exit Details", new ObjectId("5f68d6146346cc4b969ac080"));
		moduleService.modulesMap.put("Expenses", new ObjectId("5f8e89a6017dc4294735cff1"));
		moduleService.modulesMap.put("Expense Reports", new ObjectId("5f8e89b50b817042f3195a21"));

//		F
//		moduleService.modulesMap.put("Firewalls", new ObjectId("5f68d6146346cc4b969ac081"));

//		G
		moduleService.modulesMap.put("GL Codes", new ObjectId("5ffd5e2766f15f52f9d734c1"));

//		H
//		moduleService.modulesMap.put("Healthcare Insurances", new ObjectId("5f68d6146346cc4b969ac082"));

//		I
		moduleService.modulesMap.put("Invoices", new ObjectId("5f68ce21f9299f5228b4da6e"));

//		L
		moduleService.modulesMap.put("Laptops", new ObjectId("5f68d6146346cc4b969ac083"));
//		moduleService.modulesMap.put("Leads", new ObjectId("5f68d6146346cc4b969ac084"));
		moduleService.modulesMap.put("Licenses", new ObjectId("5f68d6146346cc4b969ac085"));
		moduleService.modulesMap.put("License Transaction", new ObjectId("5f68d65327a8147d495646c4"));

//		N
//		moduleService.modulesMap.put("Network Switches", new ObjectId("5f68d65327a8147d495646c5"));

//		O
//		moduleService.modulesMap.put("Onboardings", new ObjectId("5f68d65327a8147d495646c6"));
		moduleService.modulesMap.put("Opportunities", new ObjectId("5f68d65327a8147d495646c7"));

//		P
		moduleService.modulesMap.put("Patch", new ObjectId("5f68d65327a8147d495646c8"));
		moduleService.modulesMap.put("Physical Servers", new ObjectId("5f68d65327a8147d495646c9"));
		moduleService.modulesMap.put("Products", new ObjectId("5f68d65327a8147d495646ca"));
//		moduleService.modulesMap.put("Promotions", new ObjectId("5f68d65327a8147d495646cb"));
		moduleService.modulesMap.put("Purchase Assignment", new ObjectId("5f68d65327a8147d495646cc"));
		moduleService.modulesMap.put("Personally Identifiable Information", new ObjectId("5fd8549e92846e21bfba7f0c"));

//		Q
		moduleService.modulesMap.put("Quotes", new ObjectId("5f68d65327a8147d495646cd"));

//		R
		moduleService.modulesMap.put("Revenue Line Items", new ObjectId("5f68d6849cbefb3bef9ea676"));

//		S
//		moduleService.modulesMap.put("Storage Appliances", new ObjectId("5f68d6849cbefb3bef9ea677"));
		moduleService.modulesMap.put("Standardized Software Installation", new ObjectId("5f9f678761e2817471781cca"));
		moduleService.modulesMap.put("Software Installation", new ObjectId("5f9f678761e2817471781cc9"));
		moduleService.modulesMap.put("Software Model", new ObjectId("5fb647d6af70447f509dcbc7"));
		moduleService.modulesMap.put("Software Products", new ObjectId("5fa03b45c566855b39c6ef55"));

//		T
		moduleService.modulesMap.put("Teams", new ObjectId("5f68d2d296151b30f85b4f10"));
		moduleService.modulesMap.put("Tickets", new ObjectId("5f68d2d296151b30f85b4f12"));
//		moduleService.modulesMap.put("Time Off Requests", new ObjectId("5f68d6849cbefb3bef9ea678"));
//		moduleService.modulesMap.put("Travel Authorizations", new ObjectId("5f68d6849cbefb3bef9ea679"));
//		moduleService.modulesMap.put("Travel Expense Line Items", new ObjectId("5f68d6849cbefb3bef9ea67a"));
//		moduleService.modulesMap.put("Travel Expenses", new ObjectId("5f68d6849cbefb3bef9ea67b"));
//		moduleService.modulesMap.put("Travel Itinerary Line Items", new ObjectId("5f68d6849cbefb3bef9ea67c"));
		moduleService.modulesMap.put("Taxes", new ObjectId("5ffd5d9666f15f52f9d734c0"));

//		U
//		moduleService.modulesMap.put("UPS", new ObjectId("5f68d6849cbefb3bef9ea67d"));
		moduleService.modulesMap.put("Users", new ObjectId("5f68d2d296151b30f85b4f11"));

//		V
//		moduleService.modulesMap.put("Virtual Servers", new ObjectId("5f68d6849cbefb3bef9ea67e"));

//		W
//		moduleService.modulesMap.put("Wireless Access Points", new ObjectId("5f68d6849cbefb3bef9ea67f"));
//		moduleService.modulesMap.put("Workstations", new ObjectId("5f68d6ab891be650e1dd5a36"));
	}

	public void postCoreModules(Company company, Map<String, String> rolesMap, String globalTeamId, String adminTeamId,
			String agentTeamId, String customersTeamId) {
		try {
			String[] coreModules = { "Users", "Accounts", "Teams", "Contacts" };
			for (String coreModule : coreModules) {

				String fileName = coreModule + "Module.json";
				String moduleJson = global.getFile(fileName);

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				moduleJson = moduleJson.replaceAll("DATE_CREATED_REPLACE", format.format(new Date()));
				moduleJson = moduleJson.replaceAll("LAST_UPDATED_REPLACE", format.format(new Date()));
				moduleJson = moduleJson.replaceAll("DATE_REPLACE", format.format(new Date()));

				moduleJson = moduleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
				moduleJson = moduleJson.replaceAll("ADMIN_TEAM_REPLACE", adminTeamId);
				moduleJson = moduleJson.replaceAll("AGENT_TEAM_REPLACE", agentTeamId);
				moduleJson = moduleJson.replaceAll("CUSTOMERS_TEAM_REPLACE", customersTeamId);

				for (String role : rolesMap.keySet()) {
					String name = role.toUpperCase().replaceAll("\\s+", "_") + "_ROLE_REPLACE";
					moduleJson = moduleJson.replaceAll(name, rolesMap.get(role));
				}

				for (String moduleName : moduleService.modulesMap.keySet()) {
					String name = moduleName.toUpperCase().replaceAll("\\s+", "_") + "_MODULE_REPLACE";
					moduleJson = moduleJson.replaceAll(name, moduleService.modulesMap.get(moduleName).toString());
				}

				Module module = new ObjectMapper().readValue(moduleJson, Module.class);
				String moduleId = moduleService.modulesMap.get(module.getName()).toString();
				module.setModuleId(moduleId);
				moduleRepository.save(module, "modules_" + company.getCompanyId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("CORE_MODULES_POST_FAILED", null);
		}
	}

	public void postPluginModules(Company company, Map<String, String> rolesMap, String globalTeamId,
			String adminTeamId, String agentTeamId, String customersTeamId) {
		for (String pluginName : company.getPlugins()) {
			try {
				Optional<Plugin> optionalPlugin = pluginRepository.findPluginByName(pluginName);

				if (optionalPlugin.isEmpty()) {
					String[] vars = { pluginName };
					throw new BadRequestException("PLUGIN_MISSING", vars);
				}

				Plugin plugin = optionalPlugin.get();

				// TODO: Change to appropriate tier in the future
				Tier freeTier = plugin.getTiers().stream().filter(tier -> tier.getName().equalsIgnoreCase("free"))
						.findFirst().orElse(null);

				List<TierModule> tierModules = freeTier.getModules();

				for (TierModule tierModule : tierModules) {
					postModule(rolesMap, tierModule.getName(), company, globalTeamId, adminTeamId, agentTeamId,
							customersTeamId);

					if (tierModule.getChildModule() != null) {
						for (String moduleName : tierModule.getChildModule()) {
							postModule(rolesMap, moduleName, company, globalTeamId, adminTeamId, agentTeamId,
									customersTeamId);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				String[] vars = { pluginName };
				throw new BadRequestException("PLUGINS_POST_FAILED", vars);
			}
		}

	}

	public void postModule(Map<String, String> rolesMap, String name, Company company, String globalTeamId,
			String adminTeamId, String agentTeamId, String customersTeamId) {
		try {
			String fileName = name.replaceAll("\\s+", "") + "Module.json";
			String moduleJson = global.getFile(fileName);

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			moduleJson = moduleJson.replaceAll("DATE_CREATED_REPLACE", format.format(new Date()));
			moduleJson = moduleJson.replaceAll("DATE_REPLACE", format.format(new Date()));
			moduleJson = moduleJson.replaceAll("LAST_UPDATED_REPLACE", format.format(new Date()));
			moduleJson = moduleJson.replaceAll("DATE_REPLACE", format.format(new Date()));

			for (String role : rolesMap.keySet()) {
				String roleName = role.toUpperCase().replaceAll("\\s+", "_") + "_ROLE_REPLACE";
				moduleJson = moduleJson.replaceAll(roleName, rolesMap.get(role));
			}

			moduleJson = moduleJson.replaceAll("GLOBAL_TEAM_REPLACE", globalTeamId);
			moduleJson = moduleJson.replaceAll("ADMIN_TEAM_REPLACE", adminTeamId);
			moduleJson = moduleJson.replaceAll("AGENT_TEAM_REPLACE", agentTeamId);
			moduleJson = moduleJson.replaceAll("CUSTOMERS_TEAM_REPLACE", customersTeamId);

			for (String moduleName : moduleService.modulesMap.keySet()) {
				if (moduleService.ignoreModulesMap.containsKey(name)
						&& moduleService.ignoreModulesMap.get(name).contains(moduleName)) {
					continue;
				}

				String nameToReplace = moduleName.toUpperCase().replaceAll("\\s+", "_") + "_MODULE_REPLACE";
				moduleJson = moduleJson.replaceAll(nameToReplace, moduleService.modulesMap.get(moduleName).toString());
			}

			moduleJson = moduleJson.replaceAll("COMPANY_SUBDOMAIN_REPLACE", company.getCompanySubdomain());

			Module module = new ObjectMapper().readValue(moduleJson, Module.class);
			String workflowJson = null;

			if (module.getName().equals("Change Request")) {

				workflowJson = global.getFile("ChangeRequestsWorkflows.json");
				workflowJson = workflowJson.replaceAll("ADMIN_TEAM_REPLACE", adminTeamId);
				workflowJson = workflowJson.replaceAll("DATE_REPLACE", format.format(new Date()));
				ObjectMapper mapper = new ObjectMapper();
				List<Workflow> changeRequestWorkflow = mapper.readValue(workflowJson,
						mapper.getTypeFactory().constructCollectionType(List.class, Workflow.class));
				module.setWorkflows(changeRequestWorkflow);

			}

			if (module.getName().equals("Expense Reports")) {

				workflowJson = global.getFile("ExpenseReportsWorkflows.json");
				workflowJson = workflowJson.replaceAll("ADMIN_TEAM_REPLACE", adminTeamId);
				ObjectMapper mapper = new ObjectMapper();
				List<Workflow> expenseReportWorkflow = mapper.readValue(workflowJson,
						mapper.getTypeFactory().constructCollectionType(List.class, Workflow.class));
				module.setWorkflows(expenseReportWorkflow);

			}

			if (module.getName().equals("Chat")) {
				moduleService.postChatChannel(company, globalTeamId);
			}

			String moduleId = moduleService.modulesMap.get(module.getName()).toString();
			module.setModuleId(moduleId);

			if (module.getName().equals("Patch")) {
				if (module.getWorkflows() != null) {
					List<Workflow> workflows = module.getWorkflows();
					for (Workflow workflow : workflows) {
						workflow.setId(new ObjectId().toString());
					}
					Workflow workflow = workflows.stream().filter(wFlow -> wFlow.getName().equals("Notify Probe"))
							.findFirst().get();
					ModuleField moduleField = module.getFields().stream()
							.filter(field -> field.getName().equals("APPLY_PATCH")).findFirst().get();
					moduleField.setWorkflow(workflow.getId());
				}
			}
			moduleRepository.save(module, "modules_" + company.getCompanyId());
			postModuleWorkflows(module, company);

		} catch (Exception e) {
			e.printStackTrace();
			String[] vars = { name };
			throw new BadRequestException("MODULES_POST_FAILED", vars);
		}
	}

	public void postModuleWorkflows(Module module, Company company) {
		try {
			if (module.getWorkflows() != null) {
				List<Workflow> workflows = module.getWorkflows();
				for (Workflow workflow : workflows) {
					workflow.setCompanyId(company.getCompanyId());
					workflow.setModuleId(module.getModuleId());
					workflowRepository.save(workflow, "module_workflows");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			String[] vars = { module.getName() };
			throw new BadRequestException("MODULE_WORKFLOWS_POST_FAILED", vars);
		}
	}

	// TODO: Use Class
	public void postTicketChannel(Company company, String globalTeamId) {
		try {
			String ticketChannelJson = global.getFile("TicketChannel.json");
			ticketChannelJson = ticketChannelJson.replaceAll("SUPPORT_EMAIL_ADDRESS", company.getCompanySubdomain());
			ticketChannelJson = ticketChannelJson.replaceAll("TICKETS_MODULE_REPLACE",
					moduleService.modulesMap.get("Tickets").toString());
			ticketChannelJson = ticketChannelJson.replaceAll("TEAM_ID_REPLACE", globalTeamId);
			Map<String, Object> ticketsChannel = new ObjectMapper().readValue(ticketChannelJson, Map.class);
			entryRepository.save(ticketsChannel, "channels_email_" + company.getCompanyId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("EMAIL_CHANNEL_POST_FAILED", null);
		}
	}

	public void postChatChannel(Company company, String globalTeamId) {
		try {
			String chatChannelJson = global.getFile("ChatChannel.json");
			chatChannelJson = chatChannelJson.replaceAll("COMPANY_SUBDOMAIN_REPLACE", company.getCompanySubdomain());
			chatChannelJson = chatChannelJson.replaceAll("MODULE_ID", moduleService.modulesMap.get("Chat").toString());
			chatChannelJson = chatChannelJson.replaceAll("COMPANY_TIMEZONE", company.getTimezone());
			Map<String, Object> chatChannel = new ObjectMapper().readValue(chatChannelJson, Map.class);
			entryRepository.save(chatChannel, "channels_chat_" + company.getCompanyId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("CHAT_CHANNEL_POST_FAILED", null);
		}

	}

	// TODO: Use class
	public void postDefaultDashboards(String companyId, Map<String, String> rolesMap) {
		try {
			Optional<Module> optionalTicket = moduleRepository.findModuleByName("Tickets", "modules_" + companyId);
			Module ticketsModule = optionalTicket.get();
			String ticketsModuleId = ticketsModule.getModuleId();

			String adminDashboardJson = global.getFile("DefaultStoryboardAdmin.json");
			String agentDashboardJson = global.getFile("DefaultStoryboardAgent.json");

			adminDashboardJson = adminDashboardJson.replaceAll("MODULE_ID_REPLACE", ticketsModuleId);
			adminDashboardJson = adminDashboardJson.replaceAll("COMPANY_ID", companyId);
			adminDashboardJson = adminDashboardJson.replaceAll("ADMIN_REPLACE", rolesMap.get("SystemAdmin"));

			agentDashboardJson = agentDashboardJson.replaceAll("MODULE_ID_REPLACE", ticketsModuleId);
			agentDashboardJson = agentDashboardJson.replaceAll("COMPANY_ID", companyId);
			agentDashboardJson = agentDashboardJson.replaceAll("AGENT_REPLACE", rolesMap.get("Agent"));

			Map<String, Object> adminDashboard = new ObjectMapper().readValue(adminDashboardJson, Map.class);
			Map<String, Object> agentDashboard = new ObjectMapper().readValue(agentDashboardJson, Map.class);

			entryRepository.save(adminDashboard, "dashboards");
			entryRepository.save(agentDashboard, "dashboards");

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("DEFAULT_DASHBOARDS_POST_FAILED", null);
		}
	}

	public List<Module> getModuleFamily(Module currentModule, List<Module> modules) {
		List<Module> moduleFamily = new ArrayList<Module>();
		moduleFamily.add(currentModule);

		while (currentModule != null && currentModule.getParentModule() != null) {
			String parentModuleId = currentModule.getParentModule();
			currentModule = modules.stream().filter(module -> module.getModuleId().equals(parentModuleId)).findFirst()
					.orElse(null);
			if (currentModule != null) {
				moduleFamily.add(currentModule);
			} else {
				break;
			}
		}
		return moduleFamily;
	}

	public List<ModuleField> getAllFields(Module currentModule, List<Module> modules) {
		List<ModuleField> allFields = new ArrayList<ModuleField>();

		List<Module> moduleFamily = getModuleFamily(currentModule, modules);

		Set<String> fieldNames = new HashSet<String>();
		moduleFamily.forEach(module -> {
			module.getFields().forEach(field -> {
				if (!fieldNames.contains(field.getName())) {
					allFields.add(field);
					fieldNames.add(field.getName());
				}
			});
		});

		return allFields;
	}

}
