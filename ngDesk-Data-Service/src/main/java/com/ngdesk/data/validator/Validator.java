package com.ngdesk.data.validator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.bson.types.ObjectId;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.dao.BasePhone;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.SlaService;
import com.ngdesk.data.modules.dao.BaseCondition;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.modules.dao.ModuleValidation;
import com.ngdesk.data.modules.dao.Validation;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.data.sam.dao.DiscoveryMap;
import com.ngdesk.data.sla.dao.SLA;
import com.ngdesk.data.sla.dao.SLABusinessRules;
import com.ngdesk.data.sla.dao.SLAConditions;
import com.ngdesk.data.sla.dao.SLARestriction;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.discoverymap.DiscoveryMapRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;

@Component
public class Validator {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	DiscoveryMapRepository discoveryMapRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	SlaService slaService;

	private final Logger log = LoggerFactory.getLogger(Validator.class);

	public boolean isValidObjectId(String objectId) {
		try {
			if (ObjectId.isValid(objectId)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// TODO: Handle Phone types check
	public boolean requiredFieldsPresent(Module module, Map<String, Object> entry) {
		List<ModuleField> requiredFields = module.getFields().stream().filter(field -> {
			return field.getRequired() == true;
		}).collect(Collectors.toList());
		requiredFields.forEach(field -> {
			String[] vars = { field.getDisplayLabel() };

			if (entry.get(field.getName()) == null) {
				throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
			} else {
				if (field.getDataType().getDisplay().equalsIgnoreCase("text")) {
					if (entry.get(field.getName()).toString().isBlank()) {
						throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
					}
				} else if (field.getDataType().getDisplay().equalsIgnoreCase("Relationship")) {
					if (field.getRelationshipType().equalsIgnoreCase("Many to One")
							|| field.getRelationshipType().equalsIgnoreCase("One to One")) {
						if (entry.get(field.getName()).toString().isBlank()) {
							throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
						}
					} else if (field.getRelationshipType().equalsIgnoreCase("Many to Many")) {
						try {
							List<String> fieldValues = (List<String>) entry.get(field.getName());
							if (fieldValues.size() == 0) {
								throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
							}
						} catch (Exception e) {
							log.error("Invalid List Type");
							throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
						}

					}
				} else if (field.getDataType().getDisplay().equalsIgnoreCase("List Text")) {
					try {
						List<String> fieldValues = (List<String>) entry.get(field.getName());
						if (fieldValues.size() == 0) {
							throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
						}

					} catch (Exception e) {
						log.error("Invalid List Type");
						throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
					}
				} else if (field.getDataType().getDisplay().equalsIgnoreCase("Email")) {
					if (entry.get(field.getName()).toString().isBlank()) {
						throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
					}
				} else if (field.getDataType().getDisplay().equalsIgnoreCase("URL")) {
					if (entry.get(field.getName()).toString().isBlank()) {
						throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
					}
				}
			}
		});

		return true;
	}

	// TODO: Extensive handling of phone data type such as country code, country
	// flag etc..
	// TODO: Country Validation (Talk to Spencer)
	// TODO: Better zipcode Validation
	public void validateBaseTypes(Module module, Map<String, Object> entry, String companyId) {
		String[] fieldNames = { "DATA_ID", "SOURCE_TYPE", "DATE_CREATED", "DATE_UPDATED", "LAST_UPDATED_BY",
				"LAST_UPDATED_ON", "CREATED_BY", "META_DATA", "_id", "EFFECTIVE_FROM", "EFFECTIVE_TO" };

		List<String> fieldNamesToSkipBaseValidations = new ArrayList<String>();
		fieldNamesToSkipBaseValidations.addAll(Arrays.asList(fieldNames));

		List<String> slaFieldNames = slaService.generateSlaFieldNames(module.getModuleId(),
				authManager.getUserDetails().getCompanyId());

		if (slaFieldNames.size() > 0 && slaFieldNames != null) {
			fieldNamesToSkipBaseValidations.addAll(slaFieldNames);
		}

		ObjectMapper mapper = new ObjectMapper();

		List<ModuleField> fields = module.getFields().stream()
				.filter(field -> !fieldNamesToSkipBaseValidations.contains(field.getName()))
				.collect(Collectors.toList());
		fields.forEach(field -> {
			String fieldName = field.getName();

			String displayDataType = field.getDataType().getDisplay();
			String backendDataType = field.getDataType().getBackend();

			log.debug("---Starting Evaluating of " + fieldName + "---");
			if (entry.get(fieldName) != null) {

				String[] vars = { field.getDisplayLabel() };

				log.debug("Entry contains field");
				log.debug("Display Data type of " + fieldName + " is " + displayDataType);
				log.debug("Backend Data type of " + fieldName + " is " + backendDataType);

				if (fieldName.equalsIgnoreCase("DISCOVERY_MAP")) {

					List<String> discoveryMapIds = (List<String>) entry.get(fieldName);
					for (String discoveryMapId : discoveryMapIds) {
						Optional<DiscoveryMap> optionalDiscoveryMap = discoveryMapRepository
								.findByCompanyIdAndId(discoveryMapId, companyId, "sam_discovery_map");
						if (optionalDiscoveryMap.isEmpty()) {
							throw new BadRequestException("DISCOVERY_MAP_NOT_FOUND", null);
						}
					}

				} else {

					if (displayDataType.equals("Picklist (Multi-Select)") && !isBlank(entry, fieldName)) {
						List<String> picklistValues = (List<String>) entry.get(fieldName);
						for (String picklistValue : picklistValues) {
							if (!field.getPicklistValues().contains(picklistValue)) {
								throw new BadRequestException("BASE_TYPE_PICKLIST_INVALID", vars);
							}
						}
					}
				}

				if (displayDataType.equals("Email") && !isBlank(entry, fieldName)) {
					String value = entry.get(fieldName).toString();
					if (!EmailValidator.getInstance().isValid(value)) {
						throw new BadRequestException("BASE_TYPE_EMAIL_INVALID", vars);
					}
				} else if (displayDataType.equals("Number")) {
					try {
						Integer.parseInt(entry.get(fieldName).toString());
					} catch (NumberFormatException e) {
						try {
							Long.parseLong(entry.get(fieldName).toString());
						} catch (NumberFormatException e1) {
							throw new BadRequestException("BASE_TYPE_NUMBER_INVALID", vars);
						}
					}
				} else if (field.getDataType().getDisplay().equalsIgnoreCase("List Text")) {

					// TODO: Implement Sharath
//					try {
//						if(field.getUnique()) {
//						Set<String> listText = new HashSet<String>((List<String>) entry.get(field.getName()));
//						entry.put(field.getName(), listText);
//						}
//						
//					} catch (Exception e) {
//						log.error("Invalid List Type");
//						throw new BadRequestException("REQUIRED_FIELD_MISSING", vars);
//					}
				}

				else if (displayDataType.equals("Phone")) {
					try {
						BasePhone basePhone = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)),
								BasePhone.class);

						if (basePhone.getPhoneNumber() != null && !basePhone.getPhoneNumber().isBlank()) {
							if (basePhone.getCountryCode() == null || basePhone.getCountryCode().isBlank()
									|| basePhone.getCountryCode().length() != 2) {
								throw new BadRequestException("BASE_TYPE_PHONE_COUNTRY_CODE_INVALID", vars);
							}

							if (basePhone.getCountryFlag() == null || basePhone.getCountryFlag().isBlank()) {
								throw new BadRequestException("BASE_TYPE_PHONE_COUNTRY_FLAG_INVALID", vars);
							}

							if (basePhone.getDialCode() == null || basePhone.getDialCode().isBlank()) {
								throw new BadRequestException("BASE_TYPE_PHONE_DIAL_CODE_INVALID", vars);
							}

							PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
							String phoneNumberE164Format = basePhone.getDialCode() + basePhone.getPhoneNumber();
							PhoneNumber phoneNumberProto = phoneUtil.parse(phoneNumberE164Format, null);
							if (!phoneUtil.isValidNumber(phoneNumberProto)) {
								throw new BadRequestException("BASE_TYPE_PHONE_PHONE_NUMBER_INVALID", vars);
							}
						}

					} catch (JsonProcessingException e) {
						throw new BadRequestException("BASE_TYPE_PHONE_INVALID", vars);
					} catch (NumberParseException e) {
						throw new BadRequestException("BASE_TYPE_PHONE_PHONE_NUMBER_INVALID", vars);
					}
				} else if (displayDataType.equals("ID") && !isBlank(entry, fieldName)) {
					try {
						UUID.fromString(entry.get(fieldName).toString());
					} catch (Exception e) {
						e.printStackTrace();
						throw new BadRequestException("BASE_TYPE_ID_INVALID", vars);
					}
				} else if (displayDataType.equals("Country") && !isBlank(entry, fieldName)) {
					if (entry.get(fieldName).toString().isBlank()) {
						throw new BadRequestException("BASE_TYPE_COUNTRY_INVALID", vars);
					}
				} else if (displayDataType.equals("Percent")) {
					try {
						Double.parseDouble(entry.get(fieldName).toString());
					} catch (NumberFormatException e) {
						throw new BadRequestException("BASE_TYPE_PERCENT_INVALID", vars);
					}
				} else if (displayDataType.equals("Checkbox")) {
					try {
						boolean bool = (boolean) entry.get(fieldName);
					} catch (ClassCastException e) {
						throw new BadRequestException("BASE_TYPE_BOOLEAN_INVALID", vars);
					}
				} else if (displayDataType.equals("URL") && !isBlank(entry, fieldName)) {
					String[] schemes = { "http", "https" }; // DEFAULT schemes = "http", "https", "ftp"
					UrlValidator urlValidator = new UrlValidator(schemes);
					String value = entry.get(fieldName).toString();
					if (!urlValidator.isValid(value)) {
						throw new BadRequestException("BASE_TYPE_URL_INVALID", vars);
					}

				} else if ((displayDataType.equals("Date/Time") || displayDataType.equals("Date")
						|| displayDataType.equals("Time")) && !isBlank(entry, fieldName)) {
					try {
						Date date = (Date) entry.get(fieldName);
					} catch (ClassCastException e) {
						e.printStackTrace();
						throw new BadRequestException("BASE_TYPE_TIMESTAMP_INVALID", vars);
					}
				} else if (displayDataType.equals("Picklist") && !isBlank(entry, fieldName)) {

					if (!field.getPicklistValues().contains(entry.get(fieldName).toString())) {
						throw new BadRequestException("BASE_TYPE_PICKLIST_INVALID", vars);
					}
				} else if (displayDataType.equals("Zipcode") && !isBlank(entry, fieldName)) {
					String value = entry.get(fieldName).toString();
					if (value.length() < 5 || value.length() > 10) {
						throw new BadRequestException("BASE_TYPE_ZIPCODE_INVALID", vars);
					}
				} else if (displayDataType.equals("Chronometer")) {
					String regex = "^(\\d+mo)?(\\d+w)?(\\d+d)?(\\d+h)?(\\d+m)?$";
					String value = entry.get(fieldName).toString();
					String valueWithoutminus = value.replaceAll("\\-", "");
					String valueWithoutSpace = valueWithoutminus.replaceAll("\\s+", "");

					Pattern p = Pattern.compile(regex, Pattern.DOTALL);
					Matcher m = p.matcher(valueWithoutSpace);
					if (!m.find()) {
						throw new BadRequestException("BASE_TYPE_CHRONOMETER_INVALID", vars);
					}
				} else if (displayDataType.equals("Relationship")) {

					String relationshipType = field.getRelationshipType();
					String relatedModuleId = field.getModule();

					if (relatedModuleId == null || !ObjectId.isValid(relatedModuleId)) {
						throw new BadRequestException("BASE_TYPE_RELATIONSHIP_MODULE_INVALID", vars);
					}

					Optional<Module> optionalRelatedModule = modulesRepository.findById(relatedModuleId,
							"modules_" + companyId);
					if (optionalRelatedModule.isEmpty()) {
						throw new BadRequestException("BASE_TYPE_RELATIONSHIP_MODULE_INVALID", vars);
					}

					Module relatedModule = optionalRelatedModule.get();
					if (relationshipType.equals("One to One") || relationshipType.equals("Many to One")) {

						String value = entry.get(fieldName).toString();
						if (value.isBlank() || !ObjectId.isValid(value)) {
							throw new BadRequestException("BASE_TYPE_RELATIONSHIP_ENTRY_INVALID", vars);
						}

						Optional<Map<String, Object>> optionalRelatedEntry = moduleEntryRepository.findEntryById(
								entry.get(fieldName).toString(),
								moduleService.getCollectionName(relatedModule.getName(), companyId));
						if (optionalRelatedEntry.isEmpty()) {
							throw new BadRequestException("BASE_TYPE_RELATIONSHIP_ENTRY_INVALID", vars);
						}
					} else if (relationshipType.equals("Many to Many")) {
						try {
							List<String> relatedEntryIds = mapper.readValue(
									mapper.writeValueAsString(entry.get(fieldName)),
									mapper.getTypeFactory().constructCollectionType(List.class, String.class));
							for (String entryId : relatedEntryIds) {
								if (entryId.isBlank() || !ObjectId.isValid(entryId)) {
									throw new BadRequestException("BASE_TYPE_RELATIONSHIP_ENTRIES_INVALID", vars);
								}
								Optional<Map<String, Object>> optionalRelatedEntry = moduleEntryRepository
										.findEntryById(entryId,
												moduleService.getCollectionName(relatedModule.getName(), companyId));
								if (optionalRelatedEntry.isEmpty()) {
									throw new BadRequestException("BASE_TYPE_RELATIONSHIP_ENTRIES_INVALID", vars);
								}
							}
						} catch (ClassCastException e) {
							throw new BadRequestException("BASE_TYPE_RELATIONSHIP_FORMAT_INVALID", vars);
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
					}
				} else if (displayDataType.equals("Discussion")) {
					try {
						List<DiscussionMessage> messages = mapper.readValue(
								mapper.writeValueAsString(entry.get(fieldName)),
								mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
					} catch (Exception e) {
						e.printStackTrace();
						throw new BadRequestException("BASE_TYPE_DISCUSSION_FORMAT_INVALID", vars);
					}
				} else if (displayDataType.equals("Approval")) {
					// TODO: ASK FOR VALIDATIONS
				} else if (displayDataType.equals("Image")) {
					String fileExtension = null;
					String value = entry.get(fieldName).toString();
					if (value == null || value.isEmpty()) {
						throw new BadRequestException("IMAGE_FIELD_INVALID", vars);
					}
					try {
						List<MessageAttachment> attachments = mapper.readValue(
								mapper.writeValueAsString(entry.get(fieldName)),
								mapper.getTypeFactory().constructCollectionType(List.class, MessageAttachment.class));
						for (MessageAttachment attachment : attachments) {
							String fileName = attachment.getFileName();
							if (fileName.contains(".jpeg") || fileName.contains(".png") || fileName.contains(".jpg")
									|| fileName.contains(".eps") || fileName.contains(".bmp")
									|| fileName.contains(".gif") || fileName.contains(".tiff")
									|| fileName.contains(".raw")) {
								if ((attachment.getFileExtension()) != null) {
									fileExtension = attachment.getFileExtension();
									if (!(fileExtension.toLowerCase().equals("image/jpg")
											|| fileExtension.toLowerCase().equals("image/jpeg")
											|| fileExtension.toLowerCase().equals("image/png")
											|| fileExtension.toLowerCase().equals("image/eps")
											|| fileExtension.toLowerCase().equals("image/bmp")
											|| fileExtension.toLowerCase().equals("image/gif")
											|| fileExtension.toLowerCase().equals("image/tiff")
											|| fileExtension.toLowerCase().equals("image/raw"))) {
										throw new BadRequestException("BASE_TYPE_IMAGE_FORMAT_INVALID", vars);
									}
								}
							} else {
								throw new BadRequestException("BASE_TYPE_IMAGE_FORMAT_INVALID", vars);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new BadRequestException("BASE_TYPE_IMAGE_FORMAT_INVALID", vars);
					}
				} else if (displayDataType.equals("File Upload")) {
					try {
						List<MessageAttachment> attachments = mapper.readValue(
								mapper.writeValueAsString(entry.get(fieldName)),
								mapper.getTypeFactory().constructCollectionType(List.class, MessageAttachment.class));
					} catch (Exception e) {
						e.printStackTrace();
						throw new BadRequestException("BASE_TYPE_FILE_UPLOAD_FORMAT_INVALID", vars);
					}
				} else if (displayDataType.equals("Condition")) {
					BaseCondition baseCondition;

					try {
						baseCondition = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)),
								BaseCondition.class);

					} catch (Exception e) {
						e.printStackTrace();
						throw new BadRequestException("BASE_TYPE_CONDITION_FORMAT_INVALID", vars);
					}

					/* isValidBaseCondition(baseCondition.getCondition()); */
					isValidVersionEditionFieldId(baseCondition.getCondition(), fieldName);

					if (!(baseCondition.getOpearator() == null)) {

						isValidOperator(baseCondition.getOpearator());
					}
				}

			}
			log.debug("---End of Evaluation of " + fieldName + "---");
		});

	}

	private boolean isBlank(Map<String, Object> entry, String fieldName) {
		return entry.get(fieldName).toString().isBlank();
	}

	// TODO: HANDLE OR CONDITON FAILUE
	public void validateModuleValidations(Module module, Map<String, Object> entry, String requestType, String roleId,
			String companyId) {
		try {
			Assert.notNull(requestType, "The given requestType must not be null!");
			Assert.notNull(module, "The given module must not be null!");
			Assert.notNull(entry, "The given entry must not be null!");

			ObjectMapper mapper = new ObjectMapper();
			Context context = Context.create("js");
			context.getBindings("js").putMember("inputMessage", mapper.writeValueAsString(entry));
			String initialScript = "inputMessage = JSON.parse(inputMessage);";
			context.eval("js", initialScript);

			List<ModuleValidation> validationsToCheck = new ArrayList<ModuleValidation>();

			if (requestType.equalsIgnoreCase("POST")) {
				validationsToCheck = module.getValidations().stream()
						.filter(validation -> (validation.getType().equals("CREATE")
								|| validation.getType().equals("CREATE_OR_UPDATE"))
								&& validation.getRoles().contains(roleId))
						.collect(Collectors.toList());
			} else if (requestType.equalsIgnoreCase("PUT")) {
				validationsToCheck = module.getValidations().stream()
						.filter(validation -> (validation.getType().equals("UPDATE")
								|| validation.getType().equals("CREATE_OR_UPDATE"))
								&& validation.getRoles().contains(roleId))
						.collect(Collectors.toList());
			}

			String dummyCondition = "1 == 1";

			for (ModuleValidation validation : validationsToCheck) {
				String expressionAnd = "";
				String expressionOr = "";

				if (validation.getValidations() != null) {
					List<Validation> validations = validation.getValidations();

					for (Validation condition : validations) {

						ModuleField field = module.getFields().stream()
								.filter(moduleField -> moduleField.getFieldId().equals(condition.getCondition()))
								.findFirst().orElse(null);

						String statement = generateStatement(condition.getOperator(), condition.getCondition(), entry,
								module, requestType, condition.getConditionValue(), companyId);

						if (condition.getRequirementType().equalsIgnoreCase("All")) {
							String script = "console.log(inputMessage.toString());var bool = " + statement;
							context.eval("js", script);
							if (!context.getBindings("js").getMember("bool").asBoolean()) {
								throwValidationErrorMessage(field.getDisplayLabel(), condition.getOperator(),
										condition.getConditionValue());
							}
							expressionAnd += " && " + statement;
						} else if (condition.getRequirementType().equalsIgnoreCase("Any")) {
							dummyCondition = " 1 == 2";
							expressionOr += "|| " + statement;
						}
					}

					String expression = "1 == 1 " + expressionAnd + " && (" + dummyCondition + " " + expressionOr + ")";
					String script = "var bool = " + expression;
					context.eval("js", script);
					if (!context.getBindings("js").getMember("bool").asBoolean()) {
						List<Validation> failedValidations = validation.getValidations().stream()
								.filter(condition -> condition.getRequirementType().equalsIgnoreCase("Any"))
								.collect(Collectors.toList());

						Validation failedValidation = failedValidations.get(0);
						ModuleField field = module.getFields().stream()
								.filter(moduleField -> moduleField.getFieldId().equals(failedValidation.getCondition()))
								.findFirst().orElse(null);
						throwValidationErrorMessage(field.getDisplayLabel(), failedValidation.getOperator(),
								failedValidation.getConditionValue());
					}
				}
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private String generateStatement(String operator, String variable, Map<String, Object> entry, Module module,
			String requestType, String value, String companyId) {
		try {

			ModuleField field = module.getFields().stream()
					.filter(moduleField -> moduleField.getFieldId().equals(variable)).findFirst().orElse(null);
			String dataType = field.getDataType().getBackend();
			String fieldName = field.getName();

			String statement = "";
			if (operator.equalsIgnoreCase("EQUALS_TO")) {
				if (dataType.equalsIgnoreCase("string")) {
					return "inputMessage." + fieldName + " == '" + value + "'";
				} else if (dataType.equalsIgnoreCase("integer")) {
					return "inputMessage." + fieldName + " == " + value;
				} else if (dataType.equalsIgnoreCase("Formula")) {
					return "inputMessage." + fieldName + " == '" + value + "'";
				} else if (dataType.equalsIgnoreCase("Float")) {
					return "inputMessage." + fieldName + " == " + value;
				} else if (dataType.equalsIgnoreCase("Boolean")) {
					return "inputMessage." + fieldName + " == " + Boolean.parseBoolean(value);
				}
			} else if (operator.equalsIgnoreCase("NOT_EQUALS_TO")) {
				if (dataType.equalsIgnoreCase("string")) {
					return "inputMessage." + fieldName + " != '" + value + "'";
				} else if (dataType.equalsIgnoreCase("integer")) {
					return "inputMessage." + fieldName + " != " + value;
				} else if (dataType.equalsIgnoreCase("Formula")) {
					return "inputMessage." + fieldName + " != '" + value + "'";
				} else if (dataType.equalsIgnoreCase("Float")) {
					return "inputMessage." + fieldName + " != " + value;
				} else if (dataType.equalsIgnoreCase("Boolean")) {
					return "inputMessage." + fieldName + " !== " + Boolean.parseBoolean(value);
				}
			} else if (operator.equalsIgnoreCase("IS")) {
				return "inputMessage." + fieldName + " == '" + value + "'";
			} else if (operator.equalsIgnoreCase("REGEX")) {
				return "inputMessage." + fieldName + ".match('" + value + "') != null";
			} else if (operator.equalsIgnoreCase("EXISTS")) {
				return "inputMessage.hasOwnProperty('" + fieldName + "') && " + "inputMessage." + fieldName
						+ ".length > " + "0";
			} else if (operator.equalsIgnoreCase("DOES_NOT_EXIST")) {
				return "!inputMessage.hasOwnProperty('" + fieldName + "')";
			} else if (operator.equalsIgnoreCase("CONTAINS")) {
				return "(inputMessage.hasOwnProperty('" + fieldName + "') && " + "inputMessage." + fieldName
						+ ".indexOf('" + value + "') != -1)";
			} else if (operator.equalsIgnoreCase("DOES_NOT_CONTAIN")) {
				return "(inputMessage.hasOwnProperty('" + fieldName + "') && " + "inputMessage." + fieldName
						+ ".indexOf('" + value + "') == -1)";
			} else if (operator.equalsIgnoreCase("LESS_THAN")) {
				if (dataType.equalsIgnoreCase("string")) {
					return "inputMessage." + fieldName + ".length < " + value;
				} else if (dataType.equalsIgnoreCase("integer")) {
					return "inputMessage." + fieldName + " < " + value;
				} else if (dataType.equalsIgnoreCase("Float")) {
					return "inputMessage." + fieldName + " < " + value;
				} else if (dataType.equalsIgnoreCase("TIMESTAMP")) {
					return "new Date(inputMessage." + fieldName + ") < new Date('" + value + "')";
				}
			} else if (operator.equalsIgnoreCase("LENGTH_IS_LESS_THAN")) {
				if (dataType.equalsIgnoreCase("string")) {
					return "inputMessage." + fieldName + ".length < " + value;
				}
			} else if (operator.equalsIgnoreCase("GREATER_THAN")) {
				if (dataType.equalsIgnoreCase("string")) {
					return "inputMessage." + fieldName + ".length > " + value;
				} else if (dataType.equalsIgnoreCase("integer")) {
					return "inputMessage." + fieldName + " > " + value;
				} else if (dataType.equalsIgnoreCase("Float")) {
					return "inputMessage." + fieldName + " > " + value;
				} else if (dataType.equalsIgnoreCase("TIMESTAMP")) {
					return "new Date(inputMessage." + fieldName + ") > new Date('" + value + "')";
				}
			} else if (operator.equalsIgnoreCase("LENGTH_IS_GREATER_THAN")) {
				if (dataType.equalsIgnoreCase("string")) {
					return "inputMessage." + fieldName + ".length > " + value;
				}
			} else if (operator.equalsIgnoreCase("IS_UNIQUE")) {
				String dataId = null;
				if (entry.get("_id") != null) {
					dataId = entry.get("_id").toString();
				}
				statement = evaluateIsUnique(fieldName, entry.get(fieldName).toString(), module.getName(), dataId,
						requestType, companyId);
			}
			return statement;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	private String evaluateIsUnique(String variable, Object value, String moduleName, String dataId, String requestType,
			String companyId) {
		try {
			Optional<Map<String, Object>> optionalEntry = null;
			if (requestType.equals("POST")) {
				optionalEntry = moduleEntryRepository.findUniqueEntryForPost(variable, value,
						moduleService.getCollectionName(moduleName, companyId));
			} else if (requestType.equals("PUT")) {
				optionalEntry = moduleEntryRepository.findUniqueEntryForPut(variable, value,
						moduleService.getCollectionName(moduleName, companyId), dataId);
			}

			if (optionalEntry.isEmpty()) {
				return "true";
			}
			return "false";
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	public void throwValidationErrorMessage(String displayLabel, String operator, String value) {

		String[] varsWithValue = { displayLabel, value };
		String[] varsWithoutValue = { displayLabel };

		if (operator.equals("EQUALS_TO")) {
			throw new BadRequestException("MODULE_VALIDATION_EQUALS_TO_FAILED", varsWithValue);
		} else if (operator.equals("NOT_EQUALS_TO")) {
			throw new BadRequestException("MODULE_VALIDATION_NOT_EQUALS_TO_FAILED", varsWithValue);
		} else if (operator.equals("GREATER_THAN")) {
			throw new BadRequestException("MODULE_VALIDATION_GREATER_THAN_FAILED", varsWithValue);
		} else if (operator.equals("LESS_THAN")) {
			throw new BadRequestException("MODULE_VALIDATION_LESS_THAN_FAILED", varsWithValue);
		} else if (operator.equals("IS")) {
			throw new BadRequestException("MODULE_VALIDATION_EQUALS_TO_FAILED", varsWithValue);
		} else if (operator.equals("REGEX")) {
			throw new BadRequestException("MODULE_VALIDATION_REGEX_FAILED", varsWithValue);
		} else if (operator.equals("EXISTS")) {
			throw new BadRequestException("MODULE_VALIDATION_EXISTS_FAILED", varsWithoutValue);
		} else if (operator.equals("DOES_NOT_EXIST")) {
			throw new BadRequestException("MODULE_VALIDATION_DOES_NOT_EXIST_FAILED", varsWithoutValue);
		} else if (operator.equals("CONTAINS")) {
			throw new BadRequestException("MODULE_VALIDATION_CONTAINS_FAILED", varsWithValue);
		} else if (operator.equals("DOES_NOT_CONTAIN")) {
			throw new BadRequestException("MODULE_VALIDATION_DOES_NOT_CONTAIN_FAILED", varsWithValue);
		} else if (operator.equals("IS_UNIQUE")) {
			throw new BadRequestException("MODULE_VALIDATION_IS_UNIQUE_FAILED", varsWithoutValue);
		} else if (operator.contentEquals("LENGTH_IS_GREATER_THAN")) {
			throw new BadRequestException("MODULE_VALIDATION_LENGTH_IS_GREATER_THAN_FAILED", varsWithValue);
		} else if (operator.contentEquals("LENGTH_IS_LESS_THAN")) {
			throw new BadRequestException("MODULE_VALIDATION_LENGTH_IS_LESSER_THAN_FAILED", varsWithValue);
		}
	}

	public void isAutorizedForRecord(List<String> teamIds, String companyId, String userId) {

		List<Map<String, Object>> teams = moduleEntryRepository.findTeamsByIds(teamIds, "Teams_" + companyId);

		List<String> authorizedUsers = new ArrayList<String>();

		teams.forEach(team -> {
			if (team.get("USERS") != null) {
				authorizedUsers.addAll((List<String>) team.get("USERS"));
			}
		});
		if (!authorizedUsers.contains(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
	}

	public void isAutorizedForUserRecord(Map<String, Object> entry, String companyId) {
		if (!entry.get("DATA_ID").toString().equalsIgnoreCase(companyId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
	}

	public void validateRestrictedUsers(List<String> entryIds, String companyId) {
		String[] emailIds = { "ghost@ngdesk.com", "system@ngdesk.com", "probe@ngdesk.com",
				"register_controller@ngdesk.com" };
		List<String> value = Arrays.asList(emailIds);
		Optional<List<Map<String, Object>>> optionalRestrictedUsers = moduleEntryRepository
				.findAllEntriesByFieldName(value, "EMAIL_ADDRESS", "Users_" + companyId);

		boolean present = optionalRestrictedUsers.get().stream()
				.filter(user -> entryIds.contains(user.get("_id").toString())).findAny().isPresent();
		if (present) {
			throw new ForbiddenException("FORBIDDEN");
		}
	}

	public void validateCurrentUser(List<String> entryIds, String userId) {
		boolean isCurrentUser = entryIds.stream().filter(objectId -> objectId.equals(userId)).findAny().isPresent();

		if (isCurrentUser) {
			throw new ForbiddenException("CANNOT_DELETE_YOURSELF");
		}
	}

	public void validateDefaultTeams(List<String> dataIds, String companyId) {
		Page<Role> roles = rolesRepository.findAll(PageRequest.of(0, 999), "roles_" + companyId);
		List<String> values = new ArrayList<String>();
		roles.forEach(role -> values.add(role.getName()));

		values.add("Global");
		Optional<List<Map<String, Object>>> optionalTeams = moduleEntryRepository.findAllEntriesByFieldName(values,
				"NAME", "Teams_" + companyId);

		List<Map<String, Object>> allTeams = optionalTeams.get();

		Optional<Map<String, Object>> optionalTeam = allTeams.stream()
				.filter(team -> dataIds.contains(team.get("_id").toString())).findAny();

		if (!optionalTeam.isEmpty()) {
			throw new ForbiddenException("FORBIDDEN");
		}
	}

	public void validateEntryIds(List<String> objectIds, String collectionName) {
		for (String objectId : objectIds) {
			if (!ObjectId.isValid(objectId)) {
				throw new BadRequestException("INVALID_ENTRY", null);
			}

			Optional<Map<String, Object>> optionalEntry = moduleEntryRepository.findEntryById(objectId, collectionName);
			if (optionalEntry.isEmpty()) {
				throw new BadRequestException("INVALID_ENTRY", null);
			}
		}
	}

	public boolean isValidConditions(List<SLAConditions> conditions, Module module, Map<String, Object> entry,
			Map<String, Object> existingEntry) {

		ObjectMapper mapper = new ObjectMapper();
		if (conditions.size() == 0) {
			return true;
		}

		Map<String, String> fieldNames = new HashMap<String, String>();
		List<String> dateFieldIds = new ArrayList<String>();
		String discussionFieldId = null;

		for (ModuleField field : module.getFields()) {
			fieldNames.put(field.getFieldId(), field.getName());

			if (field.getDataType().getDisplay().equalsIgnoreCase("Discussion")) {
				discussionFieldId = field.getFieldId();
			} else if (field.getDataType().getDisplay().equalsIgnoreCase("Date/Time")
					|| field.getDataType().getDisplay().equalsIgnoreCase("Date")
					|| field.getDataType().getDisplay().equalsIgnoreCase("Time")) {
				dateFieldIds.add(field.getFieldId());
			}

		}

		List<Boolean> all = new ArrayList<Boolean>();
		List<Boolean> any = new ArrayList<Boolean>();

		if (entry != null) {

			Set<String> entryKeys = entry.keySet();

			for (SLAConditions condition : conditions) {
				String requirementType;
				requirementType = condition.getRequirementType();

				String fieldId = condition.getCondition();
				String operator = condition.getOperator();
				String value = condition.getConditionValue();
				String fieldName = fieldNames.get(fieldId);

				if (!entryKeys.contains(fieldName)) {
					if (operator.equalsIgnoreCase("DOES_NOT_EXIST")) {
						if (entry.get(fieldName) == null) {
							if (requirementType.equals("All")) {
								all.add(true);
							} else if (requirementType.equals("Any")) {
								any.add(true);
							}

						} else {
							if (requirementType.equals("All")) {
								all.add(false);
							} else if (requirementType.equals("Any")) {
								any.add(false);
							}
						}

					} else {
						return false;
					}
				}
				try {

					if (operator.equalsIgnoreCase("EQUALS_TO") || operator.equalsIgnoreCase("IS")) {
						if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
							if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
								List<DiscussionMessage> messages = mapper.readValue(
										mapper.writeValueAsString(entry.get(fieldName)), mapper.getTypeFactory()
												.constructCollectionType(List.class, DiscussionMessage.class));

								if (requirementType.equals("All")) {
									boolean isValid = true;
									for (DiscussionMessage message : messages) {
										if (!message.getMessage().equals(value)) {
											isValid = false;
											break;
										}
									}
									all.add(isValid);
								} else if (requirementType.equals("Any")) {
									boolean isValid = false;
									for (DiscussionMessage message : messages) {
										if (message.getMessage().equals(value)) {
											isValid = true;
											break;
										}
									}
									any.add(isValid);
								}
							}
						} else {
							if (!value.equals(entry.get(fieldName).toString())) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}
					} else if (operator.equalsIgnoreCase("NOT_EQUALS_TO")) {

						if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
							if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
								List<DiscussionMessage> messages = mapper.readValue(
										mapper.writeValueAsString(entry.get(fieldName)), mapper.getTypeFactory()
												.constructCollectionType(List.class, DiscussionMessage.class));

								if (requirementType.equals("All")) {
									boolean isValid = true;
									for (DiscussionMessage message : messages) {
										if (message.getMessage().equals(value)) {
											isValid = false;
											break;
										}
									}
									all.add(isValid);
								} else if (requirementType.equals("Any")) {
									boolean isValid = false;
									for (DiscussionMessage message : messages) {
										if (!message.getMessage().equals(value)) {
											isValid = true;
											break;
										}
									}
									any.add(isValid);
								}
							}
						} else {
							if (value.equals(entry.get(fieldName).toString())) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}

					} else if (operator.equalsIgnoreCase("contains")) {

						if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
							if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
								List<DiscussionMessage> messages = mapper.readValue(
										mapper.writeValueAsString(entry.get(fieldName)), mapper.getTypeFactory()
												.constructCollectionType(List.class, DiscussionMessage.class));

								if (requirementType.equals("All")) {
									boolean isValid = true;
									for (DiscussionMessage message : messages) {
										if (!message.getMessage().contains(value)) {
											isValid = false;
											break;
										}
									}
									all.add(isValid);
								} else if (requirementType.equals("Any")) {
									boolean isValid = false;
									for (DiscussionMessage message : messages) {
										if (message.getMessage().contains(value)) {
											isValid = true;
											break;
										}
									}
									any.add(isValid);
								}
							}
						} else {
							if (!entry.get(fieldName).toString().contains(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}
					} else if (operator.equalsIgnoreCase("DOES_NOT_CONTAIN")) {

						if (discussionFieldId != null && fieldId.equals(discussionFieldId)) {
							if (entry.containsKey(fieldName) && entry.get(fieldName) != null) {
								List<DiscussionMessage> messages = mapper.readValue(
										mapper.writeValueAsString(entry.get(fieldName)), mapper.getTypeFactory()
												.constructCollectionType(List.class, DiscussionMessage.class));

								if (requirementType.equals("All")) {
									boolean isValid = true;
									for (DiscussionMessage message : messages) {
										if (message.getMessage().contains(value)) {
											isValid = false;
											break;
										}
									}
									all.add(isValid);
								} else if (requirementType.equals("Any")) {
									boolean isValid = false;
									for (DiscussionMessage message : messages) {
										if (!message.getMessage().contains(value)) {
											isValid = true;
											break;
										}
									}
									any.add(isValid);
								}
							}
						} else {
							if (entry.get(fieldName).toString().contains(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}
					} else if (operator.equalsIgnoreCase("REGEX")) {
						Pattern pattern = Pattern.compile(value);
						Matcher matcher = pattern.matcher(entry.get(fieldName).toString());
						if (!matcher.find()) {
							if (requirementType.equals("All")) {
								all.add(false);
							} else if (requirementType.equals("Any")) {
								any.add(false);
							}
						} else {
							if (requirementType.equals("All")) {
								all.add(true);
							} else if (requirementType.equals("Any")) {
								any.add(true);
							}
						}
					} else if (operator.equalsIgnoreCase("LESS_THAN")) {
						if (!dateFieldIds.contains(fieldId)) {
							if (Integer.parseInt(entry.get(fieldName).toString()) >= Integer.parseInt(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						} else if (dateFieldIds.contains(fieldId)) {
							Instant instant = null;
							instant = Instant.parse(value);
							Date dateValue = (Date) Date.from(instant);

							if (dateValue.after(new Date())) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}
					} else if (operator.equalsIgnoreCase("LENGTH_IS_LESS_THAN")) {
						if (!dateFieldIds.contains(fieldId)) {
							if ((entry.get(fieldName).toString()).length() >= Integer.parseInt(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						} else if (dateFieldIds.contains(fieldId)) {
							Instant instant = null;
							instant = Instant.parse(value);
							Date dateValue = (Date) Date.from(instant);

							if (dateValue.after(new Date())) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}
					} else if (operator.equalsIgnoreCase("LENGTH_IS_GREATER_THAN")) {
						if (!dateFieldIds.contains(fieldId)) {
							if ((entry.get(fieldName).toString()).length() < Integer.parseInt(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						} else if (dateFieldIds.contains(fieldId)) {
							Instant instant = null;
							instant = Instant.parse(value);

							Date dateValue = (Date) Date.from(instant);

							if (dateValue.before(new Date())) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}
					} else if (operator.equalsIgnoreCase("GREATER_THAN")) {
						if (!dateFieldIds.contains(fieldId)) {
							if (Integer.parseInt(entry.get(fieldName).toString()) < Integer.parseInt(value)) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						} else if (dateFieldIds.contains(fieldId)) {
							Instant instant = null;
							instant = Instant.parse(value);

							Date dateValue = (Date) Date.from(instant);

							if (dateValue.before(new Date())) {
								if (requirementType.equals("All")) {
									all.add(false);
								} else if (requirementType.equals("Any")) {
									any.add(false);
								}
							} else {
								if (requirementType.equals("All")) {
									all.add(true);
								} else if (requirementType.equals("Any")) {
									any.add(true);
								}
							}
						}
					} else if (operator.equalsIgnoreCase("EXISTS")) {
						if (entry.get(fieldName) != null) {
							if (requirementType.equals("All")) {
								all.add(true);
							} else if (requirementType.equals("Any")) {
								any.add(true);
							}
						} else {
							if (requirementType.equals("All")) {
								all.add(false);
							} else if (requirementType.equals("Any")) {
								any.add(false);
							}
						}

					}
				} catch (Exception e) {
					if (requirementType.equals("All")) {
						all.add(false);
					} else if (requirementType.equals("Any")) {
						any.add(false);
					}
				}

			}

			boolean allValue = true;
			for (boolean booleanValue : all) {
				if (!booleanValue) {
					allValue = false;
					break;
				}
			}
			boolean anyValue = true;
			for (boolean booleanValue : any) {
				if (!booleanValue) {
					anyValue = false;
				} else {
					anyValue = true;
					break;
				}
			}
			return (allValue && anyValue);
		} else {
			return false;
		}
	}

	public boolean validateBusinessRulesForSla(SLA sla, String companyId) {
		if (sla.getBusinessRules() == null) {
			return true;
		}
		SLABusinessRules slaBuisnessRules = sla.getBusinessRules();
		if (sla.getIsRestricted()) {

			String timeZone = "UTC";
			Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");

			if (optionalCompany.get().getTimezone() != null) {
				timeZone = optionalCompany.get().getTimezone();
			}

			// GET CURRENT HOURS AND MINUTES
			ZonedDateTime now = ZonedDateTime.now();
			now = now.toInstant().atZone(ZoneId.of(timeZone));
			int currentHour = now.getHour();
			int currentMinutes = now.getMinute();

			// GET CURRENT DAY OF THE WEEK
			Calendar calendar = Calendar.getInstance();
			int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

			for (SLARestriction restriction : slaBuisnessRules.getRestrictions()) {
				return isValidBusinessHour(restriction, currentHour, currentMinutes, currentDay, sla);
			}
		} else {
			return true;
		}
		return false;
	}

	private boolean isValidBusinessHour(SLARestriction restriction, int currentHour, int currentMinutes, int currentDay,
			SLA sla) {
		try {
			String restrictionType = sla.getBusinessRules().getRestrictionType();
			String startTime = restriction.getStartTime();
			String endTime = restriction.getEndTime();

			Calendar cal = Calendar.getInstance();
			DateFormat dateFormat = new SimpleDateFormat("HH:mm");

			cal.setTime(dateFormat.parse(startTime));
			int startHour = cal.get(Calendar.HOUR_OF_DAY);

			cal.setTime(dateFormat.parse(endTime));

			int endHour = cal.get(Calendar.HOUR_OF_DAY);
			int endMinute = cal.get(Calendar.MINUTE);

			switch (restrictionType) {

			case "Day":
				if (currentHour >= startHour && currentHour <= endHour) {
					if ((currentHour == endHour) && (currentMinutes > endMinute)) {
						return false;
					}
					return true;
				}
				if (endHour <= startHour) {
					if (endHour == startHour) {
						endHour = 24 + endHour;
						int timeWindow = endHour - startHour;
						if (currentHour < timeWindow && currentMinutes < endMinute) {
							return true;
						}
						return false;
					}
					if (currentHour <= startHour && currentHour > endHour) {
						if ((currentHour == endHour) && (currentMinutes > endMinute)) {
							return false;
						}
						return true;
					}
					endHour = 24 + endHour;
					int timeWindow = endHour - startHour;
					if (currentHour < timeWindow) {
						return true;
					}
				}
				break;

			case "Week":
				String startDay = restriction.getStartDay();
				String endDay = restriction.getEndDay();
				int start = getDay(startDay);
				int end = getDay(endDay);
				if (start > end || (start == end && currentHour > endHour)) {
					if (currentDay <= end) {
						currentDay = currentDay + 7;
					}
					end = end + 7;
				}

				if (currentDay == start && currentDay == end) {
					if (startHour <= currentHour && currentHour < endHour) {
						if ((currentHour == endHour) && (currentMinutes > endMinute)) {
							return false;
						}
						return true;
					}
				} else if (currentDay >= start && currentDay <= end) {
					if (currentDay >= 7 && currentDay == end && start + 7 == end
							&& (currentHour < endHour || currentHour >= startHour)) {
						if ((currentHour == endHour) && (currentMinutes > endMinute)) {
							return false;
						}
						return true;
					} else if (currentDay == start) {
						if (currentHour >= startHour) {
							if ((currentHour == endHour) && (currentMinutes > endMinute)) {
								return false;
							}
							return true;
						}
					} else if (currentDay == end) {
						if (currentHour < endHour) {
							return true;
						}
					} else {
						return true;
					}
				}
				break;
			default:
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			String[] vars = { sla.getName() };
			throw new BadRequestException("SLA_BUSINESS_RULES_DATE_TIME_FORMAT_INVALID", vars);
		}
		return false;

	}

	public void validateAlternatePrimaryKeys(Module module, Map<String, Object> entry, String collectionName) {
		List<ModuleField> alternatePrimaryFields = new ArrayList<ModuleField>();

		if (module.getAlternatePrimaryKeys() != null && module.getAlternatePrimaryKeys().size() > 0) {
			module.getAlternatePrimaryKeys().forEach(primaryField -> {
				ModuleField alternatePrimaryField = module.getFields().stream()
						.filter(field -> field.getFieldId().equals(primaryField)).findFirst().orElse(null);
				if (alternatePrimaryField != null) {
					alternatePrimaryFields.add(alternatePrimaryField);
				}
			});
		}

		if (alternatePrimaryFields.size() > 0) {
			Map<String, Object> keyValuePairs = new HashMap<String, Object>();
			for (ModuleField alternatePrimaryField : alternatePrimaryFields) {
				String alternatePrimaryFieldName = alternatePrimaryField.getName();
				if (entry.get(alternatePrimaryFieldName) != null) {
					keyValuePairs.put(alternatePrimaryFieldName, entry.get(alternatePrimaryFieldName));
				}
			}
			if (keyValuePairs.size() > 0) {
				Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
						.findEntryByAlternatePrimaryKeys(keyValuePairs, collectionName);
				if (optionalEntry.isPresent()) {

					String fieldNames = "";
					int i = 0;
					for (String key : keyValuePairs.keySet()) {
						fieldNames += key + " ";
						i++;
					}

					String[] vars = { fieldNames };
					throw new BadRequestException("ALTERNATE_PRIMARY_KEY_EXISTS", vars);
				}
			}
		}

	}

	public void validateUniqueField(Module module, Map<String, Object> entry, String collectionName, String requestType,
			String companyId) {
		String moduleName = module.getName();
		String dataId = entry.get("_id").toString();

		List<ModuleField> uniqueFields = module.getFields().stream()
				.filter(field -> ((field.getUnique() != null) && (field.getUnique().equals(true))))
				.collect(Collectors.toList());

		uniqueFields.forEach(field -> {
			String isUnique = evaluateIsUnique(field.getName(), entry.get(field.getName()), moduleName, dataId,
					requestType, companyId);
			if (isUnique == "false") {
				String[] vars = { field.getDisplayLabel() };
				throw new BadRequestException("FIELD_VALUE_NOT_UNIQUE", vars);
			}
		});
	}

	private int getDay(String day) {
		if (day.equals("Sun")) {
			return 0;
		}
		if (day.equals("Mon")) {
			return 1;
		}
		if (day.equals("Tue")) {
			return 2;
		}
		if (day.equals("Wed")) {
			return 3;
		}
		if (day.equals("Thu")) {
			return 4;
		}
		if (day.equals("Fri")) {
			return 5;
		}
		if (day.equals("Sat")) {
			return 6;
		}
		return -1;
	}

	public void validateRestrictedContacts(List<String> entryIds) {
		String[] firstName = { "Ghost", "System", "Probe", "Register_controller" };
		List<String> value = Arrays.asList(firstName);
		Optional<List<Map<String, Object>>> optionalRestrictedContacts = moduleEntryRepository
				.findAllEntriesByFieldName(value, "FIRST_NAME",
						"Contacts_" + authManager.getUserDetails().getCompanyId());

		boolean present = optionalRestrictedContacts.get().stream()
				.filter(contact -> entryIds.contains(contact.get("_id").toString())).findAny().isPresent();
		if (present) {
			throw new BadRequestException("DO_NOT_DELETE_RESTRICTED_CONTACTS", null);
		}
	}

	public void validateCurrentContacts(List<String> entryIds) {

		Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository.findEntryById(
				authManager.getUserDetails().getUserId(), "Users_" + authManager.getUserDetails().getCompanyId());
		if (!optionalUserEntry.isPresent()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Map<String, Object> userEntry = optionalUserEntry.get();
		boolean isCurrentContact = entryIds.stream().filter(objectId -> objectId.equals(userEntry.get("CONTACT")))
				.findAny().isPresent();

		if (isCurrentContact) {
			throw new BadRequestException("CANNOT_DELETE_YOURSELF", null);
		}
	}

	/*
	 * public void isValidBaseCondition(String fieldId) {
	 * 
	 * Optional<Module> optionalModule =
	 * modulesRepository.findIdbyModuleName("Software Installation", "modules_" +
	 * authManager.getUserDetails().getCompanyId());
	 * 
	 * List<ModuleField> fields = optionalModule.get().getFields().stream().filter(
	 * moduleField -> moduleField.getName().equals("VERSION") ||
	 * moduleField.getName().equals("EDITION")) .collect(Collectors.toList());
	 * List<String> fieldIds = new ArrayList<String>(); for (ModuleField field :
	 * fields) { fieldIds.add(field.getFieldId()); } if
	 * (!fieldIds.contains(fieldId)) { throw new
	 * BadRequestException("INVALID_CONDITION_FIELD", null); } }
	 */

	public void isValidVersionEditionFieldId(String fieldId, String fieldName) {

		Optional<Module> optionalModule = modulesRepository.findIdbyModuleName("Software Installation",
				"modules_" + authManager.getUserDetails().getCompanyId());

		List<ModuleField> fields = optionalModule.get().getFields().stream().filter(
				moduleField -> moduleField.getName().equals("VERSION") || moduleField.getName().equals("EDITION"))
				.collect(Collectors.toList());
		Map<String, Object> conditionFields = new HashMap<String, Object>();
		for (ModuleField field : fields) {
			conditionFields.put(field.getName(), field.getFieldId());
		}
		if (!((fieldName.equals("VERSION_CONDITION") && conditionFields.get("VERSION").toString().equals(fieldId))
				|| (fieldName.equals("EDITION_CONDITION")
						&& conditionFields.get("EDITION").toString().equals(fieldId)))) {
			throw new BadRequestException("INVALID_CONDITION_FIELD_SELECTION", null);

		}
	}

	public void isValidOperator(String operator) {

		List<String> operators = List.of("Is", "Is Anything", "Starts With");
		if (!operators.contains(operator)) {
			throw new BadRequestException("INVALID_OPERATOR", null);
		}
	}

}
