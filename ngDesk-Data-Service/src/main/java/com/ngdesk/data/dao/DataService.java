package com.ngdesk.data.dao;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.apache.http.util.Asserts;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.graalvm.polyglot.Context;
import org.jsoup.Jsoup;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.mail.EmailService;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.elastic.ElasticMessage;
import com.ngdesk.data.modules.dao.Condition;
import com.ngdesk.data.modules.dao.ListFormulaField;
import com.ngdesk.data.modules.dao.ListLayout;
import com.ngdesk.data.modules.dao.ListMobileLayout;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.sla.dao.SLA;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.attachments.AttachmentsRepository;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;
import com.ngdesk.repositories.sla.SLARepository;
import com.ngdesk.tesseract.dao.OCRPayload;

@Component
public class DataService {

	private final Logger log = LoggerFactory.getLogger(DataService.class);

	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	AttachmentsRepository attachmentsRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	Global global;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	RolesService rolesService;

	@Autowired
	RedissonClient client;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	Validator validator;

	@Value("${manager.host}")
	String managerHost;

	@Value("${elastic.host}")
	private String elasticHost;

	@Autowired
	EmailService emailService;

//	@Autowired
//	Prometheus prometheus;

	@Autowired
	RedisTemplate<String, WorkflowPayload> redisTemplate;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	RestHighLevelClient elasticClient;

	@Autowired
	MergeService mergeService;

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	SlaService slaService;

	@Autowired
	FormulaService formulaService;

	@Autowired
	SLARepository slaRepository;

	public Map<String, Object> addFieldsWithDefaultValue(List<ModuleField> fields, Map<String, Object> entry) {
		ObjectMapper mapper = new ObjectMapper();
		fields.forEach(field -> {
			try {
				if (entry.get(field.getName()) == null && field.getDefaultValue() != null
						&& !field.getDefaultValue().isBlank()) {

					if (field.getDataType().getDisplay().equalsIgnoreCase("phone")) {
						Map<String, String> phoneNumber = mapper.readValue(field.getDefaultValue(), Map.class);
						entry.put(field.getName(), phoneNumber);
					} else {

						if (field.getDataType().getBackend().equalsIgnoreCase("array")) {
							entry.put(field.getName(), Arrays.asList(field.getDefaultValue().split("\\s*,\\s*")));
						} else if (field.getDataType().getBackend().equalsIgnoreCase("boolean")) {
							entry.put(field.getName(), Boolean.parseBoolean(field.getDefaultValue()));
						} else {
							Pattern pattern = Pattern.compile("\\{\\{(.*)\\}\\}");
							Matcher matcher = pattern.matcher(field.getDefaultValue());
							if (matcher.find()) {
								if (matcher.group(1).equals("CURRENT_USER")) {
									field.setDefaultValue(field.getDefaultValue().replaceAll("\\{\\{CURRENT_USER\\}\\}",
											authManager.getUserDetails().getAttributes().get("CONTACT").toString()));
								} else if (matcher.group(1).equals("CURRENT_CONTACT")) {
									field.setDefaultValue(field.getDefaultValue().replaceAll(
											"\\{\\{CURRENT_CONTACT\\}\\}",
											authManager.getUserDetails().getAttributes().get("CONTACT").toString()));
								}
							}
							entry.put(field.getName(), field.getDefaultValue());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return entry;
	}

	public String getFormulaFieldValue(Module module, Map<String, Object> entry, ModuleField formulaField,
			String value) {

		List<ModuleField> moduleFields = module.getFields();
		List<String> customOperators = List.of("BLANK_SPACE");
		List<String> stringDisplayDataTypes = List.of("Text", "Street 1", "Street 2", "City", "Country", "State",
				"Zipcode", "Chronometer", "Email", "Picklist");
		List<String> numericDisplayDataTypes = List.of("Auto Number", "Currency", "Number", "Currency Exchange",
				"List Formula");

		try {
			value = getFormulaRecursively(formulaField, moduleFields, value, entry);
			String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z0-9\\.\\-]+)\\}\\}";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(value);
			while (matcher.find()) {
				String path = matcher.group(1).split("(?i)inputMessage\\.")[1];
				String[] fields = path.split("\\.");
				String generatedValue;
				if (customOperators.contains(fields[0])) {
					generatedValue = handleCustomOperators(fields[0]);
					value = value.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}", generatedValue);
				} else {
					generatedValue = formulaService.getValue(module, entry, path);
					String fieldDisplayType;

					if (fields.length == 1) {
						ModuleField moduleField = moduleFields.stream()
								.filter(field -> field.getName().equals(fields[0])).findFirst().orElse(null);
						fieldDisplayType = moduleField.getDataType().getDisplay();
					} else {
						List<Module> modules = modulesRepository
								.getAllModules("modules_" + authManager.getUserDetails().getCompanyId());
						fieldDisplayType = getDisplayTypeByPath(path, module, modules);
					}
					if (stringDisplayDataTypes.contains(fieldDisplayType)) {
						if (generatedValue.equals("") || generatedValue == null) {
							value = value.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}", "''");
						}
						value = value.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}", "'" + generatedValue + "'");
					} else if (numericDisplayDataTypes.contains(fieldDisplayType)) {
						if (generatedValue.equals("") || generatedValue == null) {
							value = value.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}", "0");
						}
						value = value.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}", generatedValue);
					}
				}
			}
			Context jsContext = Context.create("js");
			var result = jsContext.eval("js", value);
			value = result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public String handleCustomOperators(String section) {
		if (section.equals("BLANK_SPACE")) {
			return "' '";
		}
		return "";
	}

	public String getDisplayTypeByPath(String path, Module module, List<Module> modules) {
		String[] fields = path.split("\\.");
		List<ModuleField> moduleFields = module.getFields();
		ModuleField moduleField = moduleFields.stream().filter(field -> field.getName().equals(fields[0])).findFirst()
				.orElse(null);

		Module relatedModule = modules.stream().filter(rmodule -> rmodule.getModuleId().equals(moduleField.getModule()))
				.findFirst().orElse(null);
		ModuleField relatedModuleField = relatedModule.getFields().stream()
				.filter(field -> field.getName().equals(fields[1])).findFirst().orElse(null);
		if (fields.length == 2) {
			return relatedModuleField.getDataType().getDisplay();
		} else if (fields.length > 2) {
			return getDisplayTypeByPath(path.split(fields[0] + "\\.")[1], relatedModule, modules);
		}
		return null;
	}

	public String getFormulaRecursively(ModuleField formulaField, List<ModuleField> moduleFields, String formula,
			Map<String, Object> entry) {
		try {
			List<String> customOperators = List.of("BLANK_SPACE");
			String reg = "\\{\\{(?i)(inputMessage[_a-zA-Z0-9\\.\\-]+)\\}\\}";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(formula);
			while (matcher.find()) {
				String path = matcher.group(1).split("(?i)inputMessage\\.")[1];

				String[] fields = path.split("\\.");

				if (!customOperators.contains(fields[0])) {
					ModuleField moduleField = moduleFields.stream().filter(field -> field.getName().equals(fields[0]))
							.findFirst().orElse(null);
					if (moduleField != null && moduleField.getDataType().getDisplay().equals("Formula")) {
						String updatedFormula = getFormulaRecursively(moduleField, moduleFields,
								moduleField.getFormula(), entry);
						formula = formula.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}",
								"(" + updatedFormula + ")");
					} else if (moduleField.getDataType().getDisplay().equals("List Formula")) {
						List<ListFormulaField> listFormulas = moduleField.getListFormula();
						String updatedListFormula = "";
						List<String> listOfFormulas = new ArrayList<String>();
						for (ListFormulaField listFormula : listFormulas) {
							if (formulaService.listFormulaAdded(entry, moduleField, listFormula.getFormulaName())) {
								listOfFormulas.add("(" + listFormula.getFormula() + ")");
							}
						}
						updatedListFormula = String.join("+", listOfFormulas);
						String updatedFormula = getFormulaRecursively(moduleField, moduleFields, updatedListFormula,
								entry);
						if (!updatedListFormula.isBlank()) {
							formula = formula.replaceAll("\\{\\{" + matcher.group(1) + "\\}\\}",
									"(" + updatedFormula + ")");

						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return formula;
	}

	public Map<String, Object> addInternalFields(Module module, Map<String, Object> entry, String userId,
			String companyId) {
		Date currentTimestamp = new Date();
		entry.put("DATE_CREATED", currentTimestamp);
		entry.put("DATE_UPDATED", currentTimestamp);
		entry.put("EFFECTIVE_FROM", currentTimestamp);
		entry.put("CREATED_BY", userId);
		entry.put("LAST_UPDATED_BY", userId);
		entry.put("DELETED", false);
		entry.put("_id", getNewObjectId(module.getName(), companyId));
		return entry;
	}

	public Map<String, Object> formatDateAndTimeField(Map<String, Object> entry, Module module) {
		String[] fieldNames = { "DATE_CREATED", "DATE_UPDATED", "EFFECTIVE_FROM", "EFFECTIVE_TO" };

		List<String> fieldsToIgnore = new ArrayList<String>();
		fieldsToIgnore.addAll(Arrays.asList(fieldNames));

		List<ModuleField> payloadFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Date/Time")
						|| field.getDataType().getDisplay().equals("Date")
						|| field.getDataType().getDisplay().equals("Time"))
				.collect(Collectors.toList());

		List<String> slaFieldNames = slaService.generateSlaFieldNames(module.getModuleId(),
				authManager.getUserDetails().getCompanyId());
		if (slaFieldNames.size() > 0 && slaFieldNames != null) {
			fieldsToIgnore.addAll(slaFieldNames);
		}
		payloadFields = payloadFields.stream().filter(field -> !fieldsToIgnore.contains(field.getName()))
				.collect(Collectors.toList());
		payloadFields.forEach(field -> {
			String fieldName = field.getName();
			if ((entry.get(fieldName) != null)) {
				String dateString = entry.get(fieldName).toString();
				try {
					Date date = new Date();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSX");
					date = df.parse(dateString);
					entry.put(fieldName, date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		return entry;
	}

	public Map<String, Object> buildEntryPayload(Module module, Map<String, Object> entry) {
		Map<String, Object> payload = new HashMap<String, Object>();

		List<ModuleField> payloadFields = module.getFields().stream()
				.filter(field -> !(field.getDataType().getDisplay().equals("Relationship")
						&& field.getRelationshipType().equals("One to Many"))
						&& !field.getDataType().getDisplay().equalsIgnoreCase("Discussion")
						&& !field.getDataType().getDisplay().equalsIgnoreCase("Aggregate"))
				.collect(Collectors.toList());

		String[] ignoreFields = { "DATE_CREATED", "DATE_UPDATED", "EFFECTIVE_FROM", "CREATED_BY", "LAST_UPDATED_BY",
				"DELETED", "_id", "WORKFLOW_STAGES", "EFFECTIVE_TO" };
		List<String> fieldsToIgnore = Arrays.asList(ignoreFields);

		payloadFields = payloadFields.stream().filter(field -> !fieldsToIgnore.contains(field.getName()))
				.collect(Collectors.toList());

		payloadFields.forEach(field -> {
			if (entry.containsKey(field.getName())) {
				payload.put(field.getName(), entry.get(field.getName()));
			}
		});
		return payload;
	}

	public Map<String, Object> addAutoNumberFields(Module module, Map<String, Object> entry) {
		module.getFields().forEach(field -> {
			if (field.getDataType().getDisplay().equals("Auto Number")) {

				int count = moduleEntryRepository.getCountOfCollectionForAutonumber(
						moduleService.getCollectionName(module.getName(), authManager.getUserDetails().getCompanyId()));

				if (count == 0) {
					if (field.getAutoNumberStartingNumber() != null) {
						entry.put(field.getName(), field.getAutoNumberStartingNumber());
					} else {
						entry.put(field.getName(), 1);
					}
				} else {
					entry.put(field.getName(), moduleEntryRepository.getNextAutoNumber(field.getName(), moduleService
							.getCollectionName(module.getName(), authManager.getUserDetails().getCompanyId())));
				}
			}
		});
		return entry;
	}

	public boolean requiredFieldsCheckRequired(Map<String, Object> entry) {
		if (entry.get("SOURCE_TYPE") != null) {
			String sourceType = entry.get("SOURCE_TYPE").toString();
			if (sourceType.equals("email") || sourceType.equals("sms") || sourceType.equals("forms")) {
				return false;
			}
		}

		return true;
	}

	public Map<String, Object> formatPayload(Module module, Map<String, Object> entry) {
		String[] dataTypes = { "Email", "Chronometer" };
		List<String> dataTypesToFilter = Arrays.asList(dataTypes);

		List<ModuleField> fields = module.getFields().stream()
				.filter(field -> dataTypesToFilter.contains(field.getDataType().getDisplay()))
				.collect(Collectors.toList());

		fields.forEach(field -> {
			String fieldName = field.getName();
			String displayDataType = field.getDataType().getDisplay();

			if (entry.get(fieldName) != null) {

				if (displayDataType.equals("Email")) {
					entry.put(fieldName, entry.get(fieldName).toString().toLowerCase());
				} else if (displayDataType.equals("Chronometer")) {
					String value = entry.get(fieldName).toString();
					value = value.replaceAll("\\s+", "");
					if (value.isBlank() || value.charAt(0) == '-') {
						entry.put(fieldName, 0);
					} else {
						entry.put(fieldName, getChronometerValueInMinutes(value));
					}
				}
			}
		});
		return entry;
	}

	private DiscussionMessage removeInLineImages(DiscussionMessage message, Module module, String dataId) {
		String htmlContent = message.getMessage();

		String regex = "<img.*?src=\"(.*?)\".*?>";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(htmlContent);
		while (matcher.find()) {
			String base64Regex = "data:image.*?;base64.*";
			Pattern base64Pattern = Pattern.compile(base64Regex, Pattern.DOTALL);
			Matcher base64Matcher = base64Pattern.matcher(matcher.group(1));
			if (base64Matcher.find()) {
				MessageAttachment attachment = new MessageAttachment();
				attachment.setFile(base64Matcher.group(0));
				attachment.setFileExtension(getFileExtensionFromBase64(base64Matcher.group(0)));
				attachment.setFileName(getFileNameFromImageSrc(matcher.group(0), attachment.getFileExtension()));
				attachment.setAttachmentUuid(UUID.randomUUID().toString());

				message.getAttachments().add(attachment);
				String attachmentUrl = global.getBaseRestUrl(authManager.getUserDetails().getCompanySubdomain())
						+ "attachments?attachment_uuid=" + attachment.getAttachmentUuid() + "&entry_id=" + dataId
						+ "&message_id=" + message.getMessageId() + "&module_id=" + module.getModuleId();
				message.setMessage(message.getMessage().replace(matcher.group(1), attachmentUrl));
			}
		}

		return message;
	}

	private String getFileExtensionFromBase64(String base64) {
		String fileExtenstionRegex = "image/(.*);";
		Pattern fileExtensionPattern = Pattern.compile(fileExtenstionRegex, Pattern.DOTALL);
		Matcher fileExtensionMatcher = fileExtensionPattern.matcher(base64);
		if (fileExtensionMatcher.find()) {
			return fileExtensionMatcher.group(1);
		}
		return "jpeg";
	}

	private String getFileNameFromImageSrc(String imageSrc, String extension) {
		String titleRegex = "title=\"(.*?)\"";
		Pattern pattern = Pattern.compile(titleRegex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(imageSrc);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "File_" + UUID.randomUUID().toString() + "." + extension;
		}
	}

	public int getChronometerValueInMinutes(String input) {

		try {
			Pattern periodPattern = Pattern.compile("(\\d+)(mo|w|d|m|h)");
			Matcher matcher = periodPattern.matcher(input);

			int chronometerValueInSecond = 0;

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

			if (!periodPattern.matcher(input).find() && chronometerValueInSecond == 0) {

				chronometerValueInSecond = chronometerValueInSecond + Integer.parseInt(input);

			}

			return chronometerValueInSecond;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public Map<String, Object> setInheritanceValue(Module module, Map<String, Object> entry) {
		List<ModuleField> manyToOneFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
						&& field.getRelationshipType().equalsIgnoreCase("many to one")
						&& field.getInheritanceMapping() != null)
				.collect(Collectors.toList());

		for (ModuleField manyToOneField : manyToOneFields) {
			if (entry.get(manyToOneField.getName()) != null
					&& !entry.get(manyToOneField.getName()).toString().isBlank()) {
				continue;
			}
			String[] vars = { manyToOneField.getDisplayLabel() };

			if (entry.get(manyToOneField.getName()) == null) {
				continue;
			}

			if (manyToOneField.getModule() == null && manyToOneField.getModule().isBlank()
					&& !ObjectId.isValid(manyToOneField.getModule())) {
				throw new BadRequestException("INHERITANCE_REFERENCE_MODULE_INVALID", vars);
			}
			Optional<Module> optionalModule = modulesRepository.findById(manyToOneField.getModule(),
					"modules_" + authManager.getUserDetails().getCompanyId());

			if (optionalModule.isEmpty()) {
				throw new BadRequestException("INHERITANCE_REFERENCE_MODULE_INVALID", vars);
			}

			Module relationshipModule = optionalModule.get();

			if (entry.get(manyToOneField.getName()).toString().isBlank()
					&& !ObjectId.isValid(entry.get(manyToOneField.getName()).toString())) {
				throw new BadRequestException("INHERITANCE_REFERENCE_ENTRY_INVALID", vars);
			}

			String collectionName = moduleService.getCollectionName(relationshipModule.getName(),
					authManager.getUserDetails().getCompanyId());

			Optional<Map<String, Object>> optionalEntry = moduleEntryRepository
					.findEntryById(entry.get(manyToOneField.getName()).toString(), collectionName);

			if (optionalEntry.isEmpty()) {
				throw new BadRequestException("INHERITANCE_REFERENCE_ENTRY_INVALID", vars);
			}

			Map<String, Object> relatedEntry = optionalEntry.get();

			Map<String, String> moduleFieldMap = new HashMap<String, String>();
			module.getFields().forEach(field -> moduleFieldMap.put(field.getFieldId(), field.getName()));

			Map<String, String> relatedModuleFieldMap = new HashMap<String, String>();
			relationshipModule.getFields()
					.forEach(field -> relatedModuleFieldMap.put(field.getFieldId(), field.getName()));

			manyToOneField.getInheritanceMapping().forEach((key, value) -> {
				entry.put(moduleFieldMap.get(value), relatedEntry.get(relatedModuleFieldMap.get(key)));
			});

		}

		return entry;
	}

	public String hashAttachment(String file) {
		String hashedPassword = "";
		if (file.isBlank()) {
			return "";
		}
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(file.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			hashedPassword = bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}

		return hashedPassword;
	}

	public ObjectId getNewObjectId(String moduleName, String companyId) {
		ObjectId objectId = null;
		while (objectId == null) {
			objectId = new ObjectId();
			Optional<Map<String, Object>> entry = moduleEntryRepository.findById(objectId.toString(),
					moduleService.getCollectionName(moduleName, companyId));
			if (entry.isPresent()) {
				objectId = null;
			}
		}
		return objectId;
	}

	public Map<String, Object> buildPutEntryPayload(Module module, Map<String, Object> entry,
			Map<String, Object> existingEntry) {
		List<ModuleField> payloadFields = module.getFields().stream()
				.filter(field -> !(field.getDataType().getDisplay().equals("Relationship")
						&& field.getRelationshipType().equals("One to Many"))
						&& !field.getDataType().getDisplay().equalsIgnoreCase("Discussion")
						&& !field.getDataType().getDisplay().equalsIgnoreCase("Aggregate")
						&& !field.getDataType().getDisplay().equalsIgnoreCase("Auto Number")
						&& ((field.getNotEditable() != null) && (!field.getNotEditable()))
						&& ((entry.get(field.getName()) != null)
								|| (field.getDataType().getDisplay().equalsIgnoreCase("Chronometer"))
								|| (field.getDataType().getDisplay().equalsIgnoreCase("Receipt Capture"))))
				.collect(Collectors.toList());

		String[] ignoreFields = { "DATE_CREATED", "DATE_UPDATED", "EFFECTIVE_FROM", "CREATED_BY", "LAST_UPDATED_BY",
				"DELETED", "_id", "SOURCE_TYPE", "CHANNEL", "DATA_ID", "WORKFLOW_STAGES", "EFFECTIVE_TO" };

		List<String> fieldsToIgnore = new ArrayList<String>();
		fieldsToIgnore.addAll(Arrays.asList(ignoreFields));

		List<String> slaFieldNames = slaService.generateSlaFieldNames(module.getModuleId(),
				authManager.getUserDetails().getCompanyId());

		if (slaFieldNames.size() > 0 && slaFieldNames != null) {
			fieldsToIgnore.addAll(slaFieldNames);
		}

		payloadFields = payloadFields.stream().filter(field -> !fieldsToIgnore.contains(field.getName()))
				.collect(Collectors.toList());

		payloadFields.forEach(field -> {
			String fieldName = field.getName();

			if (field.getDataType().getDisplay().equalsIgnoreCase("Chronometer")) {
				if (entry.get(fieldName) == null || entry.get(fieldName).toString().isBlank()) {
					existingEntry.put(fieldName, "0m");
				}
			}

			boolean isChanged = false;
			if (!existingEntry.containsKey(fieldName)) {
				isChanged = true;
			} else if (entry.get(fieldName) != null && !entry.get(fieldName).equals(existingEntry.get(fieldName))) {
				isChanged = true;
			}

			if (isChanged) {
				if (field.getDataType().getDisplay().equalsIgnoreCase("Email")) {
					existingEntry.put(fieldName, entry.get(fieldName).toString().toLowerCase());
				}
				existingEntry.put(fieldName, entry.get(fieldName));
			}

		});

		return existingEntry;
	}

	public Map<String, Object> updateInternalFields(Module module, Map<String, Object> entry) {
		entry.put("DATE_UPDATED", new Date());
		entry.put("LAST_UPDATED_BY", authManager.getUserDetails().getUserId());

		return entry;
	}

	public Map<String, Object> formatChronometer(Module module, Map<String, Object> payload) {

		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());
		Optional<Map<String, Object>> optionalEntry = moduleEntryRepository.findById(payload.get("_id").toString(),
				collectionName);
		Map<String, Object> existingEntry = optionalEntry.get();

		List<ModuleField> fields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Chronometer")
						&& payload.containsKey(field.getName()))
				.collect(Collectors.toList());

		fields.forEach(field -> {

			String value = payload.get(field.getName()).toString();

			value = value.replaceAll("\\s+", "");

			if (value.isBlank()) {
				value = "0";
			}

			int existingValue = 0;
			if (existingEntry.containsKey(field.getName()) && existingEntry.get(field.getName()) != null) {

				existingValue = getChronometerValueInMinutes(existingEntry.get(field.getName()).toString());

			}
			if (existingValue == 0 && value.charAt(0) == '-') {

				payload.put(field.getName(), 0);
			} else if (value.charAt(0) == '-') {

				payload.put(field.getName(), existingValue - getChronometerValueInMinutes(value));
			} else {

				payload.put(field.getName(), existingValue + getChronometerValueInMinutes(value));

			}
		});

		return payload;
	}

	public Map<String, Object> formatDiscussion(Module module, Map<String, Object> entry, Map<String, Object> payload) {

		ObjectMapper mapper = new ObjectMapper();

		Optional<ModuleField> optionalDiscussionField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Discussion")
						&& entry.containsKey(field.getName()))
				.findAny();

		if (optionalDiscussionField.isEmpty()) {
			return payload;
		}

		ModuleField discussionField = optionalDiscussionField.get();

		String[] vars = { discussionField.getName() };
		List<DiscussionMessage> messages;
		List<DiscussionMessage> existingMessages = new ArrayList<DiscussionMessage>();
		try {
			messages = mapper.readValue(mapper.writeValueAsString(entry.get(discussionField.getName().toString())),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));

			if (payload.get(discussionField.getName()) != null) {

				existingMessages = mapper.readValue(mapper.writeValueAsString(payload.get(discussionField.getName())),
						mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));

			}

			if (messages.size() > 1) {

				throw new BadRequestException("DISCUSSION_MESSAGE_INVALID", vars);
			}

			if (messages.size() == 0) {
				return payload;
			}

			for (DiscussionMessage message : messages) {
				try {
					message.setMessage(StringEscapeUtils.unescapeJava(message.getMessage()));
				} catch (Exception e) {
					// org.apache.commons.lang.exception.NestableRuntimeException: Unable to parse
					// unicode value: /</s
					// drive(unicode"\\u")/3/folders/13zQT394PriNJvFGdtMDMbsMm1W8DMKsW - : When its
					// coming from UI
				}
				message.setMessageId(UUID.randomUUID().toString());
				message.setDateCreated(new Date());

				if (message.getMessageType() == null || message.getMessageType().isBlank()) {
					message.setMessageType("MESSAGE");
				}
				Sender sender = new Sender(authManager.getUserDetails().getFirstName(),
						authManager.getUserDetails().getLastName(), authManager.getUserDetails().getUserUuid(),
						authManager.getUserDetails().getRole());
				message.setSender(sender);
				if (message.getAttachments() == null) {
					message.setAttachments(new ArrayList<MessageAttachment>());
				}
				message = mapCidImages(message, module, entry);
				message = removeInLineImages(message, module, payload.get("_id").toString());

				message = postAttachment(message, entry, module);
				try {
					String messageEscape = escapeUtilsAndUnicode(message.getMessage());

					message.setMessage(messageEscape);
				} catch (Exception e) {
					// ADDED BECAUSE IT THROWS Unable to parse unicode value for below URL when
					// coming from manager
					// drive(unicode"\\u")/3/folders/13zQT394PriNJvFGdtMDMbsMm1W8DMKsW 
				}

				org.jsoup.nodes.Document html = Jsoup.parse(message.getMessage());
				html.select("script, .hidden").remove();

				message.setMessage(html.toString().replaceAll("&amp;", "&"));

				existingMessages.add(message);
			}
			payload.put(discussionField.getName(), existingMessages);
			return payload;

		} catch (JsonMappingException e) {
			throw new BadRequestException("BASE_TYPE_DISCUSSION_FORMAT_INVALID", vars);
		} catch (JsonProcessingException e) {
			throw new BadRequestException("BASE_TYPE_DISCUSSION_FORMAT_INVALID", vars);
		}
	}

	private DiscussionMessage mapCidImages(DiscussionMessage message, Module module, Map<String, Object> entry) {

		String cidRegex = "src=\"(cid:(.*?))\"";

		Pattern cidPattern = Pattern.compile(cidRegex);
		Matcher cidMatcher = cidPattern.matcher(message.getMessage());
		while (cidMatcher.find()) {
			String attachmentUuid = cidMatcher.group(2).strip();
			MessageAttachment attachment = message.getAttachments().stream()
					.filter(mAttachment -> (mAttachment.getAttachmentUuid() != null
							&& mAttachment.getAttachmentUuid().equals(attachmentUuid)))
					.findFirst().orElse(null);
			if (attachment != null) {
				String attachmentUrl = global.getBaseRestUrl(authManager.getUserDetails().getCompanySubdomain())
						+ "attachments?attachment_uuid=" + attachmentUuid + "&entry_id=" + entry.get("_id").toString()
						+ "&message_id=" + message.getMessageId() + "&module_id=" + module.getModuleId() + "\"";
				message.setMessage(message.getMessage().replace(cidMatcher.group(1), attachmentUrl));
			}
		}
		return message;
	}

	private String escapeUtilsAndUnicode(String messageString) {
		// IF NOT A UNICODE CHARACTER, REPLACING BACKSLASH WITH FRONTSLASH
		Pattern backslashUPattern = Pattern.compile("(\\\\)u[a-zA-Z]+");
		Matcher backslashUMatcher = backslashUPattern.matcher(messageString);
		while (backslashUMatcher.find()) {
			messageString = messageString.replace(backslashUMatcher.group(0),
					backslashUMatcher.group(0).replace("\\", "/"));
		}
		Pattern backslashTPattern = Pattern.compile("(\\\\)t[a-zA-Z]+");
		Matcher backslashTMatcher = backslashTPattern.matcher(messageString);
		while (backslashTMatcher.find()) {
			messageString = messageString.replace(backslashTMatcher.group(0),
					backslashTMatcher.group(0).replace("\\", "///"));
		}

		// QUOTES ARE ESCAPED, AND GREEK CHARACTERS ARE CONVERTED TO UNICODE ESCAPES
		String escapedMessage = new UnicodeUnescaper().translate(messageString);

		escapedMessage = StringEscapeUtils.unescapeJava(escapedMessage).replaceAll("\n", "");

		// IF NOT A UNICODE CHARACTER, REPLACING FRONTSLASH WITH BACKSLASH
		Pattern frontslashUPattern = Pattern.compile("(\\/)u");
		Matcher frontslashUMatcher = frontslashUPattern.matcher(escapedMessage);
		while (frontslashUMatcher.find()) {
			escapedMessage = escapedMessage.replace(frontslashUMatcher.group(0),
					frontslashUMatcher.group(0).replace("/", "\\"));
		}
		Pattern frontslashTPattern = Pattern.compile("(\\///)t");
		Matcher frontslashTMatcher = frontslashTPattern.matcher(escapedMessage);
		while (frontslashTMatcher.find()) {
			escapedMessage = escapedMessage.replace(frontslashTMatcher.group(0),
					frontslashTMatcher.group(0).replace("///", "\\"));
		}

		// REPLACING DOUBLE SLASH WITH SINGLE SLASH
		Pattern doubleslashPattern = Pattern.compile("(\\\\\\\\)");
		Matcher doubleslashMatcher = doubleslashPattern.matcher(escapedMessage);
		while (doubleslashMatcher.find()) {
			escapedMessage = escapedMessage.replace(doubleslashMatcher.group(0),
					doubleslashMatcher.group(0).replace("\\\\", "\\"));
		}

		return escapedMessage;

	}

	private DiscussionMessage postAttachment(DiscussionMessage message, Map<String, Object> entry, Module module) {
		if (message.getAttachments() != null) {

			Map<String, String> attachmentsUuidMap = new HashMap<String, String>();
			message.getAttachments().forEach(attachment -> {
				if (attachment.getFileName() == null) {
					if (attachment.getFileExtension() == null) {
						attachment.setFileName("File_" + UUID.randomUUID().toString() + ".jpeg");
					} else {
						attachment.setFileName(
								"File_" + UUID.randomUUID().toString() + "." + attachment.getFileExtension());
					}
				}
				String hash = hashAttachment(attachment.getFile());
				Optional<Attachment> optionalAttachment = attachmentsRepository.findAttachmentByHash(hash,
						"attachments_" + authManager.getUserDetails().getCompanyId());

				if (optionalAttachment.isEmpty()) {

					String file = attachment.getFile();

					if (attachment.getAttachmentUuid() == null) {
						attachment.setAttachmentUuid(UUID.randomUUID().toString());
					}

					Pattern pattern = Pattern.compile(",(.*)", Pattern.DOTALL);
					Matcher matcher = pattern.matcher(file);
					if (matcher.find()) {
						file = matcher.group(1);

					}

					Attachment newAttachment = new Attachment(null, hash, file, attachment.getAttachmentUuid());
					attachmentsRepository.save(newAttachment,
							"attachments_" + authManager.getUserDetails().getCompanyId());
					attachment.setHash(hash);
				} else {

					attachment.setHash(optionalAttachment.get().getHash());
					if (attachment.getAttachmentUuid() != null) {
						message.setMessage(message.getMessage().replaceAll(attachment.getAttachmentUuid(),
								(optionalAttachment.get().getAttachmentUuid())));
					}
					attachment.setAttachmentUuid(optionalAttachment.get().getAttachmentUuid());
				}

				attachmentsUuidMap.put(attachment.getFileName(), attachment.getAttachmentUuid());
			});

			String htmlContent = message.getMessage();

			// HANDLING CID IMAGES
			String regex = "src=\"(cid:(.*?)@.*?)\">";
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(htmlContent);
			while (matcher.find()) {
				String output = matcher.group(1);
				String fileName = matcher.group(2);
				String attachmentUrl = global.getBaseRestUrl(authManager.getUserDetails().getCompanySubdomain())
						+ "attachments?attachment_uuid=" + attachmentsUuidMap.get(fileName) + "&entry_id="
						+ entry.get("_id").toString() + "&message_id=" + message.getMessageId() + "&module_id="
						+ module.getModuleId();
				htmlContent = htmlContent.replaceAll(output, attachmentUrl);
			}
			message.setMessage(htmlContent);
		}
		return message;
	}

	public void addToWorkflowQueue(WorkflowPayload workflowPayload) {
		try {
			log.debug("Publishing to manager");
			log.debug(new ObjectMapper().writeValueAsString(workflowPayload));

			rabbitTemplate.convertAndSend("execute-module-workflows", workflowPayload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void isEditableTeam(String dataId, String name) {
		Page<Role> roles = rolesRepository.findAll(PageRequest.of(0, 999),
				"roles_" + authManager.getUserDetails().getCompanyId());
		List<String> values = new ArrayList<String>();
		roles.forEach(role -> values.add(role.getName()));
		values.add("Global");
		values.add("Public");
		Optional<List<Map<String, Object>>> optionalTeams = moduleEntryRepository.findAllEntriesByFieldName(values,
				"NAME", "Teams_" + authManager.getUserDetails().getCompanyId());

		List<Map<String, Object>> allTeams = optionalTeams.get();

		Optional<Map<String, Object>> optionalTeam = allTeams.stream()
				.filter(team -> team.get("_id").toString().equals(dataId)).findAny();

		if (optionalTeam.isPresent()) {
			if (optionalTeam.get().get("NAME").equals("Global") || optionalTeam.get().get("NAME").equals("Public")) {
				throw new ForbiddenException("FORBIDDEN");
			}

			else if (!values.contains(name)) {

				throw new ForbiddenException("FORBIDDEN");
			}

		}
	}

	public void isEditableUser(String dataId) {
		List<String> values = new ArrayList<String>();
		values.add("ghost@ngdesk.com");
		values.add("system@ngdesk.com");
		values.add("probe@ngdesk.com");
		values.add("register_controller@ngdesk.com");
		Optional<List<Map<String, Object>>> optionalUsers = moduleEntryRepository.findAllEntriesByFieldName(values,
				"EMAIL_ADDRESS", "Users_" + authManager.getUserDetails().getCompanyId());

		Optional<Map<String, Object>> optionalUser = optionalUsers.get().stream()
				.filter(user -> user.get("_id").toString().equals(dataId)).findAny();
		if (optionalUser.isPresent()) {
			throw new ForbiddenException("FORBIDDEN");
		}
	}

	public void roleChangeCheck(Map<String, Object> entry, Map<String, Object> existingEntry) {
		if (!entry.get("ROLE").equals(existingEntry.get("ROLE"))) {
			if (rolesService.isSystemAdmin(existingEntry.get("ROLE").toString())) {
				long count = moduleEntryRepository.getSystemAdminCount(existingEntry.get("ROLE").toString(),
						"Users_" + authManager.getUserDetails().getCompanyId());
				if (count <= 1) {
					throw new BadRequestException("SYSTEM_ADMIN_REQUIRED", null);
				}
			}
		}
	}

	public void isCustomerEntry(Map<String, Object> entry) {
		if (!entry.get("USER_UUID").toString().equalsIgnoreCase(authManager.getUserDetails().getUserUuid())) {
			throw new ForbiddenException("FORBIDDEN");
		}
	}

	public boolean hasConditions(String filterFieldId, List<ModuleField> fields) {
		ModuleField filterField = fields.stream().filter(field -> field.getFieldId().equalsIgnoreCase(filterFieldId))
				.findAny().get();

		if (filterField.getDataFilter() != null && filterField.getDataFilter().getConditions().size() > 0) {
			return true;
		}

		return false;
	}

	public ListLayout getListLayout(Module module, String layoutId, String roleId) {
		Optional<ListLayout> optionalListLayout = module.getListLayouts().stream()
				.filter(layout -> layout.getListLayoutId().equals(layoutId) && layout.getRole().equals(roleId))
				.findAny();

		if (optionalListLayout.isEmpty()) {
			return null;
		} else {
			return optionalListLayout.get();
		}
	}

	public ListMobileLayout getListMobileLayout(Module module, String layoutId, String roleId) {

		if (module.getListMobileLayouts() == null) {
			return null;
		}

		Optional<ListMobileLayout> optionalListLayout = module.getListMobileLayouts().stream()
				.filter(layout -> layout.getListLayoutId().equals(layoutId) && layout.getRole().equals(roleId))
				.findAny();

		if (optionalListLayout.isEmpty()) {
			return null;
		} else {
			return optionalListLayout.get();
		}
	}

	public Map<String, Object> setSlas(Module module, Map<String, Object> payload, Map<String, Object> existingEntry,
			String type, String companyId) {

		String moduleId = module.getModuleId();

		List<SLA> filteredSlas = slaRepository.findAllSlaByModuleId(moduleId, companyId);
		if (filteredSlas != null) {

			for (SLA sla : filteredSlas) {

				if (validator.validateBusinessRulesForSla(sla, companyId)
						&& validator.isValidConditions(sla.getConditions(), module, payload, existingEntry)) {

					if (type.equalsIgnoreCase("POST")) {
						payload = slaService.postSlaCheckViolationAndAddToReddis(sla, module, payload);

					} else if (type.equals("PUT")) {

						payload = slaService.putSlaCheckViolationAndAddToReddis(sla, module, payload, existingEntry);

					}

				} else {
					payload = slaService.unsetSlaKeyIfExists(sla, payload);
				}
			}

		}
		return payload;
	}

	public void postTemporalEntry(Module module, Map<String, Object> payload) {
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());
		Optional<Map<String, Object>> optionalEntry = moduleEntryRepository.findEntryById(payload.get("_id").toString(),
				collectionName);

		if (optionalEntry.isPresent() && isEntryChanged(module, optionalEntry.get(), payload)) {

			Map<String, Object> existingEntry = optionalEntry.get();
			existingEntry.put("_id", getNewObjectId(module.getName(), authManager.getUserDetails().getCompanyId()));
			existingEntry.put("DATA_ID", payload.get("_id").toString());
			existingEntry.put("EFFECTIVE_TO", new Date());

			moduleEntryRepository.save(existingEntry, collectionName);
		}
	}

	public void postIntoElastic(Module module, String companyId, Map<String, Object> payload) {
		try {
			ElasticMessage message = new ElasticMessage(module.getModuleId(), companyId, payload);
			rabbitTemplate.convertAndSend("elastic-updates", message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addToNotifyQueue(NotificationMessage message) {
		redisTemplate.convertAndSend("module_notification", message);
	}

	public void addToDeleteEntryQueue(DeleteEntriesPayload deleteEntry) {
		try {
			log.debug("Publishing to delete queue");
			log.debug(new ObjectMapper().writeValueAsString(deleteEntry));

			rabbitTemplate.convertAndSend("delete-entries", deleteEntry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			RQueue<String> queue = client.getQueue("delete_child_entries", new JsonJacksonCodec());
			queue.add(new ObjectMapper().writeValueAsString(deleteEntry));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public void addMetadataEntryQueue(MetadataPayload metadataPayload) {
		try {
			log.debug(new ObjectMapper().writeValueAsString(metadataPayload));

			rabbitTemplate.convertAndSend("add-events", metadataPayload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isEntryChanged(Module module, Map<String, Object> existingEntry, Map<String, Object> payload) {
		if (module.getName().equalsIgnoreCase("Teams")) {
			return false;
		}

		List<ModuleField> payloadFields = module.getFields().stream()
				.filter(field -> !(field.getDataType().getDisplay().equals("Relationship")
						&& field.getRelationshipType().equals("One to Many"))
						&& !field.getDataType().getDisplay().equalsIgnoreCase("Discussion")
						&& !field.getDataType().getDisplay().equalsIgnoreCase("Auto Number")
						&& payload.containsKey(field.getName()))
				.collect(Collectors.toList());

		String[] ignoreFields = { "DATE_CREATED", "DATE_UPDATED", "EFFECTIVE_FROM", "CREATED_BY", "LAST_UPDATED_BY",
				"DELETED", "_id", "SOURCE_TYPE", "CHANNEL", "DATA_ID" };
		List<String> fieldsToIgnore = Arrays.asList(ignoreFields);

		payloadFields = payloadFields.stream().filter(field -> !fieldsToIgnore.contains(field.getName()))
				.collect(Collectors.toList());

		for (ModuleField field : payloadFields) {
			String fieldName = field.getName();

			if (!existingEntry.containsKey(fieldName)) {
				return true;
			} else if (existingEntry.get(fieldName) != null
					&& !payload.get(fieldName).equals(existingEntry.get(fieldName))) {
				return true;
			}
		}

		return false;
	}

	public List<String> getIdsFromGlobalSearch(String value, Module module, Set<String> teams,
			List<Condition> conditions) {
		List<String> ids = new ArrayList<String>();
		try {
			String companyId = authManager.getUserDetails().getCompanyId();

			boolean isFieldSearch = true;

			String moduleId = module.getModuleId();

			if (value.contains("~~")) {
				String[] keyValues = value.split("~~");
				if (keyValues.length > 0) {
					for (String keyValue : keyValues) {
						if (!keyValue.contains("=")) {
							isFieldSearch = false;
							break;
						} else if (keyValue.split("=").length != 2) {
							isFieldSearch = false;
							break;
						}
					}
				} else {
					isFieldSearch = false;
				}
			} else if (value.contains("=") && value.split("=").length == 2) {
				isFieldSearch = true;
			} else {
				isFieldSearch = false;
			}

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			// TODO: Adjust size based off of user input
			sourceBuilder.from(0);
			sourceBuilder.size(50);

			if (!isFieldSearch) {

				BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
				boolQueryBuilder.must().add(QueryBuilders.wildcardQuery("input", "*" + value + "*"));
				if (!module.getName().equals("Teams")) {
					boolQueryBuilder.must().add(QueryBuilders.termsQuery("TEAMS", teams));
				}
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId));
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

				sourceBuilder.query(boolQueryBuilder);
				SearchRequest searchRequest = new SearchRequest();
				searchRequest.indices("global_search");
				searchRequest.source(sourceBuilder);

				SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

				SearchHits hits = searchResponse.getHits();
				SearchHit[] searchHits = hits.getHits();
				for (SearchHit hit : searchHits) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();
					String dataId = sourceAsMap.get("ENTRY_ID").toString();
					ids.add(dataId);
				}

			} else {

				BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
				boolQueryBuilder1.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId));
				boolQueryBuilder1.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

				SearchSourceBuilder sourceBuilder1 = new SearchSourceBuilder();
				sourceBuilder1.query(boolQueryBuilder1);

				SearchRequest searchRequest1 = new SearchRequest();
				searchRequest1.indices("field_lookup");
				searchRequest1.source(sourceBuilder1);

				SearchResponse searchResponse1 = elasticClient.search(searchRequest1, RequestOptions.DEFAULT);

				SearchHits hits1 = searchResponse1.getHits();
				SearchHit[] searchHits1 = hits1.getHits();

				Map<String, String> fieldLookUpMap = new HashMap<String, String>();
				for (SearchHit hit : searchHits1) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();

					for (String key : sourceAsMap.keySet()) {
						if (!key.equals("MODULE_ID") && !key.equals("COMPANY_ID")) {
							fieldLookUpMap.put(sourceAsMap.get(key).toString(), key);
						}
					}
				}

				String[] keyValues = null;

				if (value.contains("~~")) {
					keyValues = value.split("~~");
				} else {
					keyValues = new String[1];
					keyValues[0] = value;
				}

				BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
				for (String keyValue : keyValues) {
					String key = keyValue.split("=")[0];

					if (fieldLookUpMap.containsKey(key)) {
						key = fieldLookUpMap.get(key).replaceAll("field", "value");
					}

					Object val = null;
					int index = 0;
					String range1 = null;
					String range2 = null;
					if (keyValue.split("=")[1].equalsIgnoreCase("true")
							|| keyValue.split("=")[1].equalsIgnoreCase("false")) {
						val = Boolean.parseBoolean(keyValue.split("=")[1]);
					} else {
						val = keyValue.split("=")[1];
						index = Integer.parseInt(key.split("value")[1]);
						if (index >= 85) {
							range1 = keyValue.split("=")[1].split("~")[0];
							range2 = keyValue.split("=")[1].split("~")[1];
						}
					}

					if (index >= 85) {
						boolQueryBuilder.must().add(QueryBuilders.rangeQuery(key).gte(range1).lte(range2));
					} else {
						boolQueryBuilder.must()
								.add(QueryBuilders.wildcardQuery(key, "*" + val.toString().toLowerCase() + "*"));

					}
				}

				if (!module.getName().equals("Teams")) {
					boolQueryBuilder.must().add(QueryBuilders.termsQuery("TEAMS", teams));
				}

				boolQueryBuilder.must()
						.add(QueryBuilders.matchQuery(fieldLookUpMap.get("DELETED").replaceAll("field", "value"), false)
								.operator(Operator.AND));

				boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", moduleId).operator(Operator.AND));
				boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId).operator(Operator.AND));

				boolQueryBuilder = buildAllConditions(conditions, moduleService.getAllFields(module, companyId),
						boolQueryBuilder, fieldLookUpMap);
				boolQueryBuilder = buildAnyConditions(conditions, moduleService.getAllFields(module, companyId),
						boolQueryBuilder, fieldLookUpMap);

				sourceBuilder.query(boolQueryBuilder);

				SearchRequest searchRequest = new SearchRequest();
				searchRequest.indices("field_search");
				searchRequest.source(sourceBuilder);

				SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
				SearchHits hits = searchResponse.getHits();

				SearchHit[] searchHits = hits.getHits();
				for (SearchHit hit : searchHits) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();
					ids.add(sourceAsMap.get("ENTRY_ID").toString());
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	private BoolQueryBuilder buildAnyConditions(List<Condition> conditions, List<ModuleField> fields,
			BoolQueryBuilder boolQueryBuilder, Map<String, String> fieldLookUpMap) {
		Asserts.notNull(fields, "Fields must not be null");

		if (conditions == null || conditions.size() == 0) {
			return boolQueryBuilder;
		}
		List<Condition> filteredCondition = conditions.stream()
				.filter(condition -> condition.getRequirementType().equals("Any")).collect(Collectors.toList());

		BoolQueryBuilder orQueryBuilder = new BoolQueryBuilder();
		for (Condition condition : filteredCondition) {
			String fieldId = condition.getCondition();
			String value = condition.getConditionValue();

			String reg = "\\{\\{(.*)\\}\\}";
			Pattern r1 = Pattern.compile(reg);
			Matcher m1 = r1.matcher(value);
			ModuleField conditionField = fields.stream().filter(field -> field.getFieldId().equals(fieldId)).findFirst()
					.get();

			String displayDatatype = conditionField.getDataType().getDisplay();
			String backendDatatype = conditionField.getDataType().getBackend();
			boolean isString = false;
			boolean isInteger = false;
			boolean isBoolean = false;

			String fieldName = conditionField.getName();
			if (!fieldLookUpMap.containsKey(fieldName)) {
				continue;
			}

			if (conditionField.getInheritedField() != null) {
				if (conditionField.getInheritedField()) {
					fieldName = conditionField.getInheritanceLevel() + "." + conditionField.getName();
				}

			}
			if (m1.find()) {
				value = authManager.getUserDetails().getAttributes().get("CONTACT").toString();
			}

			if (displayDatatype.equals("Number") || displayDatatype.equals("Auto Number")
					|| displayDatatype.equals("Chronometer") || displayDatatype.equals("Formula")) {
				isInteger = true;
			}
			if (backendDatatype.equalsIgnoreCase("Boolean")) {
				isBoolean = true;
			}
			if (backendDatatype.equalsIgnoreCase("String") || displayDatatype.equals("Text")) {
				isString = true;
			}

			if (!fieldLookUpMap.containsKey(fieldName)) {
				continue;
			}
			fieldName = fieldLookUpMap.get(fieldName).replaceAll("field", "value");

			switch (condition.getOpearator()) {
			case "EQUALS_TO":
				if (isInteger) {
					orQueryBuilder.should()
							.add(QueryBuilders.matchQuery(fieldName, Integer.parseInt(value)).operator(Operator.OR));
				} else if (isBoolean) {
					orQueryBuilder.should().add(
							QueryBuilders.matchQuery(fieldName, Boolean.parseBoolean(value)).operator(Operator.OR));
				} else {
					orQueryBuilder.should().add(QueryBuilders.matchQuery(fieldName, value).operator(Operator.OR));
				}
				break;
			case "NOT_EQUALS_TO":
				if (isInteger) {
					orQueryBuilder.mustNot()
							.add(QueryBuilders.matchQuery(fieldName, Integer.parseInt(value)).operator(Operator.OR));
				} else if (isBoolean) {
					orQueryBuilder.mustNot().add(
							QueryBuilders.matchQuery(fieldName, Boolean.parseBoolean(value)).operator(Operator.OR));
				} else {
					orQueryBuilder.mustNot().add(QueryBuilders.matchQuery(fieldName, value).operator(Operator.OR));
				}
				break;
			case "GREATER_THAN":
				orQueryBuilder.should().add(QueryBuilders.rangeQuery(fieldName).gte(value));
				break;
			case "LENGTH_IS_GREATER_THAN":
				if (isString) {
					orQueryBuilder.should().add(QueryBuilders.regexpQuery(fieldName, "^.{" + value + ",}$"));
				}
				break;
			case "LENGTH_IS_LESS_THAN":
				if (isString) {
					orQueryBuilder.should().add(QueryBuilders.regexpQuery(fieldName, "^.{0," + value + "}$"));
				}
				break;
			case "LESS_THAN":
				orQueryBuilder.should().add(QueryBuilders.rangeQuery(fieldName).lte(value));
				break;
			case "CONTAINS":
				orQueryBuilder.should()
						.add(QueryBuilders.wildcardQuery(fieldName, value.toString().toLowerCase() + "*"));
				break;
			case "DOES_NOT_CONTAIN":
				orQueryBuilder.mustNot()
						.add(QueryBuilders.wildcardQuery(fieldName, value.toString().toLowerCase() + "*"));
				break;
			case "REGEX":
				orQueryBuilder.should().add(QueryBuilders.regexpQuery(fieldName, value));
				break;
			case "EXISTS":
				orQueryBuilder.should().add(QueryBuilders.existsQuery(fieldName));
				break;
			case "DOES_NOT_EXIST":
				orQueryBuilder.mustNot().add(QueryBuilders.existsQuery(fieldName));
				break;
			}
		}
		boolQueryBuilder.must().add(orQueryBuilder);
		return boolQueryBuilder;
	}

	private BoolQueryBuilder buildAllConditions(List<Condition> conditions, List<ModuleField> fields,
			BoolQueryBuilder boolQueryBuilder, Map<String, String> fieldLookUpMap) {
		Asserts.notNull(fields, "Fields must not be null");

		if (conditions == null || conditions.size() == 0) {
			return boolQueryBuilder;
		}
		List<Condition> filteredCondition = conditions.stream()
				.filter(condition -> condition.getRequirementType().equals("All")).collect(Collectors.toList());

		for (Condition condition : filteredCondition) {
			String fieldId = condition.getCondition();
			String value = condition.getConditionValue();

			String reg = "\\{\\{(.*)\\}\\}";
			Pattern r1 = Pattern.compile(reg);
			Matcher m1 = r1.matcher(value);
			ModuleField conditionField = fields.stream().filter(field -> field.getFieldId().equals(fieldId)).findFirst()
					.get();

			String displayDatatype = conditionField.getDataType().getDisplay();
			String backendDatatype = conditionField.getDataType().getBackend();
			boolean isString = false;
			boolean isInteger = false;
			boolean isBoolean = false;

			String fieldName = conditionField.getName();
			if (!fieldLookUpMap.containsKey(fieldName)) {
				continue;
			}

			if (conditionField.getInheritedField() != null) {
				if (conditionField.getInheritedField()) {
					fieldName = conditionField.getInheritanceLevel() + "." + conditionField.getName();
				}

			}
			if (m1.find()) {
				value = authManager.getUserDetails().getAttributes().get("CONTACT").toString();
			}

			if (displayDatatype.equals("Number") || displayDatatype.equals("Auto Number")
					|| displayDatatype.equals("Chronometer") || displayDatatype.equals("Formula")) {
				isInteger = true;
			}
			if (backendDatatype.equalsIgnoreCase("Boolean")) {
				isBoolean = true;
			}
			if (backendDatatype.equalsIgnoreCase("String") || displayDatatype.equals("Text")) {
				isString = true;
			}
			fieldName = fieldLookUpMap.get(fieldName).replaceAll("field", "value");
			switch (condition.getOpearator()) {
			case "EQUALS_TO":
				if (isInteger) {
					boolQueryBuilder.must()
							.add(QueryBuilders.matchQuery(fieldName, Integer.parseInt(value)).operator(Operator.AND));
				} else if (isBoolean) {
					boolQueryBuilder.must().add(
							QueryBuilders.matchQuery(fieldName, Boolean.parseBoolean(value)).operator(Operator.AND));
				} else {
					boolQueryBuilder.must().add(QueryBuilders.matchQuery(fieldName, value).operator(Operator.AND));
				}
				break;
			case "NOT_EQUALS_TO":
				if (isInteger) {
					boolQueryBuilder.mustNot()
							.add(QueryBuilders.matchQuery(fieldName, Integer.parseInt(value)).operator(Operator.AND));
				} else if (isBoolean) {
					boolQueryBuilder.mustNot().add(
							QueryBuilders.matchQuery(fieldName, Boolean.parseBoolean(value)).operator(Operator.AND));
				} else {
					boolQueryBuilder.mustNot().add(QueryBuilders.matchQuery(fieldName, value).operator(Operator.AND));
				}
				break;
			case "GREATER_THAN":
				boolQueryBuilder.must().add(QueryBuilders.rangeQuery(fieldName).gte(value));
				break;
			case "LENGTH_IS_GREATER_THAN":
				if (isString) {
					boolQueryBuilder.must().add(QueryBuilders.regexpQuery(fieldName, "^.{" + value + ",}$"));
				}
				break;
			case "LENGTH_IS_LESS_THAN":
				if (isString) {
					boolQueryBuilder.must().add(QueryBuilders.regexpQuery(fieldName, "^.{0," + value + "}$"));
				}
				break;
			case "LESS_THAN":
				boolQueryBuilder.must().add(QueryBuilders.rangeQuery(fieldName).lte(value));
				break;
			case "CONTAINS":
				boolQueryBuilder.must()
						.add(QueryBuilders.wildcardQuery(fieldName, value.toString().toLowerCase() + "*"));
				break;
			case "DOES_NOT_CONTAIN":
				boolQueryBuilder.mustNot()
						.add(QueryBuilders.wildcardQuery(fieldName, value.toString().toLowerCase() + "*"));
				break;
			case "REGEX":
				boolQueryBuilder.must().add(QueryBuilders.regexpQuery(fieldName, value));
				break;
			case "EXISTS":
				boolQueryBuilder.must().add(QueryBuilders.existsQuery(fieldName));
				break;
			case "DOES_NOT_EXIST":
				boolQueryBuilder.mustNot().add(QueryBuilders.existsQuery(fieldName));
				break;
			}
		}
		return boolQueryBuilder;
	}

	public Set<String> getAllTeamIds() {
		Optional<List<Map<String, Object>>> optionalTeams = moduleEntryRepository.findAllTeamsOfCurrentUser();
		List<Map<String, Object>> teams = optionalTeams.get();
		Set<String> teamIds = new HashSet<String>();
		teams.forEach(team -> {
			String teamId = team.get("_id").toString();
			teamIds.add(teamId);
		});
		return teamIds;
	}

	public void insertIntoElasticSearch(Map<String, Map<String, Object>> payloadMap, List<Module> moduleFamily,
			String companyId) {

		Map<String, Object> payload = new HashMap<String, Object>();
		moduleFamily.forEach(module -> {
			payload.putAll(payloadMap.get(module.getModuleId()));
			String dataId = payload.get("_id").toString();
			payload.put("_id", dataId);
			postIntoElastic(module, companyId, payload);
		});
	}

	public void updateIntoElasticSearch(Module module, Map<String, Object> payload) {
		String dataId = payload.get("_id").toString();
		Optional<Map<String, Object>> optionalEntry = moduleEntryRepository.findEntryWithAggregation(module, dataId);
		if (optionalEntry.isEmpty()) {
			throw new BadRequestException("INVALID_ENTRY", null);
		}
		Map<String, Object> entry = optionalEntry.get();
		entry.putAll(payload);
		entry.put("_id", dataId);
		postIntoElastic(module, authManager.getUserDetails().getCompanyId(), entry);
	}

	public Optional<ModuleField> getFieldByFieldId(Module module, String fieldId) {
		List<ModuleField> fields = moduleService.getAllFields(module, authManager.getUserDetails().getCompanyId());

		return fields.stream().filter(field -> field.getFieldId().equalsIgnoreCase(fieldId)).findAny();
	}

	public Map<String, Object> formatRelationship(Module module, Map<String, Object> payload) {
		String[] ignored = { "LAST_UPDATED_BY", "CREATED_BY" };
		List<String> ignoredFields = Arrays.asList(ignored);
		List<ModuleField> relationshipFields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Relationship")).collect(Collectors.toList());
		relationshipFields = relationshipFields.stream()
				.filter(field -> !field.getRelationshipType().equals("One To Many")).collect(Collectors.toList());
		relationshipFields = relationshipFields.stream().filter(field -> !ignoredFields.contains(field.getName()))
				.collect(Collectors.toList());
		relationshipFields = relationshipFields.stream().filter(field -> payload.get(field.getName()) != null)
				.collect(Collectors.toList());

		ObjectMapper mapper = new ObjectMapper();

		relationshipFields.forEach(relationshipField -> {
			try {
				if (relationshipField.getRelationshipType().equalsIgnoreCase("One To One")
						|| relationshipField.getRelationshipType().equalsIgnoreCase("Many To One")) {

					// TODO: REMOVE MISMATCH EXCEPTION AND HANDLE PROPERLY IN MANAGER
					try {
						Relationship value = mapper.readValue(
								mapper.writeValueAsString(payload.get(relationshipField.getName())),
								Relationship.class);
						if (value.getDataId() != null) {
							payload.put(relationshipField.getName(), value.getDataId());
						} else {
							payload.remove(relationshipField.getName());
						}

					} catch (MismatchedInputException e) {
					}

				} else if (relationshipField.getRelationshipType().equalsIgnoreCase("Many To Many")) {
					// TODO: REMOVE MISMATCH EXCEPTION AND HANDLE PROPERLY IN MANAGER
					try {
						List<Relationship> values = mapper.readValue(
								mapper.writeValueAsString(payload.get(relationshipField.getName())),
								mapper.getTypeFactory().constructCollectionType(List.class, Relationship.class));
						List<String> relationshipValues = new ArrayList<String>();
						values.forEach(value -> {
							relationshipValues.add(value.getDataId());
						});
						payload.put(relationshipField.getName(), relationshipValues);
					} catch (MismatchedInputException e) {
						e.printStackTrace();
					}
				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
				throw new InternalErrorException("INTERNAL_ERROR");
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				throw new InternalErrorException("INTERNAL_ERROR");
			}
		});
		return payload;
	}

	private List<String> getAllNumberFields(Module module) {
		List<ModuleField> fields = moduleService.getAllFields(module, authManager.getUserDetails().getCompanyId());
		List<ModuleField> numberFields = fields.stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Number")
						|| field.getDataType().getDisplay().equalsIgnoreCase("Auto Number"))
				.collect(Collectors.toList());

		List<String> numberFieldNames = new ArrayList<String>();

		numberFields.forEach(field -> numberFieldNames.add(field.getName()));

		return numberFieldNames;
	}

	public List<ModuleField> getAlternatePrimaryKeys(Module module) {
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

		return alternatePrimaryFields;
	}

	public Module getRequiredModule(Module module, String name) {

		ModuleField moduleField = module.getFields().stream().filter(field -> field.getName().equals(name)).findFirst()
				.orElse(null);

		String[] vars = { name };

		if (moduleField == null) {
			throw new BadRequestException("ENTRY_DOES_NOT_EXIST", vars);
		}
		String moduleNameId = moduleField.getModule();
		Optional<Module> optionalModule = modulesRepository.findById(moduleNameId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("ENTRY_DOES_NOT_EXIST", vars);
		}
		return optionalModule.get();
	}

	public Map<String, Object> formatChronometerInCreateLayout(Module module, Map<String, Object> payload) {

		List<ModuleField> fields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Chronometer")
						&& payload.containsKey(field.getName()))
				.collect(Collectors.toList());
		fields.forEach(field -> {
			String value = null;

			if (payload.containsKey(field.getName()) && payload.get(field.getName()) != null) {
				value = payload.get(field.getName()).toString();
				value = value.replaceAll("\\s+", "");
				if (value.isBlank()) {
					value = "0";
				}
				payload.put(field.getName(), getChronometerValueInMinutes(value));

			}

		});
		return payload;

	}

	public Map<String, Object> formatReceiptCapture(Module module, Map<String, Object> entry,
			ModuleField receiptcapturedField) {
		ObjectMapper mapper = new ObjectMapper();

		try {

			if (entry.get(receiptcapturedField.getName()) != null) {

				MessageAttachment attachment = mapper.readValue(
						mapper.writeValueAsString(entry.get(receiptcapturedField.getName())), MessageAttachment.class);

				if (attachment.getFileName() == null) {
					if (attachment.getFileExtension() == null) {
						attachment.setFileName("File_" + UUID.randomUUID().toString() + ".jpeg");
					} else {
						attachment.setFileName(
								"File_" + UUID.randomUUID().toString() + "." + attachment.getFileExtension());
					}
				}
				String hash = hashAttachment(attachment.getFile());

				Optional<Attachment> optionalAttachment = attachmentsRepository.findAttachmentByHash(hash,
						"attachments_" + authManager.getUserDetails().getCompanyId());
				if (optionalAttachment.isEmpty()) {
					String file = attachment.getFile();

					if (attachment.getAttachmentUuid() == null) {
						attachment.setAttachmentUuid(UUID.randomUUID().toString());
					}

					String base64Regex = "data:image.*?;base64.*";
					Pattern base64Pattern = Pattern.compile(base64Regex, Pattern.DOTALL);
					Matcher base64Matcher = base64Pattern.matcher(file);

					if (base64Matcher.find()) {
						file = base64Matcher.group(0);
					}

					Pattern pattern = Pattern.compile(",(.*)", Pattern.DOTALL);
					Matcher matcher = pattern.matcher(file);

					if (matcher.find()) {
						file = matcher.group(1);
					}

					Attachment newAttachment = new Attachment(null, hash, file, attachment.getAttachmentUuid());

					newAttachment = attachmentsRepository.save(newAttachment,
							"attachments_" + authManager.getUserDetails().getCompanyId());
					attachment.setHash(hash);
					attachment.setFile(file);

				} else {
					attachment.setFile(optionalAttachment.get().getFile());
					attachment.setHash(optionalAttachment.get().getHash());
					attachment.setAttachmentUuid(optionalAttachment.get().getAttachmentUuid());

				}

				entry.put(receiptcapturedField.getName(), attachment);

				return entry;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entry;

	}

	public void addToOCRQueue(Map<String, Object> payload, Module module) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			ModuleField receiptcapturedField = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equals("Receipt Capture")).findFirst()
					.orElse(null);
			if (receiptcapturedField != null && payload.get(receiptcapturedField.getName()) != null) {
				MessageAttachment attachment = mapper.readValue(
						mapper.writeValueAsString(payload.get(receiptcapturedField.getName())),
						MessageAttachment.class);
				OCRPayload ocrPayload = new OCRPayload(authManager.getUserDetails().getCompanyId(),
						payload.get("DATA_ID").toString(), module.getModuleId(), receiptcapturedField.getFieldId(),
						attachment);
				rabbitTemplate.convertAndSend("ocr-response", ocrPayload);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Map<String, Object> encryptPassword(Module module, Map<String, Object> entry) {

		List<ModuleField> fields = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Password"))
				.collect(Collectors.toList());

		fields.forEach(field -> {
			String fieldName = field.getName();
			if (entry.get(fieldName) != null) {
				String secretKey = authManager.getUserDetails().getCompanySubdomain();
				String originalString = entry.get(fieldName).toString();
				if (!originalString.contains("ENCRYPTED:")) {
					if (originalString.isBlank()) {
						entry.put(fieldName, originalString);
					} else {
						String encryptedString = encrypt(originalString, secretKey);
						String encryptedFinalString = "ENCRYPTED:" + encryptedString;
						entry.put(fieldName, encryptedFinalString);
					}
				} else {
					entry.put(fieldName, originalString);
				}
			}
		});
		return entry;

	}

	private SecretKeySpec prepareSecreteKey(String myKey) {
		MessageDigest sha = null;
		byte[] key;
		SecretKeySpec secretKey = null;
		try {
			key = myKey.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return secretKey;
	}

	private String encrypt(String strToEncrypt, String secret) {
		SecretKeySpec secretKey = null;
		try {
			secretKey = prepareSecreteKey(secret);
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addToSingleWorkflowQueue(SingleWorkflowPayload singleWorkflowPayload) {
		try {
			rabbitTemplate.convertAndSend("execute-single-workflow", singleWorkflowPayload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, Object> getListFormulaField(List<ModuleField> listFormulaFields, Map<String, Object> entry,
			Map<String, Object> payload, Module module) {

		for (ModuleField listFormulaField : listFormulaFields) {
			boolean isPresent = false;
			ObjectMapper mapper = new ObjectMapper();
			List<ListFormulaFieldValue> finalListFormulaFieldValues = new ArrayList<ListFormulaFieldValue>();
			List<ListFormulaFieldValue> listFormulaFieldValues = new ArrayList<ListFormulaFieldValue>();
			try {
				listFormulaFieldValues = mapper.readValue(
						mapper.writeValueAsString(entry.get(listFormulaField.getName())),
						mapper.getTypeFactory().constructCollectionType(List.class, ListFormulaFieldValue.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (listFormulaFieldValues != null) {
				for (ListFormulaFieldValue listFormulaFieldValue : listFormulaFieldValues) {
					if (!listFormulaFieldValue.getFormulaName().isEmpty()) {
						isPresent = true;
						List<ListFormulaField> listFormulas = listFormulaField.getListFormula();
						ListFormulaField listFormula = listFormulas.stream()
								.filter(lFormula -> lFormula.getFormulaName()
										.equalsIgnoreCase(listFormulaFieldValue.getFormulaName()))
								.findAny().orElse(null);
						if (listFormula == null) {
							String[] vars = { listFormulaFieldValue.getFormulaName() };
							throw new BadRequestException("FORMULA_NAME_INVALID", vars);
						} else {
							String formula = listFormula.getFormula();
							String value = getFormulaFieldValue(module, payload, listFormulaField, formula);
							if (NumberUtils.isParsable(value)) {
								listFormulaFieldValue.setValue(Float.valueOf(value));
							} else {
								listFormulaFieldValue.setValue(value);
							}
							finalListFormulaFieldValues.add(listFormulaFieldValue);

						}
					}
				}
			}
			if (isPresent) {
				payload.put(listFormulaField.getName(), finalListFormulaFieldValues);
			}
		}
		return payload;
	}

}
