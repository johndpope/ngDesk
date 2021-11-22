package com.ngdesk.report.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleJobForReportCsv {

	@Scheduled(fixedRate = 60 * 1000)
	public void schedule() {

		File directoryPath = new File("/opt/");
		File filesList[] = directoryPath.listFiles();
		if (filesList != null) {
			for (File file : filesList) {
				if (file.getName().contains(".csv")) {
					try {
						BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
						FileTime fileTime = attr.creationTime();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
						Date fileDateCreated = dateFormat.parse(dateFormat.format(fileTime.toMillis()));

						Calendar calendar = Calendar.getInstance();
						calendar.setTime(fileDateCreated);
						calendar.add(Calendar.DATE, 1);

						if (calendar.getTime().before(new Date())) {
							file.deleteOnExit();
							if (file.delete()) {
							}
						}

					} catch (IOException ex) {
						ex.printStackTrace();

					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
