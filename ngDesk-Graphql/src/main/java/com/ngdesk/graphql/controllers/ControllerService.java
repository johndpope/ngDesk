package com.ngdesk.graphql.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;
import com.opencsv.CSVWriter;

@Component
public class ControllerService {

	@Autowired
	SessionManager sessionManager;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	Global global;

	@Autowired
	AuthManager authManager;

	@Autowired
	SendMail sendMail;

	ObjectMapper mapper = new ObjectMapper();

	public void generateCsvForEntries(Map<String, Object> map, List<String> fieldNames, String fileName,
			List<String> emailIds) {
		
		String companyId = authManager.getUserDetails().getCompanyId();
		String subdomain = authManager.getUserDetails().getCompanySubdomain();

		String name = map.keySet().toString();
		String regex = "\\[|\\]";
		name = name.replaceAll(regex, "");
		try {
			List<Map<String, Object>> entries = mapper.readValue(mapper.writeValueAsString(map.get(name)),
					mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
			Module module = (Module) sessionManager.getSessionInfo().get("currentModule");

			if (entries == null) {
				String[] vars = { module.getName() };
				throw new BadRequestException("INVALID_MODULE_ENTRIES", vars);

			}

			List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);
			List<String> fieldNameForvalues = new ArrayList<String>();
			Map<String, List<String>> mapChildFileName = new HashMap<String, List<String>>();
			for (String key : fieldNames) {
				String[] arrayOfFieldNames = key.split("[.]");
				if (fieldNameForvalues.contains(arrayOfFieldNames[0])) {
					List<String> newlist = new ArrayList<String>();
					newlist.addAll(mapChildFileName.get(arrayOfFieldNames[0]));
					newlist.add(arrayOfFieldNames[1].toString());
					mapChildFileName.put(arrayOfFieldNames[0], newlist);
				} else {
					if (arrayOfFieldNames.length > 1) {
						mapChildFileName.put(arrayOfFieldNames[0], Arrays.asList(arrayOfFieldNames[1]));
					}
					fieldNameForvalues.add(arrayOfFieldNames[0]);
				}
			}

			Writer writer = new StringWriter();
			CSVWriter csvWriter = new CSVWriter(writer);

			// Write the csv header fieldNames
			csvWriter.writeNext(fieldNames.toArray(new String[fieldNames.size()]));
			fieldNameForvalues = fieldNameForvalues.stream().distinct().collect(Collectors.toList());

			// Looping the all entires
			for (Map<String, Object> entry : entries) {
				List<Map<String, Object>> oneToManyEntries = new ArrayList<Map<String, Object>>();
				List<String> values = new ArrayList<String>();
				List<List<String>> listOfEntryValues = new ArrayList<List<String>>();

				// looping the entries based on fieldnames
				for (String fieldName : fieldNameForvalues) {
					ModuleField moduleField = module.getFields().stream()
							.filter(filter -> filter.getName().equalsIgnoreCase(fieldName)).findFirst().orElse(null);
					String dataType = moduleField.getDataType().getDisplay();

					if (entry.get(fieldName) != null) {

						// Check if the dataType is Relationship and RelationshipType is Many To One
						if (dataType.equalsIgnoreCase("Relationship")
								&& moduleField.getRelationshipType().equalsIgnoreCase("Many To One")) {
							if (listOfEntryValues.isEmpty()) {
								values.add(
										getformatedRelationshipManyToOneValues(entry, fieldName, modules, moduleField));
							} else {
								for (List<String> listOfEntryValue : listOfEntryValues) {
									listOfEntryValue.add(getformatedRelationshipManyToOneValues(entry, fieldName,
											modules, moduleField));
								}
							}
						}

						// Check if the dataType is Relationship and RelationshipType is One To Many
						else if (dataType.equalsIgnoreCase("Relationship")
								&& moduleField.getRelationshipType().equalsIgnoreCase("One To Many")) {

							oneToManyEntries = mapper.readValue(mapper.writeValueAsString(entry.get(fieldName)),
									mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

							if (oneToManyEntries.isEmpty()) {
								for (int i = 0; i < mapChildFileName.get(fieldName).size(); i++) {
									values.add("");
								}
							}
							if (listOfEntryValues.isEmpty()) {
								for (Map<String, Object> oneToManyEntry : oneToManyEntries) {
									List<String> updatedValues = new ArrayList<String>();
									updatedValues.addAll(values);
									for (String key : oneToManyEntry.keySet()) {
										if (oneToManyEntry.get(key) != null) {
											updatedValues.add(oneToManyEntry.get(key).toString());
										} else {
											updatedValues.add("");
										}
									}
									listOfEntryValues.add(updatedValues);
								}
							} else {
								List<List<String>> newArrlistOfEntryValues = new ArrayList<List<String>>();

								for (List<String> listOfEntryValue : listOfEntryValues) {
									if (oneToManyEntries.isEmpty()) {
										for (int i = 0; i < mapChildFileName.get(fieldName).size(); i++) {
											listOfEntryValue.add("");
											newArrlistOfEntryValues.add(listOfEntryValue);
										}
									}
									for (Map<String, Object> oneToManyEntry : oneToManyEntries) {
										List<String> updatedValues = new ArrayList<String>();
										updatedValues.addAll(listOfEntryValue);
										for (String key : oneToManyEntry.keySet()) {
											if (oneToManyEntry.get(key) != null) {
												updatedValues.add(oneToManyEntry.get(key).toString());
											} else {
												updatedValues.add("");
											}
										}
										newArrlistOfEntryValues.add(updatedValues);
									}
								}
								listOfEntryValues = new ArrayList<List<String>>();
								listOfEntryValues = newArrlistOfEntryValues;
							}

						}

						// Check if the dataType is Discussion
						else if (dataType.equalsIgnoreCase("Discussion")) {
							if (listOfEntryValues.isEmpty()) {
								values.add(getformatedDiscussionValues(entry, fieldName));
							} else {
								for (List<String> listOfEntryValue : listOfEntryValues) {
									listOfEntryValue.add(getformatedDiscussionValues(entry, fieldName));
								}
							}
						}

						// Check if the dataType is Phone
						else if (dataType.equalsIgnoreCase("Phone")) {
							if (listOfEntryValues.isEmpty()) {
								values.add(getformatedPhoneValues(entry, fieldName));
							} else {
								for (List<String> listOfEntryValue : listOfEntryValues) {
									listOfEntryValue.add(getformatedPhoneValues(entry, fieldName));
								}
							}
						} else {
							if (listOfEntryValues.isEmpty()) {
								values.add(entry.get(fieldName).toString());
							} else {
								for (List<String> listOfEntryValue : listOfEntryValues) {
									listOfEntryValue.add(entry.get(fieldName).toString());
								}
							}
						}
					} else {
						values.add("");
					}
				}

				// Write all the values into csv
				if (listOfEntryValues.isEmpty()) {
					csvWriter.writeNext(values.toArray(new String[values.size()]));
				} else {

					for (List<String> listOfEntryValue : listOfEntryValues) {
						csvWriter.writeNext(listOfEntryValue.toArray(new String[listOfEntryValue.size()]));

					}
				}
			}

			// Save the file in System in particular path
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String reportName = fileName + "-" + format.format(new Date());
			FileWriter myWriter = new FileWriter("/opt/ngdesk/reports/" + reportName + ".csv");
			myWriter.write(writer.toString());
			myWriter.close();

			String link = "https://" + subdomain
					+ ".ngdesk.com/api/ngdesk-report-service-v1/reports/download?reportName=" + reportName;
			String messageHTML = global.getFile("report_download_email.html");
			messageHTML = messageHTML.replaceAll("REPORT_NAME", fileName);
			messageHTML = messageHTML.replaceAll("LINK_REPLACE", link);
			if (emailIds != null) {
				for (String emailId : emailIds) {
					sendMail.send(emailId, "support@" + subdomain + ".ngdesk.com", "Download" + fileName + " Report",
							messageHTML);
				}
			} else {
				String emailId = authManager.getUserDetails().getEmailAddress();
				sendMail.send(emailId, "support@" + subdomain + ".ngdesk.com", "Download" + fileName + " Report",
						messageHTML);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getRelationshipName(List<Module> modules, ModuleField moduleField) {

		Module relatedModule = modules.stream()
				.filter(relationModule -> relationModule.getModuleId().equals(moduleField.getModule())).findFirst()
				.orElse(null);

		if (relatedModule != null) {
			ModuleField relatedModuleField = relatedModule.getFields().stream()
					.filter(filter -> filter.getFieldId().equalsIgnoreCase(moduleField.getPrimaryDisplayField()))
					.findFirst().orElse(null);
			if (relatedModuleField != null) {
				String relatedModuleFieldName = relatedModuleField.getName();
				return relatedModuleFieldName;
			}
		}

		return null;

	}

	public String getformatedPhoneValues(Map<String, Object> entry, String arrayOfFieldNames) {

		Map<String, Object> doc = (Map<String, Object>) entry.get(arrayOfFieldNames);
		String countryCode = "";
		if (doc.get("DIAL_CODE") != null) {
			countryCode = doc.get("DIAL_CODE").toString();
		}
		String phoneNumber = "";
		if (doc.get("PHONE_NUMBER") != null) {
			phoneNumber = doc.get("PHONE_NUMBER").toString();
		}

		if (countryCode != null && phoneNumber != null) {
			String value = countryCode + phoneNumber;
			return value;
		}
		return null;

	}

	public String getformatedDiscussionValues(Map<String, Object> entry, String arrayOfFieldNames) {

		String exportMessages = "";
		List<Map<String, Object>> messages = (List<Map<String, Object>>) entry.get(arrayOfFieldNames);
		for (Map<String, Object> message : messages) {
			exportMessages = exportMessages + message.get("MESSAGE");
		}
		String text = Jsoup.parse(exportMessages).text();
		return text;

	}

	public String getformatedRelationshipManyToOneValues(Map<String, Object> entry, String arrayOfFieldNames,
			List<Module> modules, ModuleField moduleField) {

		try {
			String relatedModuleFieldName = getRelationshipName(modules, moduleField);

			Map<String, Object> relationsipValue = mapper
					.readValue(mapper.writeValueAsString(entry.get(arrayOfFieldNames)), Map.class);

			if (relationsipValue.get("PRIMARY_DISPLAY_FIELD") != null) {
				return relationsipValue.get("PRIMARY_DISPLAY_FIELD").toString();

			} else {
				if (relationsipValue.get(relatedModuleFieldName) != null) {
					return relationsipValue.get(relatedModuleFieldName).toString();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

}
