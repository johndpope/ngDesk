package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.catalogue.dao.Catalogue;
import com.ngdesk.graphql.categories.dao.Category;
import com.ngdesk.graphql.escalation.dao.EscalateTo;
import com.ngdesk.graphql.form.dao.Form;
import com.ngdesk.graphql.knowledgebase.section.dao.Section;
import com.ngdesk.graphql.schedules.dao.Layer;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RelationshipDataFetcher implements DataFetcher<List<Map<String, Object>>> {

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	AuthManager manager;

	@Override
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) {
		List<String> entryIds = new ArrayList<String>();
		try {
			Map<String, Object> entry = environment.getSource();
			String fieldName = environment.getField().getName();
			entryIds = (List<String>) entry.get(fieldName);
		} catch (Exception e) {
			// CHECK FOR SCHEDULES
			try {
				Layer layer = environment.getSource();
				entryIds = layer.getUsers();
			} catch (Exception e1) {
				try {
					Catalogue catalogue = environment.getSource();
					entryIds = catalogue.getVisibleTo();
				} catch (Exception e2) {
					try {
						Form form = environment.getSource();
						entryIds = form.getVisibleTo();
					} catch (Exception e3) {
						try {
							Category categgory = environment.getSource();
							entryIds = categgory.getVisibleTo();
						} catch (Exception e4) {
							try {
								Section section = environment.getSource();
								String fieldName = environment.getField().getName();
								if (fieldName.equalsIgnoreCase("visibleTo")) {
									entryIds = section.getVisibleTo();
								} else {
									entryIds = section.getManagedBy();
								}

							} catch (Exception e5) {

								try {
									EscalateTo escalateTo = environment.getSource();
									String fieldType = environment.getFieldType().toString();
									if (fieldType.equals("[Users]")) {
										entryIds = escalateTo.getUsers();
									} else {
										entryIds = escalateTo.getTeams();
									}

								} catch (Exception e6) {
									e6.printStackTrace();
									return null;
								}
							}

						}
					}
				}

			}
		}

		if (entryIds == null || entryIds.size() == 0) {
			return new ArrayList<Map<String, Object>>();
		}

		String fieldType = environment.getFieldType().toString();
		fieldType = fieldType.replaceAll("\\[", "");
		fieldType = fieldType.replaceAll("\\]", "");
		String collectionName = fieldType + "_" + manager.getUserDetails().getCompanyId();
		return entryRepository.findEntriesByIds(entryIds, collectionName);
	}

}
