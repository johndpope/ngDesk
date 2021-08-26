package com.ngdesk.module.field.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.FieldRepository;
import com.ngdesk.repositories.ModuleRepository;

@Component
public class ModuleFieldService {

	@Autowired
	Global global;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleService roleService;

	@Autowired
	FieldRepository fieldRepository;

	@Autowired
	FieldRepository fieldsRepository;

	public void isValidModuleId(String moduleId, String collectionName) {
		Optional<Module> optionalModule;
		optionalModule = moduleRepository.findById(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {

			throw new BadRequestException("MODULE_ID_INVALID", null);
		}

	}

	public void isModuleFieldValid(String moduleId, String fieldId, String collectionName) {
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		List<ModuleField> moduleFields = optionalModule.get().getFields();
		// List<String> fieldIds = new ArrayList<String>();

		ModuleField fields = moduleFields.stream().filter(ModuleField -> ModuleField.getFieldId().equals(fieldId))
				.findAny().get();

		if (!(fields.equals(fieldId))) {
			throw new BadRequestException("FIELD_ID_INVALID", null);
		}

	}

	public void isInternalField(String moduleId, String fieldId, String collectionName) {

		Optional<Module> optionalModule1 = moduleRepository.findById(moduleId, collectionName);
		Module module = optionalModule1.get();

		for (int i = 0; i < module.getFields().size(); i++) {
			if ((module.getFields().get(i).getFieldId().equals(fieldId)) && (module.getFields().get(i).getInternal())) {

				throw new BadRequestException("INTERNAL_FIELD_ID", null);

			}

		}
	}

	public void isFieldNameUnchange(String moduleId, String fieldId, String collectionName, ModuleField moduleField) {

		Optional<Module> optionalModule1 = moduleRepository.findById(moduleId, collectionName);
		Module module = optionalModule1.get();

		for (int i = 0; i < module.getFields().size(); i++) {

			String fieldName = module.getFields().get(i).getName();

			if (!(moduleField.getName().equals(module.getFields().get(i).getName()))
					&& (module.getFields().get(i).getFieldId().equals(fieldId))) {

				throw new BadRequestException("NAME_IS_INVALID", null);
			}

		}

	}

	public long chronometerValueConversionInSeconds(String input) {
		try {
			Pattern periodPattern = Pattern.compile("(\\d+)(mo|w|d|m|h)");
			Matcher matcher = periodPattern.matcher(input);
			long chronometerValueInSecond = 0;
			while (matcher.find()) {
				int num = Integer.parseInt(matcher.group(1));
				String typ = matcher.group(2);
				switch (typ) {
				case "mo":
					chronometerValueInSecond = num * 9600;
					break;
				case "w":
					chronometerValueInSecond = chronometerValueInSecond + (num * 2400);
					break;
				case "d":
					chronometerValueInSecond = chronometerValueInSecond + (num * 480);
					break;
				case "h":
					chronometerValueInSecond = chronometerValueInSecond + (num * 60);
					break;
				case "m":
					chronometerValueInSecond = chronometerValueInSecond + num;
					break;

				}
			}
			return chronometerValueInSecond;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void isDataTypeModified(ModuleField field, String moduleId) {
		Optional<Module> optionalModule;
		optionalModule = moduleRepository.findById(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());
		List<ModuleField> moduleFields = optionalModule.get().getFields();

		for (ModuleField fielddoc : moduleFields) {
			DataType fieldDataTypeDoc = fielddoc.getDataType();
			if (!fielddoc.getFieldId().equals(field.getFieldId())) {
		
				if (field.getDataType().getDisplay().equals("Discussion")
						&& fieldDataTypeDoc.getDisplay().equals("Discussion")) {
					throw new BadRequestException("FIELD_DISCUSSION_EXISTS", null);
				}
			} else {
				// SAME FIELD
				ModuleField existingField = new ModuleField();
				String existingFieldName = existingField.getName();
				String datatypeDisplay = field.getDataType().getDisplay();
				String datatypeBackend = field.getDataType().getBackend();

				String existingDisplayDatatype = existingField.getDataType().getDisplay();
				String existingBackEndDatatype = existingField.getDataType().getBackend();

				if (!datatypeDisplay.equals(existingDisplayDatatype)
						|| !existingBackEndDatatype.equals(datatypeBackend)) {
					throw new BadRequestException("DATA_TYPE_MODIFIED", null);
				}
				if (!field.getName().equals(existingFieldName)) {
					throw new BadRequestException("FIELD_NAME_CANNOT_BE_CHANGED", null);
				}

			}
		}
	}

	public ModuleField initializeFieldWithDefaults(ModuleField field) {
		field.setDateCreated(new Date());
		field.setDateUpdated(new Date());
		field.setCreatedBy(authManager.getUserDetails().getUserId());
		field.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		field.setInternal(false);
		field.setFieldId(UUID.randomUUID().toString());
		return field;

	}

	public int getSizeForElastic(ModuleField field, Module module) {
		int size = 0;
		int totalDataTimeField = 0;
		for (ModuleField moduleField : module.getFields()) {

			if (moduleField.getDataType().getDisplay().equals("Date/Time")
					|| moduleField.getDataType().getDisplay().equals("Date")
					|| moduleField.getDataType().getDisplay().equals("Time")) {
				totalDataTimeField++;
			}
		}

		if (totalDataTimeField == 0 && !field.getDataType().getDisplay().equals("Date/Time")
				&& !field.getDataType().getDisplay().equals("Date")
				&& !field.getDataType().getDisplay().equals("Time")) {
			size = module.getFields().size();

		} else if (totalDataTimeField == 0 && (field.getDataType().getDisplay().equals("Date/Time")
				|| field.getDataType().getDisplay().equals("Date")
				|| field.getDataType().getDisplay().equals("Time"))) {
			size = 84;
		} else if (totalDataTimeField != 0 && !field.getDataType().getDisplay().equals("Date/Time")
				&& !field.getDataType().getDisplay().equals("Date")
				&& !field.getDataType().getDisplay().equals("Time")) {

			size = module.getFields().size() - totalDataTimeField;
		} else if (totalDataTimeField != 0 && (field.getDataType().getDisplay().equals("Date/Time")
				|| field.getDataType().getDisplay().equals("Date")
				|| field.getDataType().getDisplay().equals("Time"))) {

			size = 84 + totalDataTimeField;
		}
		return size;
	}

	public void postDataFilter(ModuleField field) {

		if (field.getDataType().getDisplay().equalsIgnoreCase("Relationship") && field.getDataFilter() != null
				&& field.getModule() != null && field.getRelationshipField() != null) {

			Optional<Module> optionalRelationshipModule = moduleRepository.findById(field.getModule(),
					"modules_" + authManager.getUserDetails().getCompanyId());
			if (optionalRelationshipModule.isEmpty()) {
				throw new BadRequestException("RELATED_MODULE_NOT_FOUND", null);
			}

			Module relatedModule = optionalRelationshipModule.get();

			Optional<ModuleField> optionalRelatedField = relatedModule.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equalsIgnoreCase(field.getRelationshipField()))
					.findFirst();

			if (optionalRelatedField.isEmpty()) {
				String[] vars = { "RELATED_FIELD" };
				throw new BadRequestException("DAO_NOT_FOUND", vars);
			}

			ModuleField relatedField = optionalRelatedField.get();

			relatedField.setDataFilter(field.getDataFilter());

			fieldRepository.updateField(relatedField, relatedModule.getModuleId(),
					"modules_" + authManager.getUserDetails().getCompanyId());
		}

	}

	public void createDefaultAddressFields(ModuleField field, Module module) {

		String[] typeName = { "Street 1", "Street 2", "City", "Country", "Zipcode" };
		String groupId = UUID.randomUUID().toString();
		field.setGroupId(groupId);
		for (int i = 0; i < typeName.length; i++) {
			String name = null;
			DataType dataType = new DataType();
			String displayType = typeName[i];
			dataType.setBackend("String");
			ModuleField newField = new ModuleField();
			newField.setFieldId(UUID.randomUUID().toString());
			newField.setDisplayLabel(field.getName().toLowerCase() + " " + displayType);
			newField.setDataType(dataType);
			newField.setGroupId(groupId);
			if (displayType.contains("Street")) {
				name = displayType.substring(0, 6).toUpperCase() + "_" + displayType.split("Street ")[1];
			} else {
				name = displayType.toUpperCase();
			}
			if (typeName[i].equals("Country")) {
				dataType.setDisplay("Picklist");
				String countries = global.getFile("Countries.json");
				ObjectMapper mapper = new ObjectMapper();
				try {
					List<String> picklistValues = mapper.readValue(countries, List.class);
					newField.setPicklistValues(picklistValues);
				} catch (JsonProcessingException e) {

					e.printStackTrace();
				}
			} else {
				dataType.setDisplay(displayType);
			}
			newField.setName(field.getName() + "_" + name);
			newField = setMandatoryFields(newField, field);

			roleService.updateRolesWithNewField(module, newField);

			fieldsRepository.saveField("modules_" + authManager.getUserDetails().getCompanyId(), newField,
					module.getModuleId());
		}
	}

	public ModuleField setMandatoryFields(ModuleField updatedField, ModuleField field) {

		if (field.getVisibility() != null) {
			updatedField.setVisibility(field.getVisibility());
		} else {
			updatedField.setVisibility(false);
		}
		if (field.getRequired() != null) {
			updatedField.setRequired(field.getRequired());
		} else {
			updatedField.setRequired(false);
		}
		if (field.getInternal() != null) {
			updatedField.setInternal(field.getInternal());
		} else {
			updatedField.setInternal(false);
		}
		if (field.getNotEditable() != null) {
			updatedField.setNotEditable(field.getNotEditable());
		} else {
			updatedField.setNotEditable(false);
		}

		updatedField.setDateCreated(new Date());
		updatedField.setDateUpdated(new Date());
		updatedField.setCreatedBy(authManager.getUserDetails().getUserId());
		updatedField.setLastUpdatedBy(authManager.getUserDetails().getUserId());

		return updatedField;
	}
}