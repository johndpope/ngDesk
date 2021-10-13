package com.ngdesk.data.csvimport.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.dao.BasePhone;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.Relationship;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.data.modules.dao.DataType;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;

@Service
public class DataTypeService {

	@Autowired
	CsvImportService csvImportService;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	Global global;

	public Map<String, Object> formatDataTypes(List<ModuleField> fields, Map<String, Object> inputMessage,
			CsvImport csvDocument, int i, String companyId, Map<String, Object> user, String globalTeamId,
			Module module) {

		Map<String, String> fieldIdNameMap = new HashMap<String, String>();
		boolean error = false;
		for (ModuleField field : fields) {
			String fieldName = field.getName();
			String displayLabel = field.getDisplayLabel();
			fieldIdNameMap.put(field.getFieldId(), displayLabel);
			DataType dataType = field.getDataType();

			if (inputMessage.containsKey(fieldName)) {
				if (!field.getRequired() && inputMessage.get(fieldName).toString().isBlank()) {
                    inputMessage.remove(fieldName);
                    continue;
                }
				String value = inputMessage.get(fieldName).toString().trim();
				List<String> ignoredFields = List.of("CREATED_BY", "LAST_UPDATED_BY");
				List<String> numericDataTypes = List.of("Auto Number", "Currency", "Number");
				if (numericDataTypes.contains(dataType.getDisplay())) {
					if (NumberUtils.isParsable(value)) {
						if (dataType.getDisplay().equalsIgnoreCase("Currency")) {
							inputMessage.put(fieldName, Float.valueOf(value));
						} else {
							inputMessage.put(fieldName, Double.valueOf(value).intValue());
						}
					} else {
						csvImportService.addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
						error = true;
						break;
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Checkbox")) {
					if (value.equalsIgnoreCase("true")) {
						inputMessage.put(fieldName, true);
					} else {
						inputMessage.put(fieldName, false);
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Picklist")) {
					List<String> picklistValues = field.getPicklistValues();
					String picklistValue = value;

					String filteredValue = picklistValues.stream().filter(list -> list.equalsIgnoreCase(picklistValue))
							.findFirst().orElse(null);

					if (filteredValue != null) {
						inputMessage.put(fieldName, filteredValue);
					} else {
						csvImportService.addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
						error = true;
						break;
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Chronometer")) {
					if (inputMessage.get(fieldName) != null) {
						value = value.toLowerCase();
						String valueWithoutSpace = value.replaceAll("\\s+", "");
						if (valueWithoutSpace.length() == 0 || valueWithoutSpace.charAt(0) == '-') {
							inputMessage.put(fieldName, 0);
						} else if (valueWithoutSpace.length() != 0) {
							inputMessage.put(fieldName, valueWithoutSpace);
						}
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Picklist (Multi-Select)")) {
					List<String> picklistValues = field.getPicklistValues();
					List<String> values = csvImportService.parseString(value);
					List<String> selectedtValues = new ArrayList<String>();
					for (String eachValue : values) {
						String filteredValue = picklistValues.stream().filter(list -> list.equalsIgnoreCase(eachValue))
								.findFirst().orElse(null);

						if (filteredValue != null) {
							selectedtValues.add(filteredValue);
						} else {
							csvImportService.addToSet(i, displayLabel + " values are invalid",
									csvDocument.getCsvImportId());
							error = true;
							break;
						}
					}
					if (error) {
						break;
					} else if (!selectedtValues.isEmpty()) {
						inputMessage.put(fieldName, selectedtValues);
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Discussion")) {
					Optional<Map<String, Object>> optionalContact = moduleEntryRepository.findEntryById(
							user.get("CONTACT").toString(), moduleService.getCollectionName("Contacts", companyId));
					if (optionalContact.isPresent()) {
						Map<String, Object> contactEntry = optionalContact.get();
						String firstName = contactEntry.get("FIRST_NAME").toString();
						String lastName = contactEntry.get("LAST_NAME").toString();

						Sender sender = new Sender(firstName, lastName, user.get("USER_UUID").toString(),
								user.get("ROLE").toString());

						DiscussionMessage message = new DiscussionMessage(value, new Date(),
								UUID.randomUUID().toString(), "MESSAGE", null, sender);

						List<DiscussionMessage> messages = Arrays.asList(message);
						inputMessage.put(fieldName, messages);
					}
				} else if (dataType.getDisplay().equalsIgnoreCase("List Text")) {
					List<String> values = csvImportService.parseString(value);
					if (values.size() != 0) {
						inputMessage.put(fieldName, values);
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Phone")) {
					BasePhone phone = new BasePhone();
					String[] split = null;

					if (value != null) {
						String separator = csvDocument.getCsvFormat().getSeparator();
						if (separator != null) {
							separator = (separator.equalsIgnoreCase("Blank space")) ? " " : "-";
							split = value.split(separator);
							if (split.length != 2) {
								error = true;
							}
						} else {
							separator = "-";
							split = value.split(separator);
							if (split.length != 2) {
								error = true;
							}
						}
					}
					if (error || phone == null) {
						csvImportService.addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
						break;
					} else {
						try {
							phone = createPhoneObject(split[0].trim(), split[1].trim(), phone);
						} catch (Exception e) {
							csvImportService.addToSet(i, displayLabel + " value is invalid", csvDocument.getCsvImportId());
							break;
						}
						inputMessage.put(fieldName, phone);
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Date/Time")
						|| dataType.getDisplay().equalsIgnoreCase("Date")
						|| dataType.getDisplay().equalsIgnoreCase("Time")) {

					List<String> dateFormats = List.of("dd/MM/yyyy hh:mm:ss", "dd-MM-yyyy hh:mm:ss",
							"MM/dd/yyyy hh:mm:ss", "MM-dd-yyyy hh:mm:ss", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy",
							"MM-dd-yyyy", "dd MMM yyyy", "dd MMMM yyyy", "MMM dd, yyyy", "MMMM dd, yyyy", "h:mm",
							"h:mm:ss");
					CsvFormat csvFormat = csvDocument.getCsvFormat();
					SimpleDateFormat df = new SimpleDateFormat();
					String format = null;

					if (dataType.getDisplay().equalsIgnoreCase("Date/Time")) {
						format = dateFormats.stream()
								.filter(dateFormat -> dateFormat.equalsIgnoreCase(csvFormat.getDateTimeFormat()))
								.findFirst().orElse(null);
					} else if (dataType.getDisplay().equalsIgnoreCase("Date")) {
						format = dateFormats.stream()
								.filter(dateFormat -> dateFormat.equalsIgnoreCase(csvFormat.getDateFormat()))
								.findFirst().orElse(null);
					} else if (dataType.getDisplay().equalsIgnoreCase("Time")) {
						format = dateFormats.stream()
								.filter(timeFormat -> timeFormat.equalsIgnoreCase(csvFormat.getTimeFormat()))
								.findFirst().orElse(null);
					}

					df = (format == null) ? new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX")
							: new SimpleDateFormat(format);
					Date date = new Date();
					try {
						date = df.parse(value);
						inputMessage.put(fieldName, date);
					} catch (Exception e) {

						df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
						date = new Date();
						try {
							date = df.parse(value);
							inputMessage.put(fieldName, date);
						} catch (Exception exception) {
							csvImportService.addToSet(i, displayLabel + " value is invalid",
									csvDocument.getCsvImportId());
							error = true;
							break;
						}
					}

				} else if (dataType.getDisplay().equalsIgnoreCase("Relationship")
						&& !ignoredFields.contains(fieldName)) {
					if (field.getRelationshipType().equalsIgnoreCase("One To One")) {
						String relationshipId = csvImportService.getRelationshipId(field, companyId, value);

						if (relationshipId != null
								&& csvImportService.checkRelationshipStatus(field, relationshipId, companyId)) {
							Relationship relationship = new Relationship(relationshipId, value);
							inputMessage.put(fieldName, relationship);
						} else {
							String message = (relationshipId == null) ? " relationship value is invalid"
									: " relationship already exist";
							csvImportService.addToSet(i, displayLabel + message, csvDocument.getCsvImportId());
							error = true;
							break;
						}

					} else if (field.getRelationshipType().equalsIgnoreCase("Many To One")) {
						String relationshipId = csvImportService.getRelationshipId(field, companyId, value);
						if (relationshipId != null) {
							Relationship relationship = new Relationship(relationshipId, value);
							inputMessage.put(fieldName, relationship);
						} else {
							csvImportService.addToSet(i, displayLabel + " relationship value is invalid",
									csvDocument.getCsvImportId());
							error = true;
							break;
						}

					} else if (field.getRelationshipType().equalsIgnoreCase("Many To Many")) {
						List<String> values = csvImportService.parseString(value);
						if (values != null) {
							List<Relationship> relationshipList = new ArrayList<Relationship>();
							for (String eachValue : values) {
								String relationshipId = csvImportService.getRelationshipId(field, companyId, eachValue);
								if (relationshipId != null) {
									Relationship relationship = new Relationship(relationshipId, value);
									relationshipList.add(relationship);
								} else {
									csvImportService.addToSet(i, displayLabel + " relationship value is invalid",
											csvDocument.getCsvImportId());
									error = true;
									break;
								}
							}
							if (error) {
								break;
							} else {
								inputMessage.put(fieldName, relationshipList);
							}
						} else {
							csvImportService.addToSet(i, displayLabel + " relationship value is invalid",
									csvDocument.getCsvImportId());
							error = true;
							break;
						}
					}
				}
			}

			if (field.getRequired()) {
				if (inputMessage.get(fieldName) == null) {
					if (dataType.getDisplay().equalsIgnoreCase("ID")) {
						inputMessage.put(fieldName, UUID.randomUUID().toString());
					}
				}
				if (field.getName().equals("TEAMS")) {
					List<Relationship> teams = new ArrayList<Relationship>();
					if (globalTeamId != null) {
						Relationship relationship = new Relationship(globalTeamId, csvImportService
								.getPrimaryDisplayFieldValue("TEAMS", module, companyId, globalTeamId).toString());
						teams.add(relationship);
					}
					inputMessage.put("TEAMS", teams);
				}
			}
		}
		if (error) {
			return null;
		}

		return inputMessage;
	}

	public BasePhone createPhoneObject(String countryDialCode, String phoneNumber, BasePhone phone) {

		String countriesJson = global.getFile("countriesWithDialCode.json");
		List<Map<String, Object>> countries = new ArrayList<Map<String, Object>>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> mapCountries = mapper.readValue(countriesJson, Map.class);
			countries = mapper.readValue(mapper.writeValueAsString(mapCountries.get("COUNTRIES")),
					mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
		} catch (Exception e) {
			throw new InternalErrorException(e.getMessage());
		}

		if (countries == null || countries.isEmpty()) {
			return null;
		}

		String updatedCode = countryDialCode.replace("+", "").trim();
		Map<String, Object> country = countries.stream()
				.filter(countryList -> countryList.get("DIAL_CODE").toString().equals(updatedCode)).findFirst()
				.orElse(null);

		if (country != null) {
			phone.setCountryCode(country.get("COUNTRY_CODE").toString());
			phone.setPhoneNumber(phoneNumber);
			phone.setCountryFlag(country.get("COUNTRY_FLAG").toString());
			countryDialCode = (!countryDialCode.contains("+")) ? "+" + countryDialCode : countryDialCode;
			phone.setDialCode(countryDialCode);
		} else {
			return null;
		}

		return phone;
	}
}
