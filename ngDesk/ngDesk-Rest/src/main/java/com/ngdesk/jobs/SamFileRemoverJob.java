package com.ngdesk.jobs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
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
public class SamFileRemoverJob {

	private final static Logger log = LoggerFactory.getLogger(SamFileRemoverJob.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Value("${env}")
	String environment;

	@Value("${email.host}")
	String host;

	@Value("${sam.installer.path}")
	String buildInstallerPath;

	@Value("${sam.builder.xml.path}")
	String xmlOutputPath;

	// Run once every 1 minutes
	@Scheduled(fixedRate = 60000)
	public void removeInstallers() {
		log.trace("Enter SamFileRemoverJob.removeInstallers()");
		try {

			MongoCollection<Document> samInstallerCollection = mongoTemplate.getCollection("sam_installers");

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -1);
			Date previousDate = calendar.getTime();
			String date = global.getFormattedDate(new Timestamp(previousDate.getTime()));

			List<Document> builders = samInstallerCollection
					.find(Filters.and(Filters.eq("STATUS", "COMPLETED"), Filters.lte("DATE_CREATED", date)))
					.into(new ArrayList<Document>());

			for (Document builder : builders) {
				
				String platform = builder.getString("PLATFORM");
				String subdomain = builder.getString("COMPANY_SUBDOMAIN");
				
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
					fileType = ".app.zip";
				}
				
				String xmlPath = xmlOutputPath + System.getProperty("file.separator") + "build_" + subdomain + "_" + platform + ".xml";
				String installerPath = buildInstallerPath + System.getProperty("file.separator") + "ngDesk_Controller_"
						+ subdomain + fileType;
				
				Process removeXml = Runtime.getRuntime().exec("rm -f " + xmlPath);
				BufferedReader reader = new BufferedReader(new InputStreamReader(removeXml.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
				removeXml.waitFor();
				
				Process removeInstaller = Runtime.getRuntime().exec("rm -f " + installerPath);
				BufferedReader read = new BufferedReader(new InputStreamReader(removeInstaller.getInputStream()));
				String line2;
				while ((line2 = read.readLine()) != null) {
					System.out.println(line2);
				}
				removeInstaller.waitFor();

				samInstallerCollection.findOneAndDelete(Filters.eq("_id", builder.getObjectId("_id")));

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
		log.trace("Exit SamFileRemoverJob.removeInstallers()");
	}
}
