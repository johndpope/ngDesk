package com.ngdesk.graphql.modules.dao;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Module {

	@Id
	private String moduleId;

	@Field("NAME")
	private String name;

	@JsonProperty("PARENT_MODULE")
	@Field("PARENT_MODULE")
	private String parentModule;

	@Field("DESCRIPTION")
	private String description;

	@Field("SINGULAR_NAME")
	private String singularName;

	@Field("PLURAL_NAME")
	private String pluralName;

	@Field("FIELDS")
	private List<ModuleField> fields;

	@Field("LIST_LAYOUTS")
	private List<ListLayout> listLayouts;

	@Field("LIST_MOBILE_LAYOUTS")
	private List<ListMobileLayout> listMobileLayouts;

	@Field("CREATE_MOBILE_LAYOUTS")
	private List<CreateEditMobileLayout> createMobileLayouts;

	@Field("EDIT_MOBILE_LAYOUTS")
	private List<CreateEditMobileLayout> editMobileLayouts;

	public Module() {

	}

	public Module(String moduleId, String name, String parentModule, String description, String singularName,
			String pluralName, List<ModuleField> fields, List<ListLayout> listLayouts,
			List<ListMobileLayout> listMobileLayouts, List<CreateEditMobileLayout> createMobileLayouts,
			List<CreateEditMobileLayout> editMobileLayouts) {
		super();
		this.moduleId = moduleId;
		this.name = name;
		this.parentModule = parentModule;
		this.description = description;
		this.singularName = singularName;
		this.pluralName = pluralName;
		this.fields = fields;
		this.listLayouts = listLayouts;
		this.listMobileLayouts = listMobileLayouts;
		this.createMobileLayouts = createMobileLayouts;
		this.editMobileLayouts = editMobileLayouts;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
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

	public String getSingularName() {
		return singularName;
	}

	public void setSingularName(String singularName) {
		this.singularName = singularName;
	}

	public String getPluralName() {
		return pluralName;
	}

	public void setPluralName(String pluralName) {
		this.pluralName = pluralName;
	}

	public List<ModuleField> getFields() {
		return fields;
	}

	public void setFields(List<ModuleField> fields) {
		this.fields = fields;
	}

	public List<ListLayout> getListLayouts() {
		return listLayouts;
	}

	public void setListLayouts(List<ListLayout> listLayouts) {
		this.listLayouts = listLayouts;
	}

	public List<ListMobileLayout> getListMobileLayouts() {
		return listMobileLayouts;
	}

	public void setListMobileLayouts(List<ListMobileLayout> listMobileLayouts) {
		this.listMobileLayouts = listMobileLayouts;
	}

	public List<CreateEditMobileLayout> getCreateMobileLayouts() {
		return createMobileLayouts;
	}

	public void setCreateMobileLayouts(List<CreateEditMobileLayout> createMobileLayouts) {
		this.createMobileLayouts = createMobileLayouts;
	}

	public List<CreateEditMobileLayout> getEditMobileLayouts() {
		return editMobileLayouts;
	}

	public void setEditMobileLayouts(List<CreateEditMobileLayout> editMobileLayouts) {
		this.editMobileLayouts = editMobileLayouts;
	}

}
