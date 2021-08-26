package com.ngdesk.modules;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.channels.chatbots.Chatbot;
import com.ngdesk.modules.detail.layouts.DCELayout;
import com.ngdesk.modules.detail.mobile.layouts.MobileLayout;
import com.ngdesk.modules.fields.Field;
import com.ngdesk.modules.forms.Form;
import com.ngdesk.modules.list.layouts.ListLayout;
import com.ngdesk.modules.list.mobile.layouts.ListMobileLayout;
import com.ngdesk.modules.monitors.ModuleMonitor;
import com.ngdesk.modules.rules.Rule;
import com.ngdesk.modules.settings.ModuleSettings;
import com.ngdesk.modules.slas.Sla;
import com.ngdesk.modules.validations.ModuleValidation;
import com.ngdesk.modules.workflows.ModuleWorkflow;

@JsonIgnoreProperties("_class")
public class Module {

	@JsonProperty("NAME")
	@NotNull(message = "MODULE_NAME_NOT_NULL")
	@Size(min = 1, message = "MODULE_NAME_NOT_EMPTY")
	@Size(max = 70, message = "MODULE_NAME_LIMIT_REACHED")
	@Pattern(regexp = "([A-Za-z0-9\\s-]+)", message = "INVALID_MODULE_NAME")
	private String name;

	@JsonProperty("PARENT_MODULE")
	private String parentModule;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("FIELDS")
	@Valid
	private List<Field> fields = new ArrayList<Field>();

	@JsonProperty("LIST_LAYOUTS")
	@Valid
	private List<ListLayout> listlayout = new ArrayList<ListLayout>();

	@JsonProperty("DETAIL_LAYOUTS")
	@Valid
	private List<DCELayout> detaillayout = new ArrayList<DCELayout>();

	@JsonProperty("LIST_MOBILE_LAYOUTS")
	@Valid
	private List<ListMobileLayout> listMobileLayouts = new ArrayList<ListMobileLayout>();

	@JsonProperty("DETAIL_MOBILE_LAYOUTS")
	@Valid
	private List<MobileLayout> detailMobilelayout = new ArrayList<MobileLayout>();

	@JsonProperty("CREATE_MOBILE_LAYOUTS")
	@Valid
	private List<MobileLayout> createMobilelayout = new ArrayList<MobileLayout>();

	@JsonProperty("EDIT_MOBILE_LAYOUTS")
	@Valid
	private List<MobileLayout> editMobilelayout = new ArrayList<MobileLayout>();

	@JsonProperty("CREATE_LAYOUTS")
	@Valid
	private List<DCELayout> createlayout = new ArrayList<DCELayout>();

	@JsonProperty("EDIT_LAYOUTS")
	@Valid
	private List<DCELayout> editlayout = new ArrayList<DCELayout>();

	@JsonProperty("FIELD_RULES")
	@Valid
	private List<Rule> rules = new ArrayList<Rule>();

	@JsonProperty("MONITORS")
	@Valid
	private List<ModuleMonitor> monitors = new ArrayList<ModuleMonitor>();

	@JsonProperty("VALIDATIONS")
	@Valid
	private List<ModuleValidation> validations = new ArrayList<ModuleValidation>();

	@JsonProperty("WORKFLOWS")
	@Valid
	private List<ModuleWorkflow> workflows = new ArrayList<ModuleWorkflow>();

	@JsonProperty("SINGULAR_NAME")
	@NotEmpty(message = "SINGULAR_NAME_REQUIRED")
	@Pattern(regexp = "([A-Za-z0-9\\s-]+)", message = "INVALID_SINGULAR_NAME")
	private String singular;

	@JsonProperty("PLURAL_NAME")
	@NotEmpty(message = "PLURAL_NAME_REQUIRED")
	@Pattern(regexp = "([A-Za-z0-9\\s-]+)", message = "INVALID_PLURAL_NAME")
	private String plural;

	@JsonProperty("SLAS")
	private List<Sla> slas = new ArrayList<Sla>();

	@JsonProperty("SETTINGS")
	@Valid
	private ModuleSettings settings;

	@JsonProperty("CHAT_BOTS")
	@Valid
	private List<Chatbot> chatBots;

	@JsonProperty("FORMS")
	private List<Form> form;

	@JsonProperty("ALTERNATE_PRIMARY_KEYS")
	private List<String> alternatePrimaryKeys;

	public Module() {

	}

	public Module(
			@NotNull(message = "MODULE_NAME_NOT_NULL") @Size(min = 1, message = "MODULE_NAME_NOT_EMPTY") @Size(max = 70, message = "MODULE_NAME_LIMIT_REACHED") @Pattern(regexp = "([A-Za-z0-9\\s-]+)", message = "INVALID_MODULE_NAME") String name,
			String parentModule, @NotNull(message = "DESCRIPTION_NOT_NULL") String description, String moduleId,
			Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy, @Valid List<Field> fields,
			@Valid List<ListLayout> listlayout, @Valid List<DCELayout> detaillayout,
			@Valid List<ListMobileLayout> listMobileLayouts, @Valid List<MobileLayout> detailMobilelayout,
			@Valid List<MobileLayout> createMobilelayout, @Valid List<MobileLayout> editMobilelayout,
			@Valid List<DCELayout> createlayout, @Valid List<DCELayout> editlayout, @Valid List<Rule> rules,
			@Valid List<ModuleMonitor> monitors, @Valid List<ModuleValidation> validations,
			@Valid List<ModuleWorkflow> workflows,
			@NotEmpty(message = "SINGULAR_NAME_REQUIRED") @Pattern(regexp = "([A-Za-z0-9\\s-]+)", message = "INVALID_SINGULAR_NAME") String singular,
			@NotEmpty(message = "PLURAL_NAME_REQUIRED") @Pattern(regexp = "([A-Za-z0-9\\s-]+)", message = "INVALID_PLURAL_NAME") String plural,
			List<Sla> slas, @Valid ModuleSettings settings, @Valid List<Chatbot> chatBots, List<Form> form,
			List<String> alternatePrimaryKeys) {
		super();
		this.name = name;
		this.parentModule = parentModule;
		this.description = description;
		this.moduleId = moduleId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.fields = fields;
		this.listlayout = listlayout;
		this.detaillayout = detaillayout;
		this.listMobileLayouts = listMobileLayouts;
		this.detailMobilelayout = detailMobilelayout;
		this.createMobilelayout = createMobilelayout;
		this.editMobilelayout = editMobilelayout;
		this.createlayout = createlayout;
		this.editlayout = editlayout;
		this.rules = rules;
		this.monitors = monitors;
		this.validations = validations;
		this.workflows = workflows;
		this.singular = singular;
		this.plural = plural;
		this.slas = slas;
		this.settings = settings;
		this.chatBots = chatBots;
		this.form = form;
		this.alternatePrimaryKeys = alternatePrimaryKeys;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentModule() {
		return parentModule;
	}

	public void setParentModule(String parentModule) {
		this.parentModule = parentModule;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public List<ListLayout> getListlayout() {
		return listlayout;
	}

	public void setListlayout(List<ListLayout> listlayout) {
		this.listlayout = listlayout;
	}

	public List<DCELayout> getDetaillayout() {
		return detaillayout;
	}

	public void setDetaillayout(List<DCELayout> detaillayout) {
		this.detaillayout = detaillayout;
	}

	public List<ListMobileLayout> getListMobileLayouts() {
		return listMobileLayouts;
	}

	public void setListMobileLayouts(List<ListMobileLayout> listMobileLayouts) {
		this.listMobileLayouts = listMobileLayouts;
	}

	public List<MobileLayout> getDetailMobilelayout() {
		return detailMobilelayout;
	}

	public void setDetailMobilelayout(List<MobileLayout> detailMobilelayout) {
		this.detailMobilelayout = detailMobilelayout;
	}

	public List<MobileLayout> getCreateMobilelayout() {
		return createMobilelayout;
	}

	public void setCreateMobilelayout(List<MobileLayout> createMobilelayout) {
		this.createMobilelayout = createMobilelayout;
	}

	public List<MobileLayout> getEditMobilelayout() {
		return editMobilelayout;
	}

	public void setEditMobilelayout(List<MobileLayout> editMobilelayout) {
		this.editMobilelayout = editMobilelayout;
	}

	public List<DCELayout> getCreatelayout() {
		return createlayout;
	}

	public void setCreatelayout(List<DCELayout> createlayout) {
		this.createlayout = createlayout;
	}

	public List<DCELayout> getEditlayout() {
		return editlayout;
	}

	public void setEditlayout(List<DCELayout> editlayout) {
		this.editlayout = editlayout;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public List<ModuleMonitor> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<ModuleMonitor> monitors) {
		this.monitors = monitors;
	}

	public List<ModuleValidation> getValidations() {
		return validations;
	}

	public void setValidations(List<ModuleValidation> validations) {
		this.validations = validations;
	}

	public List<ModuleWorkflow> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<ModuleWorkflow> workflows) {
		this.workflows = workflows;
	}

	public String getSingular() {
		return singular;
	}

	public void setSingular(String singular) {
		this.singular = singular;
	}

	public String getPlural() {
		return plural;
	}

	public void setPlural(String plural) {
		this.plural = plural;
	}

	public List<Sla> getSlas() {
		return slas;
	}

	public void setSlas(List<Sla> slas) {
		this.slas = slas;
	}

	public ModuleSettings getSettings() {
		return settings;
	}

	public void setSettings(ModuleSettings settings) {
		this.settings = settings;
	}

	public List<Chatbot> getChatBots() {
		return chatBots;
	}

	public void setChatBots(List<Chatbot> chatBots) {
		this.chatBots = chatBots;
	}

	public List<Form> getForm() {
		return form;
	}

	public void setForm(List<Form> form) {
		this.form = form;
	}

	public List<String> getAlternatePrimaryKeys() {
		return alternatePrimaryKeys;
	}

	public void setAlternatePrimaryKeys(List<String> alternatePrimaryKeys) {
		this.alternatePrimaryKeys = alternatePrimaryKeys;
	}

}
