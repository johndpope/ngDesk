package com.ngdesk.jobs;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;

//@Component
public class SamInstallerJob {

	private final static Logger log = LoggerFactory.getLogger(SamInstallerJob.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Value("${env}")
	String environment;

	@Value("${email.host}")
	String host;

	@Value("${sam.builder.xml.path}")
	String xmlOutputPath;

	@Value("${sam.installbuilder.path}")
	String installBuilderPath;

	// Run once every 1 minutes
	@Scheduled(fixedRate = 60000)
	public void buildInstallers() {
		log.trace("Enter SamInstallerJob.buildInstallers()");
		try {
			
			MongoCollection<Document> samInstallerCollection = mongoTemplate.getCollection("sam_installers");
			List<Document> builders = samInstallerCollection.find(Filters.eq("STATUS", "QUEUED"))
					.into(new ArrayList<Document>());
			
			for (Document builder : builders) {
				String platform = builder.getString("PLATFORM");
				String subdomain = builder.getString("COMPANY_SUBDOMAIN");
				String buildXmlPath = xmlOutputPath + "build_" + subdomain + "_" + platform + ".xml";
				
				Process buildProcess = Runtime.getRuntime()
						.exec(installBuilderPath + " build " + buildXmlPath + " " + platform);
				buildProcess.waitFor();

				String fileType = null;

				if (platform.equals("windows")) {
					fileType = ".exe";
				} else if (platform.equals("windows-x64")) {
					fileType = ".exe";
				} else if (platform.equals("linux")) {
					fileType = ".run";
				} else if (platform.equals("linux-x64")) {
					fileType = ".run";
				} else if (platform.equals("osx")) {
					fileType = ".app";
				}

				if (buildProcess.exitValue() == 0) {

					if (platform.equals("osx")) {

						String fileSep = System.getProperty("file.separator");

//						String command1 = "cd /opt/installers; zip -r " + "ngDesk_Controller_" + subdomain + fileType
//								+ ".zip " + "ngDesk_Controller_" + subdomain + fileType;
//
//						Runtime.getRuntime().exec(command1).waitFor();

						ProcessBuilder builder1 = new ProcessBuilder("bash", "-c", "zip -r " + "ngDesk_Controller_" + subdomain
								+ fileType + ".zip " + "ngDesk_Controller_" + subdomain + fileType).directory(new File("/opt/installers"));
						Process process = builder1.start();
						process.waitFor();
						
//						String command2 = "mv ngDesk_Controller_" + subdomain + fileType
//								+ ".zip /opt/installers/" + "ngDesk_Controller_" + subdomain + fileType + ".zip";
//						Runtime.getRuntime().exec(command2).waitFor();

						Runtime.getRuntime().exec("rm -rf " + fileSep + "opt" + fileSep + "installers" + fileSep
								+ "ngDesk_Controller_" + subdomain + fileType).waitFor();
					}

					samInstallerCollection.updateOne(Filters.eq("_id", builder.getObjectId("_id")),
							Updates.set("STATUS", "COMPLETED"));

					Document company = global.getCompanyFromSubdomain(subdomain);
					String companyId = company.getObjectId("_id").toString();

					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					Document user = usersCollection.find(Filters.and(Filters.eq("DELETED", false),
							Filters.eq("_id", new ObjectId(builder.getString("REQUESTOR"))))).first();

					String emailAddress = user.getString("EMAIL_ADDRESS");

					String emailSubject = "Installer ready to download";
					String messageBody = global.getFile("installer_ready_email.html");
					String from = "support@ngdesk.com";
					String name = "";
					if (user.containsKey("FIRST_NAME") && user.get("FIRST_NAME") != null) {
						name = name + user.getString("FIRST_NAME") + " ";
					}

					if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
						name = name + user.getString("LAST_NAME");
					}
					messageBody = messageBody.replace("NAME_REPLACE", name);
					messageBody = messageBody.replace("PLATFORM_REPLACE", platform);
					messageBody = messageBody.replace("SUBDOMAIN_REPLACE", subdomain);

					SendEmail sendEmail = new SendEmail(emailAddress, from, emailSubject, messageBody, host);
					sendEmail.sendEmail();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}
		}
		log.trace("Exit SamInstallerJob.buildInstallers()");
	}
}
