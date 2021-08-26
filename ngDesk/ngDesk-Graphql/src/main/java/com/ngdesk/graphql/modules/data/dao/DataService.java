package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.util.Asserts;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

@Service
public class DataService {

	@Autowired
	AuthManager authManager;

	@Autowired
	RestHighLevelClient elasticClient;

	@Autowired
	ModulesService moduleService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public boolean isAutorizedForRecord(Set<String> teamIds, List<String> entryTeamIds) {

		for (String teamId : teamIds) {
			if (entryTeamIds.contains(teamId)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getAllTeamIds(Boolean isAdmin) {

		List<Map<String, Object>> teams = moduleEntryRepository
				.findAllTeamsOfCurrentUser(authManager.getUserDetails().getCompanyId(), isAdmin);
		Set<String> teamIds = new HashSet<String>();
		teams.forEach(team -> {
			String teamId = team.get("_id").toString();
			teamIds.add(teamId);
		});
		return teamIds;
	}

	public List<String> getIdsFromGlobalSearch(String value, Module module, Set<String> teams) {
		List<String> ids = new ArrayList<String>();
		try {
			String companyId = authManager.getUserDetails().getCompanyId();
			List<Module> allModules = modulesRepository.findAllModules("modules_" + companyId);

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
				boolQueryBuilder.must()
						.add(QueryBuilders.wildcardQuery("input", "*" + value.toString().toLowerCase() + "*"));

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

				sourceBuilder.query(boolQueryBuilder);

				System.out.println(boolQueryBuilder);

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

	private List<String> getAllNumberFields(Module module, List<Module> allModules) {
		List<ModuleField> fields = moduleService.getAllFields(module, allModules);
		List<ModuleField> numberFields = fields.stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Number")
						|| field.getDataType().getDisplay().equalsIgnoreCase("Auto Number"))
				.collect(Collectors.toList());

		List<String> numberFieldNames = new ArrayList<String>();

		numberFields.forEach(field -> numberFieldNames.add(field.getName()));

		return numberFieldNames;
	}

	public String chronometerFormatTransform(Integer value, String formattedTime) {

		int remainder = 0;
		if (value >= 9600) {
			// 1 Month = 9600 minutes
			remainder = value % 9600;
			if (remainder == 0) {
				return value / 9600 + "mo";
			} else {
				formattedTime = (int) Math.floor(value / 9600) + "mo";
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else if (value >= 2400) {
			// 1 Week = 2400 minutes
			remainder = value % 2400;
			if (remainder == 0) {
				if (formattedTime.length() > 0) {
					return formattedTime + ' ' + value / 2400 + "w";
				} else {
					return value / 2400 + "w";
				}
			} else {
				if (formattedTime.length() > 0) {
					formattedTime = formattedTime + ' ' + (int) Math.floor(value / 2400) + "w";
				} else {
					formattedTime = (int) Math.floor(value / 2400) + "w";
				}
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else if (value >= 480) {

			// 1 Day = 480 minutes
			remainder = value % 480;
			if (remainder == 0) {
				if (formattedTime.length() > 0) {
					return formattedTime + ' ' + value / 480 + "d";
				} else {
					return value / 480 + "d";
				}
			} else {
				if (formattedTime.length() > 0) {
					formattedTime = formattedTime + ' ' + (int) Math.floor(value / 480) + "d";
				} else {
					formattedTime = (int) Math.floor(value / 480) + "d";
				}
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else if (value >= 60) {
			// 1 Hour = 60 minutes
			remainder = value % 60;
			if (remainder == 0) {
				if (formattedTime.length() > 0) {
					return formattedTime + ' ' + value / 60 + "h";
				} else {
					return value / 60 + "h";
				}
			} else {
				if (formattedTime.length() > 0) {
					formattedTime = formattedTime + ' ' + (int) Math.floor(value / 60) + "h";
				} else {
					formattedTime = (int) Math.floor(value / 60) + "h";
				}
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else {
			if (formattedTime.length() > 0) {
				return formattedTime + ' ' + value + "m";
			} else {
				return formattedTime + value + "m";
			}
		}
	}

}
