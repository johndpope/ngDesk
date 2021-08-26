package com.ngdesk.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@Component
@RestController
public class PendingInvitesService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	RoleService roleService;

	private final Logger log = LoggerFactory.getLogger(PendingInvitesService.class);

	@GetMapping("/companies/users/invite/pending")
	public ResponseEntity<Object> getPendingInvites(HttpServletRequest request,
			@RequestParam(value = "authentication_token") String uuid,
			@RequestParam(value = "page_size", required = false) String pageSize,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order) {

		JSONArray users = new JSONArray();
		JSONObject resultObj = new JSONObject();
		int totalSize = 0;

		try {

			log.trace("Enter PendingInvitesService.getPendingInvites()");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}

			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			int pgSize = 100;
			int pg = 1;
			int skip = 0;
			String collectionName = "Users_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			totalSize = (int) collection.countDocuments(
					Filters.and(Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
							Filters.eq("INVITE_ACCEPTED", false), Filters.eq("DELETED", false)));
			List<Document> pendingUsers = null;

			List<Document> rolesList = rolesCollection.find().into(new ArrayList<Document>());
			Map<String, String> rolesMap = new HashMap<String, String>();

			for (Document roleDocument : rolesList) {
				String roleId = roleDocument.getObjectId("_id").toString();
				String roleName = roleDocument.getString("NAME");
				rolesMap.put(roleId, roleName);
			}

			if (pageSize != null && page != null) {
				pgSize = Integer.valueOf(pageSize);
				pg = Integer.valueOf(page);

				if (pgSize <= 0) {
					throw new BadRequestException("INVALID_PAGE_SIZE");
				} else if (pg <= 0) {
					throw new BadRequestException("INVALID_PAGE_NUMBER");
				} else {
					skip = (pg - 1) * pgSize;
				}
			}

			if (sort != null && order != null) {
				if (order.equalsIgnoreCase("asc")) {
					pendingUsers = collection
							.find(Filters.and(
									Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
									Filters.eq("INVITE_ACCEPTED", false), Filters.eq("DELETED", false)))
							.sort(Sorts.orderBy(Sorts.ascending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else if (order.equalsIgnoreCase("desc")) {
					pendingUsers = collection
							.find(Filters.and(
									Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
									Filters.eq("INVITE_ACCEPTED", false), Filters.eq("DELETED", false)))
							.sort(Sorts.orderBy(Sorts.descending(sort))).skip(skip).limit(pgSize)
							.into(new ArrayList<Document>());
				} else {
					throw new BadRequestException("INVALID_SORT_ORDER");
				}

			} else {
				pendingUsers = collection
						.find(Filters.and(
								Filters.or(Filters.eq("EFFECTIVE_TO", null), Filters.exists("EFFECTIVE_TO", false)),
								Filters.eq("INVITE_ACCEPTED", false), Filters.eq("DELETED", false)))
						.skip(skip).limit(pgSize).into(new ArrayList<Document>());
			}

			for (Document document : pendingUsers) {

				String userId = document.getObjectId("_id").toString();
				document.remove("_id");
				JSONObject userJson = new JSONObject(document.toJson());
				userJson.put("USER_ID", userId);
				userJson.put("ROLE", rolesMap.get(userJson.getString("ROLE")));
				userJson.remove("PASSWORD");

				users.put(userJson);
			}
			resultObj.put("PENDING_INVITES", users);
			resultObj.put("TOTAL_RECORDS", totalSize);
			log.trace("Exit PendingInvitesService.getPendingInvites()");
			return new ResponseEntity<>(resultObj.toString(), Global.postHeaders, HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
