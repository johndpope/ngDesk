package com.ngdesk.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.SetOperators.SetIntersection;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;

import io.netty.handler.codec.json.JsonObjectDecoder;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

@Component
public class Validator {
	private static ScriptEngine engine;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	private final Logger log = LoggerFactory.getLogger(Validator.class);

	public class Validate extends AbstractJSObject {
		public Validate() {
		}

		@Override
		public boolean isFunction() {
			return true;
		}

		@Override
		public Object call(Object thiz, Object... args) {
			try {

				JSONArray validations = new JSONArray();

				JSONObject inputMessage = new JSONObject(args[0].toString());
				String companyId = args[1].toString();
				String moduleName = args[2].toString();
				String requestType = args[3].toString();
				String role = args[4].toString();

				validations = getModuleValidations(moduleName, companyId, requestType, role);

				String dummyCondition = "1 == 1";

				String javascript = "";
				javascript += System.lineSeparator();
				javascript += "var inputMessage = " + inputMessage + ";";
				javascript += System.lineSeparator();
				for (int i = 0; i < validations.length(); i++) {
					JSONArray array = new JSONArray();
					String error = null;
					ScriptEngineManager factory = new ScriptEngineManager();
					ScriptEngine engine = factory.getEngineByName("JavaScript");
					engine.eval(javascript);

					String expressionAnd = "";
					String expressionOr = "";

					JSONObject customObject = validations.getJSONObject(i);

					JSONArray validationArray = customObject.getJSONArray("VALIDATIONS");
					for (int j = 0; j < validationArray.length(); j++) {
						JSONObject validation = validationArray.getJSONObject(j);
						JSONObject object = new JSONObject();

						String variable = validation.getString("CONDITION");
						String operator = validation.getString("OPERATOR");
						String requirementType = validation.getString("REQUIREMENT_TYPE");
						String value = validation.getString("CONDITION_VALUE");

						String statement = null;
						statement = generateStatement(operator, variable, inputMessage, value, companyId, moduleName,
								requestType);
						if (requirementType.equalsIgnoreCase("All")) {
							expressionAnd += " && " + statement;
							Boolean result = (Boolean) engine.eval(statement);
							if (!result) {
								// UPDATING VALUE OF RELATIONSHIP TYPE FIELD TO DISPLAY IN ERROR
								String updatedValue = value;
								try {
									MongoCollection<Document> moduleCollection = mongoTemplate
											.getCollection("modules_" + companyId);
									Document moduleDoc = moduleCollection.find(Filters.eq("NAME", moduleName)).first();
									if (moduleDoc != null) {
										List<Document> fields = (List<Document>) moduleDoc.get("FIELDS");
										outer: for (Document field : fields) {
											Document dataType = (Document) field.get("DATA_TYPE");

											if (dataType.getString("DISPLAY").equals("Relationship")) {
												String primaryDisplayField = field.getString("PRIMARY_DISPLAY_FIELD");

												Document relationModule = moduleCollection.find(
														Filters.eq("_id", new ObjectId(field.getString("MODULE"))))
														.first();
												List<Document> relationModuleFields = (List<Document>) relationModule
														.get("FIELDS");
												String relationModuleName = relationModule.getString("NAME");

												MongoCollection<Document> relationEntries = mongoTemplate
														.getCollection(relationModuleName + "_" + companyId);
												Document entryDoc = relationEntries
														.find(Filters.eq("_id", new ObjectId(value))).first();

												if (entryDoc != null) {
													for (Document relationField : relationModuleFields) {
														if (relationField.getString("FIELD_ID")
																.equals(primaryDisplayField)) {
															updatedValue = entryDoc
																	.getString(relationField.getString("NAME"));
															break outer;
														}
													}
												}
											}
										}
									}
								} catch (IllegalArgumentException e) {
									updatedValue = value;
								}

								error = generateInvalidErrorMessage(
										determineFieldParameters(variable, "DISPLAY_LABEL", companyId, moduleName),
										operator, updatedValue, null);
								return error;
							}
						} else if (requirementType.equalsIgnoreCase("Any")) {
							dummyCondition = " 1 == 2";
							expressionOr += "|| " + statement;
							Boolean result = (Boolean) engine.eval(statement);
							if (!result) {
								object.put("FIELD",
										determineFieldParameters(variable, "DISPLAY_LABEL", companyId, moduleName));
								object.put("OPERATOR", operator);
								object.put("VALUE", value);
								array.put(object);
							}
						}
					}

					String expression = "1 == 1 " + expressionAnd + " && (" + dummyCondition + " " + expressionOr + ")";
					Boolean result = (Boolean) engine.eval(expression);
					if (!result) {
						error = generateInvalidErrorMessage(null, null, null, array);
						return error;
					}
				}

				return "";

			} catch (Exception e) {
				e.printStackTrace();
				throw new InternalErrorException("INTERNAL_ERROR");
			}
		}

		private JSONArray getModuleValidations(String moduleName, String companyId, String requestType, String role) {
			JSONArray validations = new JSONArray();
			try {

				log.trace("Enter Validator.getModuleValidations() moduleName: " + moduleName + ", companyId: "
						+ companyId);
				// Retrieving a collection
				String collectionName = "modules_" + companyId;
				MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

				Document module = collection.find(Filters.eq("NAME", moduleName)).first();

				if (module.containsKey("VALIDATIONS")) {
					List<Document> moduleValidations = (List<Document>) module.get("VALIDATIONS");
					for (Document validation : moduleValidations) {

						JSONObject validationJson = new JSONObject(validation.toJson());

						if (requestType.equalsIgnoreCase("POST")
								&& (validation.getString("TYPE").equalsIgnoreCase("CREATE")
										|| validation.getString("TYPE").equalsIgnoreCase("CREATE_OR_UPDATE"))) {
							List<String> roles = (List<String>) validation.get("ROLES");
							if (roles.contains(role)) {
								JSONObject data = new JSONObject();
								data.put("NAME", validation.getString("NAME"));
								data.put("VALIDATIONS", validationJson.getJSONArray("VALIDATIONS"));
								validations.put(data);
							}
						} else if (requestType.equalsIgnoreCase("PUT")
								&& (validation.getString("TYPE").equalsIgnoreCase("UPDATE")
										|| validation.getString("TYPE").equalsIgnoreCase("CREATE_OR_UPDATE"))) {
							List<String> roles = (List<String>) validation.get("ROLES");

							if (roles.contains(role)) {
								JSONObject data = new JSONObject();
								data.put("NAME", validation.getString("NAME"));
								data.put("VALIDATIONS", validationJson.getJSONArray("VALIDATIONS"));
								validations.put(data);
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				throw new InternalErrorException("INTERNAL_ERROR");
			}
			log.trace("Exit Validator.getModuleValidations() moduleName: " + moduleName + ", companyId: " + companyId);
			return validations;
		}

	}

	public String isValid(JSONObject inputMessage, String companyId, String moduleName, String requestType,
			String role) {
		try {
			NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
			engine = factory.getScriptEngine(new String[] { "-strict", "--no-java", "--no-syntax-extensions" });
			engine.put("validate", new Validate());

			// FIX FOR .TOSTRING GETTING RID OF ESCAPES
			String inputMessageString = StringEscapeUtils.escapeJson(inputMessage.toString());
			inputMessageString = inputMessageString.replaceAll("'", "\\\\'");
			String errorMessage = (String) engine.eval("validate('" + inputMessageString + "','" + companyId + "','"
					+ moduleName + "','" + requestType + "', '" + role + "')");
			return errorMessage;

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	private String isUnique(String variable, String value, String companyId, String moduleName, String dataId,
			String requestType) {

		try {
			log.trace("Enter Validator.isUnique() variable: " + variable + ", value: " + value + ", companyId: "
					+ companyId);
			String collectionName = moduleName.replaceAll("\\s+", "_") + "_" + companyId;
			// Retrieving a collection
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document document = null;
			if (requestType.equalsIgnoreCase("POST")) {
				document = collection.find(Filters.and(Filters.eq(variable, value), Filters.eq("DELETED", false),
						Filters.eq("EFFECTIVE_TO", null))).first();

			} else if (requestType.equalsIgnoreCase("PUT")) {
				document = collection
						.find(Filters.and(Filters.eq(variable, value), Filters.ne("_id", new ObjectId(dataId)),
								Filters.eq("DELETED", false), Filters.eq("EFFECTIVE_TO", null)))
						.first();
			}
			if (document == null) {
				log.trace("Exit Validator.isUnique() variable: " + variable + ", value: " + value + ", companyId: "
						+ companyId);
				return "true";
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		log.trace(
				"Exit Validator.isUnique() variable: " + variable + ", value: " + value + ", companyId: " + companyId);
		return "false";
	}

	public boolean isValidBaseTypes(JSONObject inputMessage, String companyId, String moduleName, String requestType) {

		try {
			log.trace("Enter Validator.isValidBaseTypes() moduleName: " + moduleName + ", companyId: " + companyId);
			String collectionName = "modules_" + companyId;

			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			Document module = collection.find(Filters.eq("NAME", moduleName)).first();

			Map<String, Document> moduleFields = new HashMap<String, Document>();
			if (module != null) {
				List<Document> fields = (List<Document>) module.get("FIELDS");
				for (Document field : fields) {
					moduleFields.put(field.getString("NAME"), field);
				}

				Iterator iterator = inputMessage.keys();

				while (iterator.hasNext()) {
					String fieldName = iterator.next().toString();
					Document modulefield = moduleFields.get(fieldName);
					log.debug("FieldName: " + fieldName);
					if (inputMessage.has(fieldName) && !inputMessage.isNull(fieldName)
							&& inputMessage.get(fieldName).toString().length() > 0) {

						if (!fieldName.equals("DATA_ID") && !fieldName.equals("DATE_CREATED")
								&& !fieldName.equals("DATE_UPDATED") && !fieldName.equals("LAST_UPDATED_BY")
								&& !fieldName.equals("LAST_UPDATED_ON") && !fieldName.equals("CREATED_BY")
								&& !fieldName.equals("SOURCE_TYPE") && !fieldName.equals("META_DATA")) {

							if (moduleFields.containsKey(fieldName)) {
								JSONObject field = new JSONObject(moduleFields.get(fieldName).toJson());
								String dataType = field.getJSONObject("DATA_TYPE").getString("DISPLAY");

								if (dataType.equals("Email")) {
									String value = inputMessage.getString(fieldName);
									if (!EmailValidator.getInstance().isValid(value)) {
										throw new BadRequestException("EMAIL_INVALID");
									}
								} else if (dataType.equals("Phone")) {
									JSONObject phone = inputMessage.getJSONObject(fieldName);
									if (phone.has("COUNTRY_CODE") && phone.has("DIAL_CODE") && phone.has("PHONE_NUMBER")
											&& phone.has("COUNTRY_FLAG")) {

										if (!phone.isNull("PHONE_NUMBER")) {

											if (phone.getString("PHONE_NUMBER").length() > 0) {
												if (phone.isNull("COUNTRY_CODE")
														|| phone.getString("COUNTRY_CODE").length() == 0) {

													throw new BadRequestException("PHONE_INVALID");
												} else if (phone.isNull("COUNTRY_FLAG")
														|| phone.getString("COUNTRY_FLAG").length() == 0) {

													throw new BadRequestException("PHONE_INVALID");
												} else if (phone.isNull("DIAL_CODE")
														|| phone.getString("DIAL_CODE").length() == 0) {

													throw new BadRequestException("PHONE_INVALID");
												}

												PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
												String phoneNumberE164Format = phone.getString("DIAL_CODE")
														+ phone.getString("PHONE_NUMBER");
												PhoneNumber phoneNumberProto = phoneUtil.parse(phoneNumberE164Format,
														null);
												boolean isValid = phoneUtil.isValidNumber(phoneNumberProto);

												if (!isValid) {
													throw new BadRequestException("PHONE_INVALID");
												}
											} else if ((phone.getString("PHONE_NUMBER").length() == 0
													|| phone.isNull("PHONE_NUMBER")) && field.getBoolean("REQUIRED")) {
												throw new BadRequestException("PHONE_INVALID");
											}

										} else if (phone.isNull("PHONE_NUMBER") && field.getBoolean("REQUIRED")) {
											throw new BadRequestException("PHONE_INVALID");
										}
									} else {
										throw new BadRequestException("PHONE_INVALID");
									}

								} else if (dataType.equals("Number")) {
									try {
										int value = inputMessage.getInt(fieldName);
									} catch (JSONException e) {
										e.printStackTrace();
										throw new BadRequestException("INVALID_FIELD_VALUE");
									}
								} else if (dataType.equals("ID")) {
									try {
										try {
											UUID.fromString(inputMessage.getString(fieldName));
										} catch (Exception e) {
											e.printStackTrace();
											throw new BadRequestException("INVALID_ID_TYPE");
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}

								} else if (dataType.equals("Country")) {
									if (!Global.countriesMap.containsValue(inputMessage.getString(fieldName))) {
										throw new BadRequestException("INVALID_COUNTRY");
									}
								} else if (dataType.equals("Percent")) {
									try {
										Double value = Double.parseDouble(inputMessage.get(fieldName).toString());
									} catch (NumberFormatException e) {
										throw new BadRequestException("INVALID_NUMBER");
									}
								} else if (dataType.equals("Currency")) {
									try {
										Double value = Double.parseDouble(inputMessage.get(fieldName).toString());
										if (BigDecimal.valueOf(value).scale() > 3) {
											throw new BadRequestException("INVALID_NUMBER");
										}
									} catch (NumberFormatException e) {
										throw new BadRequestException("INVALID_NUMBER");
									}

								} else if (dataType.equals("Checkbox")) {
									Boolean value = inputMessage.getBoolean(fieldName);
								} else if (dataType.equals("URL")) {
									String[] schemes = { "http", "https" }; // DEFAULT schemes = "http", "https", "ftp"
									UrlValidator urlValidator = new UrlValidator(schemes);
									String value = inputMessage.getString(fieldName);
									if (!urlValidator.isValid(value)) {
										throw new BadRequestException("INVALID_URL");
									}
								} else if (dataType.equals("Date/Time") || dataType.equals("Date")
										|| dataType.equals("Time")) {
									String value = inputMessage.getString(fieldName);
									if (global.verifyDateTime(value) == null) {
										throw new BadRequestException("INVALID_DATE_TIME");
									}
								} else if (dataType.equalsIgnoreCase("Picklist")) {
									JSONArray picklistValues = field.getJSONArray("PICKLIST_VALUES");
									String value = inputMessage.getString(fieldName);
									boolean valueExists = false;

									for (int i = 0; i < picklistValues.length(); i++) {
										if (picklistValues.getString(i).equals(value)) {
											valueExists = true;
											break;
										}
									}

									if (!valueExists) {
										throw new BadRequestException("VALUE_MISSING_IN_PICKLIST");
									}

								} else if (dataType.equalsIgnoreCase("Discussion")) {
									List<String> keys = new ArrayList<String>();
									keys.add("MESSAGE");
									keys.add("DATE_CREATED");
									keys.add("MODULE");
									keys.add("SENDER");
									keys.add("MESSAGE_ID");
									keys.add("ATTACHMENTS");
									keys.add("ENTRY_ID");
									keys.add("CHAT_ID");
									keys.add("COMPANY_UUID");
									keys.add("MESSAGE_TYPE");

									JSONArray messages = inputMessage.getJSONArray(fieldName);
									for (int i = 0; i < messages.length(); i++) {
										JSONObject message = messages.getJSONObject(i);

										for (String key : message.keySet()) {
											if (!keys.contains(key)) {
												throw new BadRequestException("DISCUSSION_MESSAGE_KEY_MISSING");
											}
										}
									}

								} else if (dataType.equals("Relationship")) {
									String relationshipType = field.getString("RELATIONSHIP_TYPE");
									String relationModuleId = field.getString("MODULE");

									Document relationModule = collection
											.find(Filters.eq("_id", new ObjectId(relationModuleId))).first();

									if (relationModule == null) {
										throw new BadRequestException("INVALID_RELATIONSHIP");
									}

									String relationModuleName = relationModule.getString("NAME");

									MongoCollection<Document> entriesCollection = mongoTemplate
											.getCollection(relationModuleName + "_" + companyId);

									if (relationshipType.equals("One to One")
											|| relationshipType.equals("Many to One")) {

										if (field.getBoolean("REQUIRED") || inputMessage.has(fieldName)) {

											String value = inputMessage.getString(fieldName);

											if (!new ObjectId().isValid(value)) {
												throw new BadRequestException("INVALID_ENTRY_ID");
											}

											Document entry = entriesCollection
													.find(Filters.eq("_id", new ObjectId(value))).first();
											if (entry == null) {
												throw new BadRequestException("ENTRY_NOT_EXIST");
											}
										}
									} else if (relationshipType.equals("One to Many")) {
										if (inputMessage.get(fieldName) != null) {
											throw new BadRequestException("ONE_TO_MANY_ERROR");
										}
									} else if (relationshipType.equals("Many to Many")) {

										JSONArray values = inputMessage.getJSONArray(fieldName);
										List<String> validValues = new ArrayList<String>();
										for (int i = 0; i < values.length(); i++) {
											String value = values.get(i).toString();
											if (validValues.contains(value)) {
												throw new BadRequestException("DUPLICATE_ENTRY_ID");
											}
											validValues.add(value);
											if (!new ObjectId().isValid(value)) {
												throw new BadRequestException("INVALID_ENTRY_ID");
											}
											Document entry = entriesCollection
													.find(Filters.and(Filters.eq("_id", new ObjectId(value)),
															Filters.eq("DELETED", false)))
													.first();
											if (entry == null) {
												throw new BadRequestException("ENTRIES_NOT_EXIST");
											}
										}
									}

								}
								// VALIDATION FOR CHRONOMETER FIELD VALUE
								else if (dataType.equals("Chronometer")) {
									Document fieldDoc = moduleFields.get(fieldName);
									String displayLabel = fieldDoc.getString("DISPLAY_LABEL");
									String regex = "^(\\d+mo)?(\\d+w)?(\\d+d)?(\\d+h)?(\\d+m)?$";
									String value = inputMessage.get(fieldName).toString();
									String valueWithoutminus = value.replaceAll("\\-", "");
									String valueWithoutSpace = valueWithoutminus.replaceAll("\\s+", "");
									Pattern p = Pattern.compile(regex, Pattern.DOTALL);
									Matcher m = p.matcher(valueWithoutSpace);

									if (!m.find()) {
										throw new BadRequestException(displayLabel + "-PATTERN_NOT_VALID");
									}

								} else if (dataType.equals("Zipcode")) {

									String zipcode = inputMessage.getString(fieldName);
									if (zipcode.length() < 5 || zipcode.length() > 10) {
										throw new BadRequestException("INVALID_ZIPCODE");
									}
								}
							} else {
								throw new BadRequestException(fieldName + "-FIELDS_NOT_EXIST_IN_MODULE");
							}
						}
					} else if (inputMessage.has(fieldName) && modulefield.getBoolean("REQUIRED")) {
						String displayLabel = modulefield.getString("DISPLAY_LABEL");
						throw new BadRequestException(displayLabel + "-IS_REQUIRED");
					}
				}
			} else {
				throw new BadRequestException("MODULE_DOES_NOT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		} catch (NumberParseException e) {
			e.printStackTrace();
			throw new BadRequestException("PHONE_INVALID");
		}

		log.trace("Exit Validator.isValidBaseTypes() moduleName: " + moduleName + ", companyId: " + companyId);
		return true;
	}

	private String generateStatement(String operator, String var, JSONObject input, String value, String companyId,
			String moduleName, String requestType) {
		try {
			String dataType = determineFieldParameters(var, "DATA_TYPE", companyId, moduleName);
			String fieldName = determineFieldParameters(var, "NAME", companyId, moduleName);
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
				return "inputMessage.hasOwnProperty('" + fieldName + "') &&" + "inputMessage." + fieldName
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
				if (input.has("DATA_ID")) {
					dataId = input.getString("DATA_ID");
				}
				statement = isUnique(fieldName, input.getString(fieldName), companyId, moduleName, dataId, requestType);
			}
			return statement;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public String determineFieldParameters(String fieldId, String key, String companyId, String moduleName) {
		try {
			String collectionName = "modules_" + companyId;
			MongoCollection<Document> moduleCollection = mongoTemplate.getCollection(collectionName);

			Document module = moduleCollection.find(Filters.eq("NAME", moduleName)).first();

			List<Document> fields = (List<Document>) module.get("FIELDS");
			for (Document field : fields) {
				if (field.getString("FIELD_ID").equals(fieldId)) {
					if (!key.equalsIgnoreCase("DATA_TYPE")) {

						return field.getString(key);
					} else {
						Document dataType = (Document) field.get("DATA_TYPE");
						return dataType.getString("BACKEND");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String generateInvalidErrorMessage(String field, String operator, String value, JSONArray array) {
		String invalidErrorMessage = "";
		if (array == null) {
			if (operator.equals("EQUALS_TO")) {
				invalidErrorMessage = field + " should be " + value;
			} else if (operator.equals("NOT_EQUALS_TO")) {
				invalidErrorMessage = field + " should not be " + value;
			} else if (operator.equals("GREATER_THAN")) {
				invalidErrorMessage = field + " should be greater than " + value;
			} else if (operator.equals("LESS_THAN")) {
				invalidErrorMessage = field + " should be less than " + value;
			} else if (operator.equals("IS")) {
				invalidErrorMessage = field + " should be " + value;
			} else if (operator.equals("REGEX")) {
				invalidErrorMessage = field + " should validate for " + value + " regular expression";
			} else if (operator.equals("EXISTS")) {
				invalidErrorMessage = field + " is required";
			} else if (operator.equals("DOES_NOT_EXIST")) {
				invalidErrorMessage = field + " should not be present";
			} else if (operator.equals("CONTAINS")) {
				invalidErrorMessage = field + " should contain " + value;
			} else if (operator.equals("DOES_NOT_CONTAIN")) {
				invalidErrorMessage = field + " should not contain " + value;
			} else if (operator.equals("IS_UNIQUE")) {
				invalidErrorMessage = field + " should be unique";
			} else if (operator.contentEquals("LENGTH_IS_GREATER_THAN")) {
				invalidErrorMessage = field + " length should be greater than " + value;
			} else if (operator.contentEquals("LENGTH_IS_LESS_THAN")) {
				invalidErrorMessage = field + " length should be less than " + value;
			}
			return invalidErrorMessage;
		} else {
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				field = object.getString("FIELD");
				operator = object.getString("OPERATOR");
				value = object.getString("VALUE");
				invalidErrorMessage += generateInvalidErrorMessage(field, operator, value, null) + " or \n";
				if (i == array.length() - 1) {
					invalidErrorMessage = invalidErrorMessage.substring(0, invalidErrorMessage.lastIndexOf("or"));
				}
			}
			return invalidErrorMessage;
		}
	}
}
