package com.ngdesk.data.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ngdesk.data.company.dao.Company;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.dao.NotificationMessage;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.companies.CompanyRepository;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
public class DeleteChildEntriesService {

	@Autowired
	ModuleService moduleService;

	@Autowired
	DataService dataService;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	RedissonClient client;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	RestHighLevelClient elasticClient;

	@Value("${elastic.host}")
	private String elasticHost;

	public void deleteChildEntries(String companyId, String entryId, String moduleId, String userId) {

		Optional<Company> optionalCompany = companyRepository.findById(companyId, "companies");
		if (!optionalCompany.isEmpty()) {

			Optional<List<Module>> optionalModules = modulesRepository.findAllModules("modules_" + companyId);

			List<Module> allModules = optionalModules.get();
			Optional<Module> optionalParentModule = allModules.stream()
					.filter(module -> module.getModuleId().equals(moduleId)).findAny();

			if (optionalParentModule.isPresent()) {
				deleteAllChildEntries(allModules, optionalParentModule.get(), entryId, companyId, userId);
			}
		}

	}

	public void deleteAllChildEntries(List<Module> allModules, Module parentModule, String parentEntryId,
			String companyId, String userId) {

		List<Module> childModules = new ArrayList<Module>();

		childModules = allModules.stream()
				.filter(module -> module.getParentModule() != null
						&& module.getParentModule().equalsIgnoreCase(parentModule.getModuleId()))
				.collect(Collectors.toList());

		if (childModules.size() == 0) {
			return;
		} else {
			for (Module childModule : childModules) {
				// GET THE FIELD NAME
				String fieldName = parentModule.getSingularName().toUpperCase().replaceAll("\\s+", "_");

				String collectionName = moduleService.getCollectionName(childModule.getName(), companyId);

				// GET ALL CHILD ENTRY IDS
				List<String> childEntryIds = getAllChildEntryIds(fieldName, parentEntryId, collectionName);
				childEntryIds.forEach(childEntryId -> {
					// DELETE EACH CHILD ENTRY
					// TODO: RE-VISIT THE LOGIC
					deleteData(childModule, childEntryId, companyId, userId);
					// DELETE ENTRIES RELATED TO THIS
					deleteAllChildEntries(allModules, childModule, childEntryId, companyId, userId);
				});
			}

			List<String> modulesNotified = new ArrayList<String>();

			childModules.forEach(module -> {

				if (!modulesNotified.contains(module.getModuleId())) {
					dataService.addToNotifyQueue(
							new NotificationMessage(module.getModuleId(), companyId, null, null));
					modulesNotified.add(module.getModuleId());
				}
			});

			return;
		}
	}

	private List<String> getAllChildEntryIds(String parentName, String parentIds, String collectionName) {

		List<String> entryIds = new ArrayList<String>();
		Optional<List<Map<String, Object>>> optionalChildEntries = moduleEntryRepository.findChildEntryIds(parentName,
				parentIds, collectionName);
		if (!optionalChildEntries.isEmpty()) {
			optionalChildEntries.get().forEach(entry -> {
				entryIds.add(entry.get("_id").toString());
			});
		}
		return entryIds;
	}

	public Map<String, Object> deleteData(Module module, String dataId, String companyId, String userId) {
		try {

			String collectionName = moduleService.getCollectionName(module.getName(), companyId);

			Map<String, Object> entry = moduleEntryRepository.findEntryById(dataId, collectionName).get();

			entry.put("DELETED", true);
			entry.put("LAST_UPDATED_BY", userId);
			entry.put("DATE_UPDATED", new Date());

			moduleEntryRepository.updateEntry(entry, collectionName);

			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("ENTRY_ID", dataId));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("MODULE_ID", module.getModuleId()));
			boolQueryBuilder.must().add(QueryBuilders.matchQuery("COMPANY_ID", companyId));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(boolQueryBuilder);

			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("field_search");
			searchRequest.source(sourceBuilder);

			SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();

			for (SearchHit hit : searchHits) {
				DeleteRequest request = new DeleteRequest("field_search", hit.getId());
				elasticClient.delete(request, RequestOptions.DEFAULT);
			}

			SearchRequest globalSearchRequest = new SearchRequest();
			globalSearchRequest.indices("global_search");
			globalSearchRequest.source(sourceBuilder);

			SearchResponse globalSearchResponse = elasticClient.search(globalSearchRequest, RequestOptions.DEFAULT);
			SearchHits globalHits = globalSearchResponse.getHits();
			SearchHit[] globalSearchHits = globalHits.getHits();

			for (SearchHit hit : globalSearchHits) {
				DeleteRequest request = new DeleteRequest("global_search", hit.getId());
				elasticClient.delete(request, RequestOptions.DEFAULT);
			}

			return entry;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
