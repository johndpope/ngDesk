package com.ngdesk.sam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class InstallerService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Authentication auth;

	@Autowired
	private RoleService roleService;

	@Autowired
	private Global global;

	@Value("${sam.installer.nssm.path}")
	String nssmPath;

	@Value("${sam.installer.nssm.x64.path}")
	String nssm64Path;

	@Value("${sam.installer.controller.path}")
	String controllerPath;

	@Value("${sam.installer.controllerupdater.path}")
	String controllerUpdaterPath;

	@Value("${sam.installer.uuidgenerator.path}")
	String uuidGeneratorPath;

	@Value("${sam.installer.windows.java.path}")
	String javaPathWindows;

	@Value("${sam.installer.linux.java.path}")
	String javaPathLinux;

	@Value("${sam.installer.osx.java.path}")
	String javaPathOsx;

	@Value("${sam.builder.xml.path}")
	String xmlOutputPath;

	@Value("${sam.installer.path}")
	String buildInstallerPath;

	@Value("${sam.installer.init.script.path}")
	String initScriptPath;

	@Value("${sam.logo.path}")
	String logoPath;

	private final static Logger log = LoggerFactory.getLogger(InstallerService.class);

	@GetMapping("sam/installer/build")
	public ResponseEntity<Object> buildInstaller(
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@RequestParam(value = "platform") String platform) {

		log.trace("Enter InstallerService.buildInstaller()");
		try {

			JSONObject user = auth.getUserDetails(uuid);
			String role = user.getString("ROLE");
			String companyId = user.getString("COMPANY_ID");
			String subdomain = user.getString("COMPANY_SUBDOMAIN");
			String userId = user.getString("USER_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
			Document userDocument = usersCollection
					.find(Filters.and(Filters.eq("DELETED", false), Filters.eq("_id", new ObjectId(userId)))).first();

			if (userDocument == null) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (!Global.validInstallerPlatforms.contains(platform)) {
				throw new BadRequestException("INSTALLER_PLATFORM_NOT_SUPPORTED");
			}

			String builderXml = null;

			if (platform.equals("windows")) {
				builderXml = global.getFile("installer-windows.xml");
				builderXml = builderXml.replaceAll("NSSM_EXE_PATH_REPLACE", nssmPath);
				builderXml = builderXml.replaceAll("WINDOWS_JAVA_PATH_REPLACE", javaPathWindows);
			} else if (platform.equals("windows-x64")) {
				builderXml = global.getFile("installer-windows64.xml");
				builderXml = builderXml.replaceAll("NSSM_EXE_PATH_REPLACE", nssm64Path);
				builderXml = builderXml.replaceAll("WINDOWS_JAVA_PATH_REPLACE", javaPathWindows);
			} else if (platform.equals("linux")) {
				builderXml = global.getFile("installer-linux.xml");
				builderXml = builderXml.replaceAll("LINUX_JAVA_PATH_REPLACE", javaPathLinux);
			} else if (platform.equals("linux-x64")) {
				builderXml = global.getFile("installer-linux64.xml");
				builderXml = builderXml.replaceAll("LINUX_JAVA_PATH_REPLACE", javaPathLinux);
			} else if (platform.equals("osx")) {
				builderXml = global.getFile("installer-osx.xml");
				builderXml = builderXml.replaceAll("OSX_JAVA_PATH_REPLACE", javaPathOsx);
			}

			// GETTING THE USER USED FOR POSTING CONTROLLER
			MongoCollection<Document> apiKeysCollection = mongoTemplate.getCollection("api_keys");
			Document apiKey = apiKeysCollection.find(Filters.and(Filters.eq("NAME", "Registration API Key"),
					Filters.eq("INTERNAL", true), Filters.eq("COMPANY_ID", companyId))).first();

			// Replace all variables on xml
			builderXml = builderXml.replaceAll("SUBDOMAIN_REPLACE", subdomain);
			builderXml = builderXml.replaceAll("REGISTRATION_API_KEY_REPLACE", apiKey.getString("TOKEN"));
			builderXml = builderXml.replaceAll("CONTROLLER_JAR_PATH_REPLACE", controllerPath);
			builderXml = builderXml.replaceAll("UPDATER_JAR_PATH_REPLACE", controllerUpdaterPath);
			builderXml = builderXml.replaceAll("GENERATOR_JAR_PATH_REPLACE", uuidGeneratorPath);
			builderXml = builderXml.replaceAll("LOGO_PATH_REPLACE", logoPath);

			builderXml = builderXml.replaceAll("FILE_NAME_REPLACE", "ngDesk_Controller_" + subdomain);
			builderXml = builderXml.replaceAll("INSTALL_PATH", buildInstallerPath);

			// DELETE EXISTING BUILD FILES AND RECREATE THEM

			String xmlPath = xmlOutputPath + System.getProperty("file.separator") + "build_" + subdomain + "_"
					+ platform + ".xml";

			File oldFile = new File(xmlPath);
			oldFile.delete();

			// WRITE INTO FILE FROM XML TEMPLATE
			File file = new File(xmlPath);
			file.createNewFile();
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(builderXml);
			output.close();

			// INSERT AN ENTRY FOR THE JOB TO BUILD THE INSTALLER
			MongoCollection<Document> samInstallerCollection = mongoTemplate.getCollection("sam_installers");
			Document document = new Document();
			document.put("COMPANY_SUBDOMAIN", subdomain);
			document.put("STATUS", "QUEUED");
			document.put("DATE_CREATED", global.getFormattedDate(new Timestamp(new Date().getTime())));
			document.put("REQUESTOR", userDocument.getObjectId("_id").toString());
			document.put("PLATFORM", platform);
			samInstallerCollection.insertOne(document);

			return new ResponseEntity<Object>(HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.trace("Exit InstallerService.buildInstaller()");
		throw new InternalErrorException("INTERNAL_ERROR");

	}
}
