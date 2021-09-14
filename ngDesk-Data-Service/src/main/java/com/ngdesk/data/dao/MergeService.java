package com.ngdesk.data.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
public class MergeService {

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	DataService dataService;

	public ModuleField getDiscussionField(Module module) {
		Optional<ModuleField> optionalDiscussionField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Discussion")).findFirst();
		return optionalDiscussionField.orElse(null);
	}

	public List<DiscussionMessage> getDiscussionMessage(ModuleField discussionField, Map<String, Object> entry) {
		List<DiscussionMessage> messages = new ArrayList<DiscussionMessage>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			if (entry.get(discussionField.getName()) == null) {
				return messages;
			}
			messages = mapper.readValue(mapper.writeValueAsString(entry.get(discussionField.getName())),
					mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
		} catch (Exception e) {
			String[] vars = { discussionField.getName() };
			throw new BadRequestException("ERROR_IN_READING_DISCUSSION", vars);
		}

		return messages;
	}

	public List<DiscussionMessage> mergeDiscussionMessages(List<String> entryIds, Module module,
			ModuleField discussionField) {
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());

		List<DiscussionMessage> mergedMessages = new ArrayList<DiscussionMessage>();
		entryIds.forEach(entryId -> {
			Optional<Map<String, Object>> optionalEntry = entryRepository.findById(entryId, collectionName);
			Map<String, Object> entry = optionalEntry.get();
			List<DiscussionMessage> messages = getDiscussionMessage(discussionField, entry);
			mergedMessages.addAll(messages);
		});

		return mergedMessages;
	}

	public List<DiscussionMessage> sortDiscussion(List<DiscussionMessage> messages) {
		Collections.sort(messages, new Comparator<DiscussionMessage>() {
			@Override
			public int compare(DiscussionMessage discussion1, DiscussionMessage discussion2) {
				return discussion1.getDateCreated().compareTo(discussion2.getDateCreated());
			}
		});

		return messages;
	}

	public DiscussionMessage mergeMetaData(Module module) {

		DiscussionMessage message = new DiscussionMessage();

		message.setDateCreated(new Date());
		message.setMessage("<html><body><div class='mat-caption' style=\"color:#68737D\">" + module.getSingularName()
				+ "(s) have been merged</div></body></html>");
		message.setMessageId(UUID.randomUUID().toString());
		message.setAttachments(new ArrayList<MessageAttachment>());
		message.setMessageType("META_DATA");
		message.setSender(getSender());

		return message;
	}

	private Sender getSender() {
		Optional<Map<String, Object>> optionalSystemUser = entryRepository.findEntryByFieldName("EMAIL_ADDRESS",
				"system@ngdesk.com", "Users_" + authManager.getUserDetails().getCompanyId());

		Map<String, Object> systemUser = optionalSystemUser.get();

		String contactDetails = systemUser.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalSystemUserContact = entryRepository.findById(contactDetails,
				"Contacts_" + authManager.getUserDetails().getCompanyId());
		Map<String, Object> systemUserContact = optionalSystemUserContact.get();

		Sender sender = new Sender(systemUserContact.get("FIRST_NAME").toString(),
				systemUserContact.get("LAST_NAME").toString(), systemUser.get("USER_UUID").toString(),
				systemUser.get("ROLE").toString());

		return sender;
	}

	// Handled text area fields

	public List<ModuleField> getTextAreaFields(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Text Area"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public String getTextAreaValue(ModuleField textAreaField, Map<String, Object> entry) {
		String textAreaValue = "";
		try {
			if (entry.get(textAreaField.getName()) == null) {
				return textAreaValue;
			}
			textAreaValue = entry.get(textAreaField.getName()).toString();
		} catch (Exception e) {
			String[] vars = { textAreaField.getName() };
			throw new BadRequestException("ERROR_IN_READING_TEXT_FIELD", vars);
		}
		return textAreaValue;
	}

	public String mergeTextAreaValues(List<String> entryIds, Module module, ModuleField textAreaField) {
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());

		String mergedTextAreaFieldMessages = "";
		String concatinateValue = "";
		for (String entryId : entryIds) {
			Optional<Map<String, Object>> optionalEntry = entryRepository.findById(entryId, collectionName);
			Map<String, Object> entry = optionalEntry.get();

			if (entry.get(textAreaField.getName()) != null) {
				String textAreaFieldMessages = getTextAreaValue(textAreaField, entry);
				concatinateValue = mergedTextAreaFieldMessages.concat(textAreaFieldMessages);
			}
		}
		return concatinateValue;
	}

	public Map<String, Object> getTextAreaFieldsValue(Module module, Merge merge, Map<String, Object> entry) {
		List<ModuleField> textAreaFields = getTextAreaFields(module);
		if (textAreaFields != null) {
			for (ModuleField textAreaField : textAreaFields) {
				if (entry.containsKey(textAreaField.getName())) {
					if (entry.get(textAreaField.getName()) != null) {
						String textAreaFieldValue = getTextAreaValue(textAreaField, entry);
						String textAreaMergedValues = mergeTextAreaValues(merge.getMergeEntryIds(), module,
								textAreaField);
						String concatValues = textAreaFieldValue + " " + textAreaMergedValues;
						entry.put(textAreaField.getName(), concatValues.trim());
					}
				}
			}
		}
		return entry;
	}

	// Handled for formula field
	public List<ModuleField> getFormulaFields(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Formula"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public Map<String, Object> getFormulaFieldsValue(Module module, Merge merge, Map<String, Object> entry) {
		List<ModuleField> formulaFields = getFormulaFields(module);
		if (formulaFields != null) {
			for (ModuleField formulaField : formulaFields) {
				if (entry.containsKey(formulaField.getName())) {
					if (entry.get(formulaField.getName()) != null) {
						String formulaFieldValue = getTextAreaValue(formulaField, entry);
						if (!NumberUtils.isParsable(formulaFieldValue)) {
							entry.put(formulaField.getName(), formulaFieldValue);

						} else {
							String value = dataService.getFormulaFieldValue(module, entry, formulaField,
									formulaField.getFormula());
							entry.put(formulaField.getName(), value);
						}
					}
				}
			}
		}
		return entry;
	}

	// Handled Number Field
	public List<ModuleField> getNumberFields(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Number"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public Map<String, Object> getNumberFieldsValue(Module module, Merge merge, Map<String, Object> entry) {
		List<ModuleField> numberFields = getNumberFields(module);
		if (numberFields != null) {
			for (ModuleField numberField : numberFields) {
				if (entry.containsKey(numberField.getName())) {
					if (entry.get(numberField.getName()) != null) {

						Integer numberValue = getNumberAndChronometerValue(numberField, entry);
						Integer numberMergedValus = mergeNumberAndChronometerValues(merge.getMergeEntryIds(), module,
								numberField);
						int sumOfNumber = numberValue + numberMergedValus;
						entry.put(numberField.getName(), sumOfNumber);
					}
				}
			}
		}
		return entry;
	}

	// Handled Chronometer Field
	public List<ModuleField> getChronometerFields(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Chronometer"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public Integer getNumberAndChronometerValue(ModuleField chronometerField, Map<String, Object> entry) {
		Integer chronometerValue = 0;
		try {
			if (entry.get(chronometerField.getName()) == null) {
				return chronometerValue;
			}
			chronometerValue = Integer.parseInt(entry.get(chronometerField.getName()).toString());
		} catch (Exception e) {
			String[] vars = { chronometerField.getName() };
			throw new BadRequestException("ERROR_IN_READING_CHRONOMETER_FIELD", vars);
		}
		return chronometerValue;
	}

	public Integer mergeNumberAndChronometerValues(List<String> entryIds, Module module, ModuleField chronometerField) {
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());

		Integer mergedMessages = 0;
		for (String entryId : entryIds) {
			Optional<Map<String, Object>> optionalEntry = entryRepository.findById(entryId, collectionName);
			Map<String, Object> entry = optionalEntry.get();
			Integer messages = getNumberAndChronometerValue(chronometerField, entry);
			mergedMessages = Integer.sum(messages, mergedMessages);
		}

		return mergedMessages;
	}

	public Map<String, Object> getChronometerFieldsValue(Module module, Merge merge, Map<String, Object> entry) {
		List<ModuleField> chronometerFields = getChronometerFields(module);
		if (chronometerFields != null) {
			for (ModuleField chronometerField : chronometerFields) {
				if (entry.containsKey(chronometerField.getName())) {
					if (entry.get(chronometerField.getName()) != null) {

						Integer chronometerValue = getNumberAndChronometerValue(chronometerField, entry);
						Integer chronometerMergedValus = mergeNumberAndChronometerValues(merge.getMergeEntryIds(),
								module, chronometerField);
						int sumOfChronometer = chronometerValue + chronometerMergedValus;
						entry.put(chronometerField.getName(), sumOfChronometer);
					}
				}
			}
		}
		return entry;
	}

	// Handled Relationship for many to many
	public List<ModuleField> getRelationshipField(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Relationship")
						&& field.getRelationshipType().equals("Many to Many"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public Map<String, Object> getRelationshipManyToManyValue(Module module, Merge merge, Map<String, Object> entry) {

		List<ModuleField> relationshipFields = getRelationshipField(module);
		if (relationshipFields != null) {
			for (ModuleField relationshipField : relationshipFields) {
				if (entry.containsKey(relationshipField.getName())) {
					if (entry.get(relationshipField.getName()) != null) {

						List<String> relationshipFieldValues = (List<String>) entry.get(relationshipField.getName());
						List<String> relationshipFieldMergedValues = mergeListTextFields(merge.getMergeEntryIds(),
								module, relationshipField);

						relationshipFieldValues.addAll(relationshipFieldMergedValues);
						List<String> relationshipWithoutDuplicates = relationshipFieldValues.stream().distinct()
								.collect(Collectors.toList());
						entry.put(relationshipField.getName(), relationshipWithoutDuplicates);
					}
				}
			}
		}
		return entry;
	}

	public List<ModuleField> getRelationshipFieldOneToMany(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equals("Relationship")
						&& field.getRelationshipType().equals("One to Many"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public Map<String, Object> getRelationshipFieldOneToManyValue(Module module, Merge merge,
			Map<String, Object> entry) {
		List<ModuleField> relationshipFields = getRelationshipFieldOneToMany(module);
		if (relationshipFields != null) {
			for (ModuleField relationshipField : relationshipFields) {
				Optional<Module> relatedModule = modulesRepository.findById(relationshipField.getModule(),
						"modules_" + authManager.getUserDetails().getCompanyId());

				if (relatedModule.isPresent()) {
					Optional<ModuleField> relatedfield = relatedModule.get().getFields().stream()
							.filter(field -> field.getFieldId().equals(relationshipField.getRelationshipField()))
							.findFirst();

					if (relatedfield.isPresent()) {
						String relatedModuleName = relatedModule.get().getName().replaceAll("\\s+", "_");
						String collectionName = relatedModuleName + "_" + authManager.getUserDetails().getCompanyId();
						Optional<List<Map<String, Object>>> relatedModuleEntries = entryRepository
								.findAllEntriesByFieldName(merge.getMergeEntryIds(), relatedfield.get().getName(),
										collectionName);

						if (relatedModuleEntries.isPresent()) {
							for (Map<String, Object> relatedModuleEntry : relatedModuleEntries.get()) {

								relatedModuleEntry.put(relatedfield.get().getName(), merge.getEntryId());
								entryRepository.updateEntry(relatedModuleEntry, collectionName);

							}
						}
					}
				}
			}
		}
		return entry;
	}

	// Handled List Text Fields
	public List<ModuleField> getListTextFields(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("List Text"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public List<String> mergeListTextFields(List<String> entryIds, Module module, ModuleField listTextFields) {
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());
		List<String> listTextDataFields = new ArrayList<String>();
		entryIds.forEach(entryId -> {
			Optional<Map<String, Object>> optionalEntry = entryRepository.findById(entryId, collectionName);
			Map<String, Object> entry = optionalEntry.get();
			List<String> listTextFieldValues = (List<String>) entry.get(listTextFields.getName());
			if (listTextFieldValues != null) {
				listTextDataFields.addAll(listTextFieldValues);
			}
		});

		return listTextDataFields;
	}

	public Map<String, Object> getListTextFieldValue(Module module, Merge merge, Map<String, Object> entry) {
		List<ModuleField> listTextFields = getListTextFields(module);
		if (listTextFields != null) {
			for (ModuleField listTextField : listTextFields) {
				if (entry.containsKey(listTextField.getName())) {
					if (entry.get(listTextField.getName()) != null) {
						List<String> listTextFieldValues = (List<String>) entry.get(listTextField.getName());
						List<String> listTextFieldMergedValues = mergeListTextFields(merge.getMergeEntryIds(), module,
								listTextField);
						listTextFieldValues.addAll(listTextFieldMergedValues);
						List<String> listWithoutDuplicates = listTextFieldValues.stream().distinct()
								.collect(Collectors.toList());
						entry.put(listTextField.getName(), listWithoutDuplicates);
					}
				}
			}
		}
		return entry;
	}

	// Handled Currency Field
	public List<ModuleField> getCurrencyField(Module module) {
		List<ModuleField> optionalListTextField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Currency"))
				.collect(Collectors.toList());
		return optionalListTextField;
	}

	public Float getCurrencyValue(ModuleField chronometerField, Map<String, Object> entry) {
		Float chronometerValues = 0.0f;
		try {
			if (entry.get(chronometerField.getName()) == null) {
				return chronometerValues;
			}

			chronometerValues = Float.parseFloat(entry.get(chronometerField.getName()).toString());
		} catch (Exception e) {
			String[] vars = { chronometerField.getName() };
			throw new BadRequestException("ERROR_IN_READING_CURRENCY_FIELD", vars);
		}

		return chronometerValues;
	}

	public Float mergeCurrencyValues(List<String> entryIds, Module module, ModuleField chronometerField) {
		String collectionName = moduleService.getCollectionName(module.getName(),
				authManager.getUserDetails().getCompanyId());

		Float mergedMessages = 0.0f;
		for (String entryId : entryIds) {
			Optional<Map<String, Object>> optionalEntry = entryRepository.findById(entryId, collectionName);
			Map<String, Object> entry = optionalEntry.get();
			Float messages = getCurrencyValue(chronometerField, entry);
			mergedMessages = Float.sum(messages, mergedMessages);
		}
		return mergedMessages;
	}

	public Map<String, Object> getCurrencyFieldValue(Module module, Merge merge, Map<String, Object> entry) {
		List<ModuleField> currencyFields = getCurrencyField(module);
		if (currencyFields != null) {
			for (ModuleField currencyField : currencyFields) {
				if (entry.containsKey(currencyField.getName())) {
					if (entry.get(currencyField.getName()) != null) {
						Float currencyValue = getCurrencyValue(currencyField, entry);
						Float currencyMergedValues = mergeCurrencyValues(merge.getMergeEntryIds(), module,
								currencyField);
						currencyValue = currencyValue + currencyMergedValues;
						entry.put(currencyField.getName(), currencyValue);
					}
				}
			}
		}
		return entry;

	}

}
