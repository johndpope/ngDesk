package com.ngdesk.data.dao;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.jobs.DeleteChildEntriesService;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.data.roles.dao.Role;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;

@Component
@RabbitListener(queues = "delete-entries", concurrency = "5")
public class DeleteDataService {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RestHighLevelClient elasticClient;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	DeleteChildEntriesService deleteChildEntriesService;

	@RabbitHandler
	public void deleteAllData(DeleteEntriesPayload delete) {
		try {
			Optional<Module> optionalModule = modulesRepository.findById(delete.getModuleId(), "modules_" + delete.getCompanyId());
			if (optionalModule.isPresent()) {
				Module module = optionalModule.get();
				if (module.getName().equalsIgnoreCase("Users")) {
					deleteUserData(delete.getEntryIds(), module, delete.getCompanyId());
				} else if (module.getName().equalsIgnoreCase("Contacts")) {
					delete.getEntryIds().forEach(deleteDataId -> {
						removeContactFromAllEntries(module, deleteDataId, delete.getCompanyId());
					});
				}

				deleteInterdependentEntries(delete.getEntryIds(), module, delete.getCompanyId(), delete.getUserId());

				if (!module.getName().equalsIgnoreCase("Users")) {
					checkDependencyAndUpdate(module, delete.getEntryIds(), delete.getCompanyId());
				}

				for (String dataId : delete.getEntryIds()) {
					replaceGhostIdForAllEntry(module, dataId, delete.getCompanyId());
					deleteChildEntriesService.deleteChildEntries(delete.getCompanyId(), dataId, delete.getModuleId(),
							delete.getUserId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteInterdependentEntries(List<String> entryIds, Module module, String companyId, String userId) {
		entryIds.forEach(deleteDataId -> {
			if (module.getName().equalsIgnoreCase("Users")) {
				Map<String, Object> deletedEntry = moduleEntryRepository.findById(deleteDataId, "Users_" + companyId)
						.get();
				String ghostContactId = getGhostContact(companyId).get("_id").toString();

				if (deletedEntry.get("CONTACT") != null
						&& !deletedEntry.get("CONTACT").toString().equals(ghostContactId)) {

					String dataContactId = deletedEntry.get("CONTACT").toString();
					Map<String, Object> deletedContactEntry = deleteDataInEntries(dataContactId, "Contacts", companyId,
							userId);
					Module contactModule = getRequiredModule(module, "CONTACT", companyId);
					removeContactFromAllEntries(contactModule, dataContactId, companyId);

				}

			} else if (module.getName().equalsIgnoreCase("Contacts")) {
				Map<String, Object> deletedEntry = moduleEntryRepository.findById(deleteDataId, "Contacts_" + companyId)
						.get();

				if (deletedEntry.get("USER") != null) {

					String dataUserId = deletedEntry.get("USER").toString();
					Map<String, Object> deletedUserEntry = deleteDataInEntries(dataUserId, "Users", companyId, userId);
					Module userModule = getRequiredModule(module, "USER", companyId);
					removeUserFromAllEntries(userModule, dataUserId, companyId);

				}
			}
		});
	}

	public Module getRequiredModule(Module module, String name, String companyId) {

		ModuleField moduleField = module.getFields().stream().filter(field -> field.getName().equals(name)).findFirst()
				.orElse(null);

		String[] vars = { name };

		if (moduleField == null) {
			throw new BadRequestException("ENTRY_DOES_NOT_EXIST", vars);
		}
		String moduleNameId = moduleField.getModule();
		Optional<Module> optionalModule = modulesRepository.findById(moduleNameId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("ENTRY_DOES_NOT_EXIST", vars);
		}
		return optionalModule.get();
	}

	public Map<String, Object> deleteDataInEntries(String dataId, String entryName, String companyId, String userId) {
		try {
			String collectionName = moduleService.getCollectionName(entryName, companyId);

			Map<String, Object> entry = moduleEntryRepository.findById(dataId, collectionName).get();
			entry.put("DELETED", true);
			entry.put("LAST_UPDATED_BY", userId);
			entry.put("DATE_UPDATED", new Date());

			moduleEntryRepository.updateEntry(entry, collectionName);

			return entry;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public void deleteUserData(List<String> entryIds, Module module, String companyId) {
		entryIds.forEach(deleteDataId -> {
			removeUserFromAllEntries(module, deleteDataId, companyId);
		});
	}

	private void checkAndDeleteTeams(String fieldName, String deleteDataId, String ghostUserId, List<Module> allModules,
			String companyId) {
		List<String> roleNames = getAllRoleNames(companyId);

		List<String> deletedTeamIds = getDeletedTeamIds(fieldName, deleteDataId, roleNames, companyId);

		Map<String, Object> ghostTeam = getGhostTeam(companyId);
		String ghostTeamId = ghostTeam.get("_id").toString();

		allModules = allModules.stream().filter(module -> !module.getName().equalsIgnoreCase("Teams"))
				.collect(Collectors.toList());

		for (String teamId : deletedTeamIds) {
			allModules.forEach(module -> {
				String collectionName = moduleService.getCollectionName(module.getName(), companyId);

				moduleEntryRepository.findAndAddGhost("TEAMS", teamId, ghostTeamId, collectionName);
				moduleEntryRepository.findAndPull("TEAMS", teamId, collectionName);

//				removeDataFromSchedulesAndEscalations(companyId, ghostTeamId, deletedTeamId);
			});
		}

		moduleEntryRepository.deleteTeamEntries(deletedTeamIds, fieldName, companyId);
		moduleEntryRepository.findAndPull(fieldName, deleteDataId, "Teams_" + companyId);
	}

	public void removeUserFromAllEntries(Module currentModule, String deleteDataId, String companyId) {
		Map<String, Object> ghostUser = getGhostUser(companyId);
		String ghostId = ghostUser.get("_id").toString();

		List<Module> allModules = modulesRepository.findAll(Pageable.unpaged(), "modules_" + companyId).getContent();
		for (Module module : allModules) {

			List<ModuleField> relationshipFields = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
							&& !field.getRelationshipType().equalsIgnoreCase("One To Many") && field.getModule() != null
							&& field.getModule().equals(currentModule.getModuleId()))
					.collect(Collectors.toList());

			String collectionName = moduleService.getCollectionName(module.getName(), companyId);

			for (ModuleField relationshipField : relationshipFields) {
				if (module.getName().equals("Contacts") && relationshipField.getName().equals("USER")) {
					continue;
				}
				if (relationshipField.getRelationshipType().equalsIgnoreCase("Many To One")
						|| relationshipField.getRelationshipType().equalsIgnoreCase("One To One")) {

					moduleEntryRepository.findAndUpdateMany(relationshipField.getName(), ghostId, deleteDataId,
							collectionName);

				} else if (relationshipField.getRelationshipType().equalsIgnoreCase("Many To Many")) {
					if (!module.getName().equalsIgnoreCase("Teams")) {
						moduleEntryRepository.findAndAddGhost(relationshipField.getName(), deleteDataId, ghostId,
								collectionName);
						moduleEntryRepository.findAndPull(relationshipField.getName(), deleteDataId, collectionName);
					} else {
						checkAndDeleteTeams(relationshipField.getName(), deleteDataId, ghostId, allModules, companyId);
					}
				}
			}

			removeSenerFromDiscussion(currentModule, ghostUser, collectionName, deleteDataId, companyId);

		}
	}

	private void removeSenerFromDiscussion(Module module, Map<String, Object> ghostUser, String collectionName,
			String deleteDataId, String companyId) {
		Optional<ModuleField> optionalDiscussionField = module.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Discussion")).findAny();

		if (!optionalDiscussionField.isEmpty()) {
			ModuleField discussionField = optionalDiscussionField.get();
			Sender sender = new Sender(ghostUser.get("FIRST_NAME").toString(), ghostUser.get("LAST_NAME").toString(),
					ghostUser.get("USER_UUID").toString(), ghostUser.get("ROLE").toString());

			Map<String, Object> user = moduleEntryRepository.findEntryById(deleteDataId, "Users_" + companyId).get();

			moduleEntryRepository.updateSenderInDiscussion(discussionField.getName(), sender,
					user.get("USER_UUID").toString(), collectionName);
		}

	}

	public Map<String, Object> getGhostTeam(String companyId) {
		String collectionName = "Teams_" + companyId;
		Optional<Map<String, Object>> optionalGhostTeam = moduleEntryRepository.findEntryByFieldName("NAME",
				"Ghost Team", collectionName);

		if (optionalGhostTeam.isEmpty()) {
			throw new BadRequestException("GHOST_TEAM_NOT_FOUND", null);
		}
		return optionalGhostTeam.get();
	}

	public void checkDependencyAndUpdate(Module currentModule, List<String> entryIds, String companyId) {
		List<ModuleField> relationshipFields = currentModule.getFields().stream()
				.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
						&& !field.getRelationshipType().equalsIgnoreCase("Many To One"))
				.collect(Collectors.toList());

		for (ModuleField field : relationshipFields) {
			checkRelationshipDependency(field, entryIds, currentModule, companyId);
		}
	}

	private void checkRelationshipDependency(ModuleField field, List<String> entryIds, Module currentModule,
			String companyId) {
		Optional<Module> optionalRelatedModule = modulesRepository.findById(field.getModule(), "modules_" + companyId);

		if (optionalRelatedModule.isEmpty()) {
			return;
		}

		Module relatedModule = optionalRelatedModule.get();
		Optional<ModuleField> optionalRelatedField = relatedModule.getFields().stream()
				.filter(rField -> rField.getFieldId().equalsIgnoreCase(field.getRelationshipField())).findAny();
		if (optionalRelatedField.isEmpty()) {
			return;
		}
		ModuleField relatedField = optionalRelatedField.get();
		String relatedFieldName = relatedField.getName();

		if (relatedModule.getName().equals("Users") && relatedFieldName.equals("CONTACT")) {
			return;
		}

		boolean isRelatedFieldRequired = relatedField.getRequired();

		for (String dataId : entryIds) {
			// Temporary fix
			boolean flag = false;
			String collectionName = moduleService.getCollectionName(relatedModule.getName(), companyId);
			Optional<List<Map<String, Object>>> optionalEntries = moduleEntryRepository.findAllEntriesByFieldName(
					Arrays.asList(new String[] { dataId }), relatedField.getName(), collectionName);
			List<Map<String, Object>> entries = optionalEntries.get();
			for (Map<String, Object> entry : entries) {
				String[] vars = { relatedModule.getName(), relatedFieldName };
				if (field.getRelationshipType().equalsIgnoreCase("Many To Many")) {
					List<String> relationEntries = (List<String>) entry.get(relatedFieldName);
					if (relationEntries.size() >= 1 && isRelatedFieldRequired) {
						// Temporary fix
						flag = true;
						String cName = moduleService.getCollectionName(currentModule.getName(), companyId);
						Optional<Map<String, Object>> optionalValue = moduleEntryRepository.findById(dataId, cName);
						if (!optionalValue.isEmpty()) {
							Map<String, Object> value = optionalValue.get();
							value.put("DELETED", false);
							value.put("DATE_UPDATED", new Date());
							moduleEntryRepository.updateEntry(value, cName);

						}
						continue;

					}
				} else if (field.getRelationshipType().equalsIgnoreCase("One To Many")
						|| field.getRelationshipType().equalsIgnoreCase("One To One")) {
					// Temporary fix
					// TODO: CHECK WITH SHASHANK
					// The entry you are trying to delete has references in field TRANSPORT on
					// module Car please update those references before deleting it.
					if (isRelatedFieldRequired && entries.size() > 0) {
						flag = true;
						String cName = moduleService.getCollectionName(currentModule.getName(), companyId);
						Optional<Map<String, Object>> optionalValue = moduleEntryRepository.findById(dataId, cName);
						if (!optionalValue.isEmpty()) {
							Map<String, Object> value = optionalValue.get();
							value.put("DELETED", false);
							value.put("DATE_UPDATED", new Date());
							moduleEntryRepository.updateEntry(value, cName);

						}
						continue;
					}
				}
			}

			if (!flag) {

				if (field.getRelationshipType().equalsIgnoreCase("Many To Many")) {
					moduleEntryRepository.findAndPull(relatedFieldName, dataId, collectionName);
				} else if (field.getRelationshipType().equalsIgnoreCase("One To Many")
						|| field.getRelationshipType().equalsIgnoreCase("One To One")) {
					moduleEntryRepository.findAndUnset(relatedFieldName, dataId, collectionName);
				}

			}
		}

	}

	public Map<String, Object> deleteData(Module module, String dataId, String userId, String companyId) {
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
			throw new InternalErrorException("INTERNAL_ERROR");
		}
	}

	public void removeContactFromAllEntries(Module currentModule, String deleteDataId, String companyId) {

		Map<String, Object> ghostContact = getGhostContact(companyId);
		String ghostId = ghostContact.get("_id").toString();

		List<Module> allModules = modulesRepository.findAll(Pageable.unpaged(), "modules_" + companyId).getContent();
		for (Module module : allModules) {

			List<ModuleField> relationshipFields = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
							&& !field.getRelationshipType().equalsIgnoreCase("One To Many") && field.getModule() != null
							&& field.getModule().equals(currentModule.getModuleId()))
					.collect(Collectors.toList());

			String collectionName = moduleService.getCollectionName(module.getName(), companyId);

			for (ModuleField relationshipField : relationshipFields) {

				if (module.getName().equals("Users") && relationshipField.getName().equals("CONTACT")) {
					continue;
				}

				if (relationshipField.getRelationshipType().equalsIgnoreCase("Many To One")
						|| relationshipField.getRelationshipType().equalsIgnoreCase("One To One")) {

					moduleEntryRepository.findAndUpdateMany(relationshipField.getName(), ghostId, deleteDataId,
							collectionName);

				} else if (relationshipField.getRelationshipType().equalsIgnoreCase("Many To Many")) {

					moduleEntryRepository.findAndAddGhost(relationshipField.getName(), deleteDataId, ghostId,
							collectionName);
					moduleEntryRepository.findAndPull(relationshipField.getName(), deleteDataId, collectionName);
				}
			}
		}

	}

	public Map<String, Object> getGhostContact(String companyId) {
		String collectionName = "Contacts_" + companyId;
		Optional<Map<String, Object>> optionalGhostContact = moduleEntryRepository.findEntryByFieldName("FIRST_NAME",
				"Ghost", collectionName);

		if (optionalGhostContact.isEmpty()) {
			throw new BadRequestException("GHOST_CONTACT_NOT_FOUND", null);
		}
		return optionalGhostContact.get();
	}

	private void replaceGhostIdForAllEntry(Module module, String dataId, String companyId) {

		String collectionName = moduleService.getCollectionName(module.getName(), companyId);

		Map<String, Object> entry = moduleEntryRepository.findById(dataId, collectionName).get();

		String userUUID = "";
		if (module.getName().equalsIgnoreCase("Users")) {

			userUUID = entry.get("USER_UUID").toString();
			setGhostId(userUUID, companyId);

		} else if (module.getName().equalsIgnoreCase("Contacts")) {

			if (entry.get("USER") == null || entry.get("USER").toString().isEmpty()) {
				return;
			}

			String userId = entry.get("USER").toString();
			Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryById(userId,
					"Users_" + companyId);

			if (optionalUser.isPresent()) {
				Map<String, Object> userEntry = optionalUser.get();
				userUUID = userEntry.get("USER_UUID").toString();
				setGhostId(userUUID, companyId);
			}
		}
	}

	public void setGhostId(String userUUID, String companyId) {

		List<Module> allModules = modulesRepository.findAll(Pageable.unpaged(), "modules_" + companyId).getContent();
		for (Module module : allModules) {
			List<ModuleField> discussionFields = module.getFields().stream()
					.filter(field -> field.getDataType().getDisplay().equalsIgnoreCase("Discussion"))
					.collect(Collectors.toList());

			String collectionName = moduleService.getCollectionName(module.getName(), companyId);

			List<Map<String, Object>> entries = moduleEntryRepository.findAllEntries(collectionName).get();
			ObjectMapper mapper = new ObjectMapper();

			for (ModuleField discussionField : discussionFields) {
				for (Map<String, Object> entry : entries) {

					try {
						List<DiscussionMessage> messages = mapper.readValue(
								mapper.writeValueAsString(entry.get(discussionField.getName())),
								mapper.getTypeFactory().constructCollectionType(List.class, DiscussionMessage.class));
						Map<String, Object> ghostContact = getGhostContact(companyId);
						Map<String, Object> ghostUser = getGhostUser(companyId);

						List<DiscussionMessage> updatedMessages = new ArrayList<>();
						for (DiscussionMessage discussionMessage : messages) {
							if (userUUID.equals(discussionMessage.getSender().getUserUuid())) {
								Sender sender = new Sender(ghostContact.get("FIRST_NAME").toString(),
										ghostContact.get("LAST_NAME").toString(), ghostUser.get("USER_UUID").toString(),
										ghostUser.get("ROLE").toString());

								discussionMessage.setSender(sender);

							}
							updatedMessages.add(discussionMessage);
						}

						entry.put(discussionField.getName(), updatedMessages);
						moduleEntryRepository.updateEntry(entry, collectionName);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	private List<String> getAllRoleNames(String companyId) {
		List<Role> roles = rolesRepository.findAll(Pageable.unpaged(), "roles_" + companyId).getContent();

		List<String> roleNames = new ArrayList<String>();
		roles.forEach(role -> roleNames.add(role.getName()));

		return roleNames;
	}

	private List<String> getDeletedTeamIds(String fieldName, String deleteDataId, List<String> roleNames,
			String companyId) {
		List<String> teamIds = new ArrayList<String>();
		Optional<List<Map<String, Object>>> optionalTeams = moduleEntryRepository.findAllTeamIdsToDelete(fieldName,
				deleteDataId, roleNames, "Teams_" + companyId);

		if (!optionalTeams.isEmpty()) {
			List<Map<String, Object>> deletedTeams = optionalTeams.get();
			deletedTeams.forEach(team -> {
				teamIds.add(team.get("_id").toString());
			});
		}
		return teamIds;
	}

	public Map<String, Object> getGhostUser(String companyId) {
		String collectionName = "Users_" + companyId;
		Optional<Map<String, Object>> optionalGhostUser = moduleEntryRepository.findEntryByFieldName("EMAIL_ADDRESS",
				"ghost@ngdesk.com", collectionName);

		if (optionalGhostUser.isEmpty()) {
			throw new BadRequestException("GHOST_USER_NOT_FOUND", null);
		}
		return optionalGhostUser.get();
	}

}
