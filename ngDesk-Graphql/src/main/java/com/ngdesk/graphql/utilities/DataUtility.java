package com.ngdesk.graphql.utilities;

import static graphql.GraphQL.newGraphQL;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.graphql.approval.dao.ApprovalDeniedDataFetcher;
import com.ngdesk.graphql.approval.dao.ApprovalOngoingDataFetcher;
import com.ngdesk.graphql.catalogue.dao.CatalogueCountDataFetcher;
import com.ngdesk.graphql.catalogue.dao.CatalogueDataFetcher;
import com.ngdesk.graphql.catalogue.dao.CatalogueFormDataFetcher;
import com.ngdesk.graphql.catalogue.dao.CataloguesDataFetcher;
import com.ngdesk.graphql.categories.dao.CategoriesCountDataFetcher;
import com.ngdesk.graphql.categories.dao.CategoriesDataFetcher;
import com.ngdesk.graphql.categories.dao.CategoryDataFetcher;
import com.ngdesk.graphql.channels.email.dao.ChannelDataFetcher;
import com.ngdesk.graphql.chat.channel.dao.ChatChannelDataFetcher;
import com.ngdesk.graphql.chat.channel.dao.ChatChannelsDataFetcher;
import com.ngdesk.graphql.chat.channel.dao.ChatPromptDataFetcher;
import com.ngdesk.graphql.chat.channel.dao.ChatPromptsDataFetcher;
import com.ngdesk.graphql.company.dao.Company;
import com.ngdesk.graphql.company.dao.CompanyDataFetcher;
import com.ngdesk.graphql.currency.dao.CurrenciesDataFetcher;
import com.ngdesk.graphql.currency.dao.CurrencyDataFetcher;
import com.ngdesk.graphql.dashboards.dao.AdvancedPieChartValueFetcher;
import com.ngdesk.graphql.dashboards.dao.BarchartValueFetcher;
import com.ngdesk.graphql.dashboards.dao.DashboardCountFetcher;
import com.ngdesk.graphql.dashboards.dao.DashboardDataFetcher;
import com.ngdesk.graphql.dashboards.dao.DashboardsDataFetcher;
import com.ngdesk.graphql.dashboards.dao.MultiScorecardValueFetcher;
import com.ngdesk.graphql.dashboards.dao.PieChartValueFetcher;
import com.ngdesk.graphql.dashboards.dao.ScorecardValueFetcher;
import com.ngdesk.graphql.dashboards.dao.WidgetEntriesCountFetcher;
import com.ngdesk.graphql.dashboards.dao.WidgetEntriesDataFetcher;
import com.ngdesk.graphql.datatypes.DateTime;
import com.ngdesk.graphql.discoverymap.dao.DiscoveryMapDataFetcher;
import com.ngdesk.graphql.discoverymap.dao.DiscoveryMapsDataFetcher;
import com.ngdesk.graphql.discoverymap.dao.UnApprovedDiscoveryMapsDataFetcher;
import com.ngdesk.graphql.enterprisesearch.dao.EnterpriseSearchCountFetcher;
import com.ngdesk.graphql.enterprisesearch.dao.EnterpriseSearchDataFetcher;
import com.ngdesk.graphql.enterprisesearch.dao.EnterpriseSearchesDataFetcher;
import com.ngdesk.graphql.enterprisesearch.dao.UnapprovedEnterpriseSearchDataFetcher;
import com.ngdesk.graphql.escalation.dao.EscalationDataFetcher;
import com.ngdesk.graphql.form.dao.FormCountDataFetcher;
import com.ngdesk.graphql.form.dao.FormDataFetcher;
import com.ngdesk.graphql.form.dao.FormsDataFetcher;
import com.ngdesk.graphql.knowledgebase.article.dao.ArticleDataFetcher;
import com.ngdesk.graphql.knowledgebase.article.dao.ArticlesCountDataFetcher;
import com.ngdesk.graphql.knowledgebase.article.dao.ArticlesDataFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionCategoryDataFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionDataFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionsCountFetcher;
import com.ngdesk.graphql.knowledgebase.section.dao.SectionsDataFetcher;
import com.ngdesk.graphql.modules.dao.FieldDataFetcher;
import com.ngdesk.graphql.modules.dao.FieldsDataFetcher;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleDataFetcher;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesDataFetcher;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.modules.data.dao.AggregationDataFetcher;
import com.ngdesk.graphql.modules.data.dao.AllEntriesFetcher;
import com.ngdesk.graphql.modules.data.dao.ChronometerDataFetcher;
import com.ngdesk.graphql.modules.data.dao.CountDataFetcher;
import com.ngdesk.graphql.modules.data.dao.DistinctEntryValuesFetcher;
import com.ngdesk.graphql.modules.data.dao.EntryDataFetcher;
import com.ngdesk.graphql.modules.data.dao.FormulaDataFetcher;
import com.ngdesk.graphql.modules.data.dao.IndividualEntryFetcher;
import com.ngdesk.graphql.modules.data.dao.ListFormulaDataFetcher;
import com.ngdesk.graphql.modules.data.dao.OneToManyCountDataFetcher;
import com.ngdesk.graphql.modules.data.dao.OneToManyDataFetcher;
import com.ngdesk.graphql.modules.data.dao.OneToManyUnmappedCountDataFetcher;
import com.ngdesk.graphql.modules.data.dao.OneToManyUnmappedDataFetcher;
import com.ngdesk.graphql.modules.data.dao.PicklistMultiselectDataFetcher;
import com.ngdesk.graphql.modules.data.dao.RelationshipDataFetcher;
import com.ngdesk.graphql.modules.data.dao.RelationshipEntryDataFetcher;
import com.ngdesk.graphql.modules.data.dao.RelationshipOneToManyDataFetcher;
import com.ngdesk.graphql.normalizationrules.dao.NormalizationRuleDataFetcher;
import com.ngdesk.graphql.normalizationrules.dao.NormalizationRulesCountFetcher;
import com.ngdesk.graphql.normalizationrules.dao.NormalizationRulesDataFetcher;
import com.ngdesk.graphql.normalizationrules.dao.UnapprovedNormalizationRulesDataFetcher;
import com.ngdesk.graphql.notification.dao.NotificationDataFetcher;
import com.ngdesk.graphql.notification.dao.NotificationsDataFetcher;
import com.ngdesk.graphql.notification.dao.UnReadNotificationsDataFetcher;
import com.ngdesk.graphql.notification.dao.UnReadNotificationsDataFetcherCount;
import com.ngdesk.graphql.reports.dao.ColumnShowDataFetcher;
import com.ngdesk.graphql.reports.dao.ReportCountDataFetcher;
import com.ngdesk.graphql.reports.dao.ReportCountModuleDataFetcher;
import com.ngdesk.graphql.reports.dao.ReportDataFetcher;
import com.ngdesk.graphql.reports.dao.ReportGenerateDataFetcher;
import com.ngdesk.graphql.reports.dao.ReportModuleDataFetcher;
import com.ngdesk.graphql.reports.dao.ReportsDataFetcher;
import com.ngdesk.graphql.role.dao.FieldPermissionDataFetcher;
import com.ngdesk.graphql.role.dao.RoleDataFetcher;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutCountFetcher;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutDataFetcher;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutModuleDataFetcher;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutValuesCountFetcher;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutValuesDataFetcher;
import com.ngdesk.graphql.role.layout.dao.RoleLayoutsDataFetcher;
import com.ngdesk.graphql.sam.file.rule.dao.AllSamFileRulesDataFetcher;
import com.ngdesk.graphql.sam.file.rule.dao.SamFileRuleCountFetcher;
import com.ngdesk.graphql.sam.file.rule.dao.SamFileRuleDataFetcher;
import com.ngdesk.graphql.schedules.dao.ScheduleDataFetcher;
import com.ngdesk.graphql.schedules.dao.SchedulesCountFetcher;
import com.ngdesk.graphql.schedules.dao.SchedulesDataFetcher;
import com.ngdesk.graphql.signaturedocument.dao.SignatureDocumentCountFetcher;
import com.ngdesk.graphql.signaturedocument.dao.SignatureDocumentDataFetcher;
import com.ngdesk.graphql.signaturedocument.dao.SignatureDocumentsDataFetcher;
import com.ngdesk.graphql.slas.dao.SLADataFetcher;
import com.ngdesk.graphql.slas.dao.SLAsCountFetcher;
import com.ngdesk.graphql.slas.dao.SLAsDataFetcher;
import com.ngdesk.graphql.task.dao.TaskDataFetcher;
import com.ngdesk.graphql.task.dao.TasksCountFetcher;
import com.ngdesk.graphql.task.dao.TasksDataFetcher;
import com.ngdesk.graphql.userplugin.dao.UserPluginDataFetcher;
import com.ngdesk.graphql.userplugin.dao.UserPluginsByStatusDataFetcher;
import com.ngdesk.graphql.workflow.NodeDataFetcher;
import com.ngdesk.graphql.workflow.NodesExecutedDataFetcher;
import com.ngdesk.graphql.workflow.StageDataFetcher;
import com.ngdesk.graphql.workflow.WorkflowEntriesDataFetcher;
import com.ngdesk.graphql.workflow.WorkflowEntryDataFetcher;
import com.ngdesk.graphql.workflow.WorkflowInstanceDataFetcher;
import com.ngdesk.graphql.workflow.WorkflowInstanceFetcher;
import com.ngdesk.graphql.workflow.WorkflowInstancesDataFetcher;
import com.ngdesk.repositories.escalation.EscalationsCountDataFetcher;
import com.ngdesk.repositories.escalation.EscalationsDataFetcher;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.micrometer.core.instrument.util.IOUtils;

@Component
public class DataUtility {

	@Value("classpath:company-schema.graphqls")
	private Resource modulesSchemaResource;

	@Value("classpath:schedules-schema.graphqls")
	private Resource scheduleSchemaResource;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AllEntriesFetcher allEntriesFetcher;

	@Autowired
	EntryDataFetcher entryDataFetcher;

	@Autowired
	ModulesService modulesService;

	@Autowired
	CountDataFetcher countDataFetcher;

	@Autowired
	DateTime dateTime;

	@Autowired
	RelationshipDataFetcher relationshipDataFetcher;

	@Autowired
	SchedulesDataFetcher schedulesDataFetcher;

	@Autowired
	ScheduleDataFetcher scheduleDataFetcher;

	@Autowired
	SchedulesCountFetcher schedulesCountFetcher;

	@Autowired
	TaskDataFetcher taskDataFetcher;

	@Autowired
	TasksDataFetcher tasksDataFetcher;

	@Autowired
	CatalogueDataFetcher catalogueDataFetcher;

	@Autowired
	CataloguesDataFetcher cataloguesDataFetcher;

	@Autowired
	CatalogueCountDataFetcher catalogueCountDataFetcher;

	@Autowired
	CatalogueFormDataFetcher calalogueFormDataFetcher;

	@Autowired
	TasksCountFetcher tasksCountFetcher;

	@Autowired
	NormalizationRuleDataFetcher normalizationRuleDataFetcher;

	@Autowired
	NormalizationRulesDataFetcher normalizationRulesDataFetcher;

	@Autowired
	NormalizationRulesCountFetcher normlizationRulesCountFetcher;

	@Autowired
	UnapprovedNormalizationRulesDataFetcher unapprovedNormalizationRulesDataFetcher;

	@Autowired
	DiscoveryMapDataFetcher discoveryMapDataFetcher;

	@Autowired
	DiscoveryMapsDataFetcher discoveryMapsDataFetcher;

	@Autowired
	UnApprovedDiscoveryMapsDataFetcher unApprovedDiscoveryMapsDataFetcher;

	@Autowired
	SamFileRuleDataFetcher samFileRuleDataFetcher;

	@Autowired
	AllSamFileRulesDataFetcher allSamFileRulesDataFetcher;

	@Autowired
	SamFileRuleCountFetcher samFileRuleCountFetcher;

	@Autowired
	DashboardDataFetcher dashboardDataFetcher;

	@Autowired
	DashboardsDataFetcher dashboardsDataFetcher;

	@Autowired
	DistinctEntryValuesFetcher distinctEntryValuesFetcher;

	@Autowired
	ModulesDataFetcher modulesDataFetcher;

	@Autowired
	ModuleDataFetcher moduleDataFetcher;

	@Autowired
	FieldDataFetcher fieldDataFetcher;

	@Autowired
	RoleDataFetcher roleDataFetcher;

	@Autowired
	FieldsDataFetcher fieldsDataFetcher;

	@Autowired
	DashboardCountFetcher dashboardCountFetcher;

	@Autowired
	ScorecardValueFetcher scorecardValueFetcher;

	@Autowired
	BarchartValueFetcher barchartValueFetcher;

	@Autowired
	PieChartValueFetcher pieChartValueFetcher;

	@Autowired
	MultiScorecardValueFetcher multiScorecardValueFetcher;

	@Autowired
	AdvancedPieChartValueFetcher advancedPieChartValueFetcher;

	@Autowired
	EnterpriseSearchDataFetcher enterpriseSearchDataFetcher;

	@Autowired
	EnterpriseSearchesDataFetcher enterpriseSearchesDataFetcher;

	@Autowired
	EnterpriseSearchCountFetcher enterpriseSearchCountFetcher;

	@Autowired
	CurrenciesDataFetcher currenciesDataFetcher;

	@Autowired
	CurrencyDataFetcher currencyDataFetcher;

	@Autowired
	UnapprovedEnterpriseSearchDataFetcher unapprovedEnterpriseSearchDataFetcher;

	@Autowired
	RoleLayoutDataFetcher roleLayoutDataFetcher;

	@Autowired
	RoleLayoutsDataFetcher roleLayoutsDataFetcher;

	@Autowired
	RoleLayoutValuesDataFetcher roleLayoutValuesDataFetcher;

	@Autowired
	RoleLayoutModuleDataFetcher roleLayoutModuleDataFetcher;

	@Autowired
	ChronometerDataFetcher chronometerDataFetcher;

	@Autowired
	PicklistMultiselectDataFetcher picklistMultiselectDataFetcher;

	@Autowired
	FormulaDataFetcher formulaDataFetcher;

	@Autowired
	AggregationDataFetcher aggregationDataFetcher;

	@Autowired
	WorkflowInstancesDataFetcher workflowInstancesDataFetcher;

	@Autowired
	WorkflowInstanceDataFetcher workflowInstanceDataFetcher;

	@Autowired
	WorkflowEntryDataFetcher workflowEntryDataFetcher;

	@Autowired
	NodeDataFetcher nodeDataFetcher;

	@Autowired
	StageDataFetcher stageDataFetcher;

	@Autowired
	NodesExecutedDataFetcher nodesExecutedDataFetcher;

	@Autowired
	RelationshipEntryDataFetcher relationshipEntryDataFetcher;

	@Autowired
	WorkflowInstanceFetcher workflowInstanceFetcher;

	@Autowired
	IndividualEntryFetcher individualEntryFetcher;

	@Autowired
	RoleLayoutCountFetcher roleLayoutCountFetcher;

	@Autowired
	SignatureDocumentsDataFetcher signatureDocumentsDataFetcher;

	@Autowired
	SignatureDocumentDataFetcher signatureDocumentDataFetcher;

	@Autowired
	NotificationDataFetcher notificationDataFetcher;

	@Autowired
	NotificationsDataFetcher notificationsDataFetcher;

	@Autowired
	UnReadNotificationsDataFetcher unreadNotificationsDataFetcher;

	@Autowired
	UnReadNotificationsDataFetcherCount unReadNotificationsDataFetcherCount;

	@Autowired
	SignatureDocumentCountFetcher signatureDocumentCountFetcher;

	@Autowired
	OneToManyDataFetcher oneToManyDataFetcher;

	@Autowired
	OneToManyCountDataFetcher oneToManyCountDataFetcher;

	@Autowired
	ChannelDataFetcher channelDataFetcher;

	@Autowired
	WidgetEntriesDataFetcher widgetEntriesDataFetcher;

	@Autowired
	WidgetEntriesCountFetcher widgetEntriesCountFetcher;

	@Autowired
	UserPluginDataFetcher userPluginDataFetcher;

	@Autowired
	UserPluginsByStatusDataFetcher userPluginsByStatusDataFetcher;

	@Autowired
	ApprovalOngoingDataFetcher approvalOngoingDataFetcher;

	@Autowired
	ApprovalDeniedDataFetcher approvalDeniedDataFetcher;

	@Autowired
	ReportDataFetcher reportDataFetcher;

	@Autowired
	ReportsDataFetcher reportsDataFetcher;

	@Autowired
	ReportCountDataFetcher reportCountDataFetcher;

	@Autowired
	ColumnShowDataFetcher columnShowDataFetcher;

	@Autowired
	RoleLayoutValuesCountFetcher roleLayoutValuesCountFetcher;

	@Autowired
	FormsDataFetcher formsDataFetcher;

	@Autowired
	FormDataFetcher formDataFetcher;

	@Autowired
	FormCountDataFetcher formCountDataFetcher;

	@Autowired
	FieldPermissionDataFetcher fieldPermissionDataFetcher;

	@Autowired
	OneToManyUnmappedDataFetcher oneToManyUnmappedDataFetcher;

	@Autowired
	OneToManyUnmappedCountDataFetcher oneToManyUnmappedCountDataFetcher;

	@Autowired
	SLADataFetcher slaDataFetcher;

	@Autowired
	CompanyDataFetcher companyDataFetcher;

	@Autowired
	RelationshipOneToManyDataFetcher relationshipOneToManyDataFetcher;

	@Autowired
	ReportModuleDataFetcher reportModuleDataFetcher;

	@Autowired
	SLAsDataFetcher slasDataFetcher;

	@Autowired
	SLAsCountFetcher slasCountFetcher;

	@Autowired
	ReportGenerateDataFetcher reportGenerateDataFetcher;

	@Autowired
	ReportCountModuleDataFetcher reportCountModuleDataFetcher;

	@Autowired
	ChatChannelDataFetcher chatChannelDataFetcher;

	@Autowired
	ChatChannelsDataFetcher chatChannelsDataFetcher;

	@Autowired
	ChatPromptDataFetcher chatPromptDataFetcher;

	@Autowired
	ChatPromptsDataFetcher chatPromptsDataFetcher;

	@Autowired

	ArticleDataFetcher articleDataFetcher;

	@Autowired
	ArticlesDataFetcher articlesDataFetcher;

	@Autowired
	ArticlesCountDataFetcher articlesCountDataFetcher;

	@Autowired
	SectionDataFetcher sectionDataFetcher;

	@Autowired
	SectionsDataFetcher sectionsDataFetcher;

	@Autowired
	SectionsCountFetcher sectionsCountFetcher;

	@Autowired
	WorkflowEntriesDataFetcher workflowEntriesDataFetcher;

	@Autowired
	SectionCategoryDataFetcher sectionCategoryDataFetcher;

	@Autowired
	CategoryDataFetcher categoryDataFetcher;

	@Autowired
	CategoriesDataFetcher categoriesDataFetcher;

	@Autowired
	CategoriesCountDataFetcher categoriesCountDataFetcher;

	@Autowired
	ListFormulaDataFetcher listFormulaDatafetcher;
	
	@Autowired
	EscalationDataFetcher escalationDataFetcher;

	@Autowired
	EscalationsDataFetcher escalationsDataFetcher;

	@Autowired
	EscalationsCountDataFetcher escalationCountDataFetcher;

	public GraphQL createGraphQlObject(Company company) throws IOException {
		try {
			String schemaString = IOUtils.toString(modulesSchemaResource.getInputStream());
			List<Module> modules = modulesRepository.findAllModules("modules_" + company.getCompanyId());
			StringBuilder functionNamesBuilder = new StringBuilder();

			for (Module module : modules) {

				String replacedModuleName = module.getName().replaceAll("\\s+", "_");

				String functionTemplate = "get" + replacedModuleName
						+ "(moduleId: String, layoutId: String, pageNumber: Int, pageSize: Int, sortBy: String, orderBy: String, search: String, includeConditions: Boolean): ["
						+ replacedModuleName + "]";

				String functionOneToManyTemplate = "get" + replacedModuleName + "OneToMany"
						+ "(moduleId: String, fieldId: String, dataId: String, pageNumber: Int, pageSize: Int, sortBy: String, orderBy: String): ["
						+ replacedModuleName + "]";

				String functionRoleTemplate = "get" + replacedModuleName + "RoleLayout"
						+ "(layoutId: String, tabId: String, pageNumber: Int, pageSize: Int, sortBy: String, orderBy: String): ["
						+ replacedModuleName + "]";

				String functionWidgetTemplate = "get" + replacedModuleName + "WidgetEntries"
						+ "(dasboardId: String,widgetId: String, value: String, pageNumber: Int, pageSize: Int, sortBy: String, orderBy: String): ["
						+ replacedModuleName + "]";

				String functionOneToManyUnmappedTemplate = "get" + replacedModuleName + "OneToManyUnmapped"
						+ "(moduleId: String, fieldId: String, pageNumber: Int, pageSize: Int, search: String, sortBy: String, orderBy: String): ["
						+ replacedModuleName + "]";

				String functionReportDataTemplate = "getReportsFor" + replacedModuleName
						+ "(moduleId: String, pageNumber: Int, pageSize: Int, sortBy: String, orderBy: String): ["
						+ replacedModuleName + "]";

				String functionReportCsvTemplate = "getCsvFor" + replacedModuleName + "(moduleId: String): ["
						+ replacedModuleName + "]";

				functionNamesBuilder.append(functionWidgetTemplate);
				functionNamesBuilder.append(functionOneToManyTemplate);
				functionNamesBuilder.append(functionOneToManyUnmappedTemplate);
				functionNamesBuilder.append(functionRoleTemplate);
				functionNamesBuilder.append(functionTemplate);
				functionNamesBuilder.append(functionReportDataTemplate);
				functionNamesBuilder.append(functionReportCsvTemplate);

				functionNamesBuilder.append("\n");

				String functionTemplateIndividual = "get" + replacedModuleName + "Entry" + "(id: String): "
						+ replacedModuleName;

				functionNamesBuilder.append(functionTemplateIndividual);
				functionNamesBuilder.append("\n");

				List<ModuleField> fields = modulesService.getAllFields(module, modules);
				Map<String, String> schemaMap = new HashMap<String, String>();
				for (ModuleField field : fields) {

					if (field.getName().equals("CHANNEL")) {
						schemaMap.put(field.getName(), "Channel");
					} else {
						if (field.getDataType().getDisplay().equals("Relationship")) {
							Optional<Module> optionalRelatedModule = modules.stream()
									.filter(mod -> mod.getModuleId().equals(field.getModule())).findFirst();
							if (optionalRelatedModule.isEmpty()) {
								schemaMap.put(field.getName(), "String");
							} else {
								Module relatedModule = optionalRelatedModule.get();

								switch (field.getRelationshipType()) {
								case "Many to Many":
									schemaMap.put(field.getName() + " (pageNumber: Int, pageSize: Int)",
											"[" + relatedModule.getName().replaceAll("\\s+", "_") + "]");
									break;
								case "One to One":
								case "Many to One":
									schemaMap.put(field.getName(), relatedModule.getName().replaceAll("\\s+", "_"));
									break;
								case "One to Many":
									schemaMap.put(field.getName()
											+ " (pageNumber: Int, pageSize: Int, sortBy: String, orderBy: String, moduleId: String)",
											"[" + relatedModule.getName().replaceAll("\\s+", "_") + "]");
									break;

								default:
									break;
								}
							}
						} else if (field.getDataType().getDisplay().equals("Discussion")) {

							schemaMap.put(field.getName(), "[Discussion]");

						} else if (field.getDataType().getDisplay().equals("Phone")) {

							schemaMap.put(field.getName(), "Phone");

						} else if (field.getDataType().getDisplay().equals("List Text")) {

							schemaMap.put(field.getName(), "[String]");

						} else if (field.getDataType().getDisplay().equals("Picklist")) {

							schemaMap.put(field.getName(), "String");

						} else if (field.getDataType().getDisplay().equals("Picklist (Multi-Select)")) {

							schemaMap.put(field.getName(), "[String]");

						} else if (field.getDataType().getDisplay().equals("Chronometer")) {

							schemaMap.put(field.getName(), "String");

						} else if (field.getDataType().getDisplay().equals("Approval")) {

							schemaMap.put(field.getName(), "Approval");

						} else if (field.getDataType().getDisplay().equals("Workflow Stages")) {

							schemaMap.put(field.getName(), "WorkflowInstance");

						} else if (field.getDataType().getDisplay().equals("Formula")) {
							if (field.getDataType().getBackend().equals("String")) {
								schemaMap.put(field.getName(), "String");
							} else {
								schemaMap.put(field.getName(), "Float");
							}

						} else if (field.getDataType().getDisplay().equals("Password")) {

							schemaMap.put(field.getName(), "String");

						} else if (field.getDataType().getDisplay().equals("Aggregate")) {

							schemaMap.put(field.getName() + " (moduleId: String)", "Float");

						} else if (field.getDataType().getDisplay().equals("File Upload")) {

							schemaMap.put(field.getName(), "[MessageAttachment]");

						} else if (field.getDataType().getDisplay().equals("Currency Exchange")) {
							schemaMap.put(field.getName(), "Float");

						} else if (field.getDataType().getDisplay().equals("Receipt Capture")) {
							schemaMap.put(field.getName(), "MessageAttachment");

						} else if (field.getDataType().getDisplay().equals("List Formula")) {

							schemaMap.put(field.getName(), "[ListFormulaFieldValue]");

						} else {
							switch (field.getDataType().getBackend()) {
							case "String":
								schemaMap.put(field.getName(), "String");
								break;
							case "Double":
							case "Float":
								schemaMap.put(field.getName(), "Float");
								break;
							case "Integer":
								schemaMap.put(field.getName(), "Int");
								break;
							case "Boolean":
								schemaMap.put(field.getName(), "Boolean");
								break;
							case "Timestamp":
							case "Date":
								schemaMap.put(field.getName(), "DateTime");
								break;
							case "BLOB":
								schemaMap.put(field.getName(), "[MessageAttachment]");
							default:
								break;
							}
						}
					}
				}
				schemaMap.put("META_DATA", "MetaData");
				schemaMap.put("_id", "String");
				try {
					ObjectMapper mapper = new ObjectMapper();
					String moduleSchema = mapper.writeValueAsString(schemaMap);
					schemaString += "\n\n" + "type " + module.getName().replaceAll("\\s+", "_") + " " + moduleSchema;

					schemaString = schemaString.replaceAll("\"", "");
					schemaString = schemaString.replaceAll(",", "\n");

				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

			}
			schemaString = schemaString.replaceAll("MODULE_FUNCTIONS_REPLACE", functionNamesBuilder.toString());
			TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaString);
			RuntimeWiring wiring = buildRuntimeWiring(modules);
			GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
			return newGraphQL(schema).build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public RuntimeWiring buildRuntimeWiring(List<Module> modules) {
		Builder builder = RuntimeWiring.newRuntimeWiring();
		builder.scalar(dateTime.dateScalar);
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("count", countDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getCountForReportsData", reportCountModuleDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getCsvForReports", reportGenerateDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getCompanyDetails", companyDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("countSchedules", schedulesCountFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSchedules", schedulesDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSchedule", scheduleDataFetcher));

		builder.type("Layer", typeWiring -> typeWiring.dataFetcher("users", relationshipDataFetcher));
		builder.type("Schedule", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Schedule", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getNormalizationRulesCount", normlizationRulesCountFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getNormalizationRule", normalizationRuleDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getNormalizationRules", normalizationRulesDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getUnapprovedNormalizationRules",
				unapprovedNormalizationRulesDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getDiscoveryMap", discoveryMapDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getDiscoveryMaps", discoveryMapsDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getUnApprovedDiscoveryMaps", unApprovedDiscoveryMapsDataFetcher));
		builder.type("DiscoveryMap", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("DiscoveryMap", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSamFileRule", samFileRuleDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSamFileRules", allSamFileRulesDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSamFileRulesCount", samFileRuleCountFetcher));
		builder.type("SamFileRule", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("SamFileRule", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getDashboard", dashboardDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getDashboards", dashboardsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getDashboardsCount", dashboardCountFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getScoreCardValue", scorecardValueFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getMultiScoreCardValue", multiScorecardValueFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getBarChartValue", barchartValueFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getPieChartValue", pieChartValueFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getAdvancePieChartValue", advancedPieChartValueFetcher));

		builder.type("Dashboard", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Dashboard", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("Dashboard", typeWiring -> typeWiring.dataFetcher("role", roleDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getDistinctValues", distinctEntryValuesFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getEnterpriseSearch", enterpriseSearchDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getEnterpriseSearches", enterpriseSearchesDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getEnterpriseSearchCount", enterpriseSearchCountFetcher));
		builder.type("EnterpriseSearch", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("EnterpriseSearch", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getUnapprovedEnterpriseSearches",
				unapprovedEnterpriseSearchDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getCurrency", currencyDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getAllCurrencies", currenciesDataFetcher));
		builder.type("Currency", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Currency", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getRoleLayout", roleLayoutDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getRoleLayouts", roleLayoutsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getRoleLayoutData", roleLayoutValuesDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getRoleLayoutValuesCount", roleLayoutValuesCountFetcher));

		builder.type("RoleLayout", typeWiring -> typeWiring.dataFetcher("role", roleDataFetcher));
		builder.type("Tab", typeWiring -> typeWiring.dataFetcher("module", roleLayoutModuleDataFetcher));
		builder.type("Tab", typeWiring -> typeWiring.dataFetcher("columnsShow", columnShowDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getTask", taskDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getTasks", tasksDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getTasksCount", tasksCountFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getCatalogue", catalogueDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getCatalogues", cataloguesDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getCatalogueCount", catalogueCountDataFetcher));
		builder.type("CatalogueForm", typeWiring -> typeWiring.dataFetcher("formId", calalogueFormDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getRoleLayoutCount", roleLayoutCountFetcher));

		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getSignatureDocument", signatureDocumentDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getSignatureDocuments", signatureDocumentsDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getSignatureDocumentsCount", signatureDocumentCountFetcher));

		// Sections
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbSection", sectionDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbSections", sectionsDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbSectionsCount", sectionsCountFetcher));

		builder.type("Section", typeWiring -> typeWiring.dataFetcher("visibleTo", relationshipDataFetcher));

		builder.type("Section", typeWiring -> typeWiring.dataFetcher("managedBy", relationshipDataFetcher));

		builder.type("Section", typeWiring -> typeWiring.dataFetcher("category", sectionCategoryDataFetcher));
		builder.type("Section", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Section", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		// ARTICLE
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbArticle", articleDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getAllKbArticles", articlesDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getAllKbArticlesCount", articlesCountDataFetcher));
		builder.type("Article", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Article", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("Article", typeWiring -> typeWiring.dataFetcher("author", entryDataFetcher));

		// Notification
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getNotification", notificationDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getNotifications", notificationsDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getUnreadNotifications", unreadNotificationsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getUnReadNotificationsDataFetcherCount",
				unReadNotificationsDataFetcherCount));

		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getOneToManyCountDataFetcher", oneToManyCountDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getOneToManyUnmappedCountDataFetcher",
				oneToManyUnmappedCountDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getUserPlugin", userPluginDataFetcher));
		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getUserPluginsByStatus", userPluginsByStatusDataFetcher));
		builder.type("UserPlugin", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("UserPlugin", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		builder.type("Query",
				typeWiring -> typeWiring.dataFetcher("getApprovalOngoingData", approvalOngoingDataFetcher));
		builder.type("DeniedBy", typeWiring -> typeWiring.dataFetcher("deniedUser", entryDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getApprovalDeniedData", approvalDeniedDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getReports", reportsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getReport", reportDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getReportsCount", reportCountDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getFieldPermissions", fieldPermissionDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getForm", formDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getForms", formsDataFetcher));
		builder.type("Form", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Form", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getFormsCount", formCountDataFetcher));

		// Categories
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbCategory", categoryDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbCategories", categoriesDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getKbCountCategories", categoriesCountDataFetcher));
		builder.type("Category", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Category", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("Category", typeWiring -> typeWiring.dataFetcher("visibleTo", relationshipDataFetcher));

		// Slas
		builder.type("SLA", typeWiring -> typeWiring.dataFetcher("workflow", workflowEntryDataFetcher));
		builder.type("SLA", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("SLA", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSla", slaDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSlas", slasDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getSlasCount", slasCountFetcher));

		builder.type("Form", typeWiring -> typeWiring.dataFetcher("workflow", workflowEntryDataFetcher));

		// MODULES
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getModules", modulesDataFetcher));
		builder.type("Module", typeWiring -> typeWiring.dataFetcher("parentModule", moduleDataFetcher));
		builder.type("Module", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Module", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));

		// MODULE FIELDS
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("primaryDisplayField", fieldDataFetcher));
//		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("module", moduleDataFetcher));
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("relationshipField", fieldDataFetcher));
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("aggregationRelatedField", fieldDataFetcher));
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("toCurrency", fieldDataFetcher));
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("fromCurrency", fieldDataFetcher));
		builder.type("ModuleField", typeWiring -> typeWiring.dataFetcher("dateIncurred", fieldDataFetcher));

		// MOBILE LIST LAYOUTS
		builder.type("ListMobileLayout", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("ListMobileLayout", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("ListMobileLayout", typeWiring -> typeWiring.dataFetcher("role", roleDataFetcher));
		builder.type("ListMobileLayout", typeWiring -> typeWiring.dataFetcher("fields", fieldsDataFetcher));

		builder.type("OrderBy", typeWiring -> typeWiring.dataFetcher("column", fieldDataFetcher));

		// MOBILE CREATE AND EDIT LAYOUTS
		builder.type("CreateEditMobileLayout", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("CreateEditMobileLayout", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("CreateEditMobileLayout", typeWiring -> typeWiring.dataFetcher("role", roleDataFetcher));
		builder.type("CreateEditMobileLayout", typeWiring -> typeWiring.dataFetcher("fields", fieldsDataFetcher));

		builder.type("ModuleCondition", typeWiring -> typeWiring.dataFetcher("condition", fieldDataFetcher));

		builder.type("Sender", typeWiring -> typeWiring.dataFetcher("ROLE", roleDataFetcher));

		// WORKFLOW
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getWorkflow", workflowEntryDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getWorkflows", workflowEntriesDataFetcher));
		builder.type("Workflow", typeWiring -> typeWiring.dataFetcher("module", roleLayoutModuleDataFetcher));
		builder.type("Workflow", typeWiring -> typeWiring.dataFetcher("createdBy", entryDataFetcher));
		builder.type("Workflow", typeWiring -> typeWiring.dataFetcher("lastUpdated", entryDataFetcher));

		// WORKFLOW INSTANCE
		builder.type("WorkflowInstance", typeWiring -> typeWiring.dataFetcher("module", moduleDataFetcher));
		builder.type("WorkflowInstance", typeWiring -> typeWiring.dataFetcher("workflow", workflowEntryDataFetcher));
		builder.type("WorkflowInstance", typeWiring -> typeWiring.dataFetcher("stage", stageDataFetcher));
		builder.type("WorkflowInstance", typeWiring -> typeWiring.dataFetcher("node", nodeDataFetcher));
		builder.type("WorkflowInstance", typeWiring -> typeWiring.dataFetcher("workflowKickedOffBy", entryDataFetcher));
		builder.type("WorkflowInstance",
				typeWiring -> typeWiring.dataFetcher("nodesExecuted", nodesExecutedDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getWorkflowInstancesByWorkflowIdAndDataId",
				workflowInstanceFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getWorkflowInstance", workflowInstanceDataFetcher));

		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getWidgetEntriesCount", widgetEntriesCountFetcher));
		builder.type("Form", typeWiring -> typeWiring.dataFetcher("visibleTo", relationshipDataFetcher));
		builder.type("Catalogue", typeWiring -> typeWiring.dataFetcher("visibleTo", relationshipDataFetcher));

		// CHAT CHANNEL
		builder.type("ChatChannel", typeWiring -> typeWiring.dataFetcher("lastUpdated", entryDataFetcher));
		builder.type("ChatChannel", typeWiring -> typeWiring.dataFetcher("module", moduleDataFetcher));
		builder.type("ChatPrompt", typeWiring -> typeWiring.dataFetcher("lastUpdatedBy", entryDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getChatChannels", chatChannelsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getChatChannel", chatChannelDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getChatPrompts", chatPromptsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getChatPrompt", chatPromptDataFetcher));

		// ESCALATION
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getEscalation", escalationDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getEscalations", escalationsDataFetcher));
		builder.type("Query", typeWiring -> typeWiring.dataFetcher("getEscalationsCount", escalationCountDataFetcher));

		for (Module module : modules) {
			String name = "get" + module.getName().replaceAll("\\s+", "_");
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(name, allEntriesFetcher));

			String roleName = name + "RoleLayout";
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(roleName, roleLayoutValuesDataFetcher));

			String relationshpOneToManyName = name + "OneToMany";
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(relationshpOneToManyName, oneToManyDataFetcher));

			String relationshpUnmappedOneToManyName = name + "OneToManyUnmapped";
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(relationshpUnmappedOneToManyName,
					oneToManyUnmappedDataFetcher));

			String widgetEntriesName = name + "WidgetEntries";
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(widgetEntriesName, widgetEntriesDataFetcher));

			String individualEntryName = name + "Entry";
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(individualEntryName, individualEntryFetcher));

			String reportDataName = "getReportsFor" + module.getName().replaceAll("\\s+", "_");
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(reportDataName, reportModuleDataFetcher));

			String csvName = "getCsvFor" + module.getName().replaceAll("\\s+", "_");
			builder.type("Query", typeWiring -> typeWiring.dataFetcher(csvName, reportGenerateDataFetcher));

			List<ModuleField> fields = module.getFields();
			fields.forEach(field -> {

				if (field.getName().equals("CHANNEL")) {
					builder.type(module.getName().replaceAll("\\s+", "_"),
							typeWiring -> typeWiring.dataFetcher(field.getName(), channelDataFetcher));
				} else {
					if (field.getDataType().getDisplay().equals("Relationship")) {
						if (field.getRelationshipType().equals("Many to Many")) {
							builder.type(module.getName().replaceAll("\\s+", "_"),
									typeWiring -> typeWiring.dataFetcher(field.getName(), relationshipDataFetcher));
						} else if (field.getRelationshipType().equals("Many to One")
								|| field.getRelationshipType().equals("One to One")) {
							builder.type(module.getName().replaceAll("\\s+", "_"), typeWiring -> typeWiring
									.dataFetcher(field.getName(), relationshipEntryDataFetcher));
						} else if (field.getRelationshipType().equals("One to Many")) {
							builder.type(module.getName().replaceAll("\\s+", "_"), typeWiring -> typeWiring
									.dataFetcher(field.getName(), relationshipOneToManyDataFetcher));
						}
					} else if (field.getDataType().getDisplay().equals("Chronometer")) {
						builder.type(module.getName().replaceAll("\\s+", "_"),
								typeWiring -> typeWiring.dataFetcher(field.getName(), chronometerDataFetcher));
					} else if (field.getDataType().getDisplay().equals("Picklist (Multi-Select)")) {
						builder.type(module.getName().replaceAll("\\s+", "_"),
								typeWiring -> typeWiring.dataFetcher(field.getName(), picklistMultiselectDataFetcher));
					} else if (field.getDataType().getDisplay().equals("Formula")) {
						builder.type(module.getName().replaceAll("\\s+", "_"),
								typeWiring -> typeWiring.dataFetcher(field.getName(), formulaDataFetcher));
					} else if (field.getDataType().getDisplay().equals("Aggregate")) {
						builder.type(module.getName().replaceAll("\\s+", "_"),
								typeWiring -> typeWiring.dataFetcher(field.getName(), aggregationDataFetcher));
					} else if (field.getDataType().getDisplay().equals("Workflow Stages")) {
						builder.type(module.getName().replaceAll("\\s+", "_"),
								typeWiring -> typeWiring.dataFetcher(field.getName(), workflowInstancesDataFetcher));
					} else if (field.getDataType().getDisplay().equals("List Formula")) {
						builder.type(module.getName().replaceAll("\\s+", "_"),
								typeWiring -> typeWiring.dataFetcher(field.getName(), listFormulaDatafetcher));
					}
				}

			});

		}

		return builder.build();
	}

}
