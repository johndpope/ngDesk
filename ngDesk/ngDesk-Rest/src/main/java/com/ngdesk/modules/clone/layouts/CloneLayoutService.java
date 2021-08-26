package com.ngdesk.modules.clone.layouts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class CloneLayoutService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	Global global;

	private final Logger log = LoggerFactory.getLogger(CloneLayoutService.class);

	@PostMapping("/modules/{module_id}/clone_layouts")
	public ResponseEntity<Object> postLayout(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("module_id") String moduleId, @RequestParam("layout_id") String layoutId,
			@RequestParam("role_id") String roleId, @RequestParam("type") String type) {

		JSONObject layout = null;

		try {
			log.trace("Enter CloneLayoutService.postLayout()");

			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token");
			}
			JSONObject user = auth.getUserDetails(uuid);
			String companyId = user.getString("COMPANY_ID");
			String userId = user.getString("USER_ID");
			if (!ObjectId.isValid(moduleId)) {
				throw new BadRequestException("INVALID_MODULE_ID");
			}
			String moduleCollectionName = "modules_" + companyId;
			MongoCollection<Document> collection = mongoTemplate.getCollection(moduleCollectionName);

			Document module = collection.find(Filters.eq("_id", new ObjectId(moduleId))).first();
			if (module == null) {
				throw new ForbiddenException("MODULE_NOT_EXISTS");
			}
			if (!roleService.isAuthorizedForModule(userId, "POST", moduleId, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!new ObjectId().isValid(roleId)) {
				throw new BadRequestException("INVALID_ROLE_ID");
			}

			MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);
			Document roleDocument = rolesCollection.find(Filters.eq("_id", new ObjectId(roleId))).first();
			if (roleDocument == null) {
				throw new BadRequestException("ROLE_DOES_NOT_EXIST");
			}

			if (module.containsKey(type)) {
				ArrayList<Document> layoutDocuments = (ArrayList) module.get(type);

				if (layoutDocuments.size() > 0) {
					for (Document layoutDocument : layoutDocuments) {
						if (layoutDocument.getString("LAYOUT_ID").equals(layoutId)) {
							layout = new JSONObject(layoutDocument.toJson());
							break;
						}
					}
				}
			} else {
				throw new BadRequestException("LAYOUT_TYPE_INVALID");
			}

			if (layout != null) {
				String layoutName = layout.getString("NAME");
				layout.put("ROLE", roleId);
				layout.put("DATE_CREATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
				layout.put("CREATED_BY", userId);
				layout.put("LAYOUT_ID", UUID.randomUUID().toString());
				layout.put("NAME", layoutName + " copy");
				if (type.equals("LIST_LAYOUTS")) {
					layout.put("IS_DEFAULT", false);
				}
				Document layoutDoc = Document.parse(layout.toString());
				collection.updateOne(Filters.eq("_id", new ObjectId(moduleId)), Updates.addToSet(type, layoutDoc));
				log.trace("Exit CloneLayoutService.postLayout()");
				return new ResponseEntity<>(layout.toString(), Global.postHeaders, HttpStatus.OK);
			} else {
				throw new ForbiddenException("LAYOUT_DOES_NOT_EXIST");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}
