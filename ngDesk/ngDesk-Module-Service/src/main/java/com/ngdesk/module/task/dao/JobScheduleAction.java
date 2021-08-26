package com.ngdesk.module.task.dao;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.task.TaskRepository;

@Component
public class JobScheduleAction {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	AuthManager auth;

	@Autowired
	CreateEntry create;

	@Scheduled(fixedRate = 60 * 1000)
	public void run() {
		ZonedDateTime zone = null;
		ZonedDateTime zonedDate = null;
		List<Task> existingTask = taskRepository.getAllTasks("tasks");
		for (Task task : existingTask) {
			final ZoneId id = ZoneId.of(task.getTimezone());
			ZonedDateTime now = ZonedDateTime.now().toInstant().atZone(id);
			ZonedDateTime zonedStartDate = ZonedDateTime
					.ofInstant(task.getStartDate().toInstant().truncatedTo(ChronoUnit.MINUTES), id);
			ZonedDateTime zonedCurrentDate = now.truncatedTo(ChronoUnit.MINUTES);

			if (task.getDateCreated() != null) {
				if (task.getLastExecuted() == null) {
					if (task.isRecurrence() == false) {
						if (task.getStartDate() != null) {
							if (zonedStartDate.isEqual(zonedCurrentDate) || zonedStartDate.isBefore(zonedCurrentDate)) {
								if (task.getAction() != null) {

									List<Action> actions = task.getAction();
									for (Action taskAction : actions) {
										try {
											task.setLastExecuted(new Date());
											taskRepository.updateLastExecutedDate(task.getTaskId(),
													task.getCompanyId());
											create.executeCreateEntry(taskAction, task.getCompanyId(),
													task.getCreatedBy());
										} catch (Exception e) {
											e.printStackTrace();
										}

									}
								}
							}
						}
					}
				}
				if (task.isRecurrence() == true) {
					ZonedDateTime zonedStopDate = ZonedDateTime
							.ofInstant(task.getStopDate().toInstant().truncatedTo(ChronoUnit.MINUTES), id);
					if (zonedStopDate.isAfter(zonedCurrentDate) || zonedStopDate.equals(zonedCurrentDate)) {
						if (task.getStartDate() != null) {
							if (task.getIntervals() != null) {
								if (task.getLastExecuted() == null) {
									if (zonedStartDate.isEqual(zonedCurrentDate)
											|| zonedStartDate.isBefore(zonedCurrentDate)) {
										if (task.getAction() != null) {

											List<Action> actions = task.getAction();
											for (Action taskAction : actions) {
												try {
													task.setLastExecuted(new Date());
													taskRepository.updateLastExecutedDate(task.getTaskId(),
															task.getCompanyId());
													create.executeCreateEntry(taskAction, task.getCompanyId(),
															task.getCreatedBy());
												} catch (Exception e) {
													e.printStackTrace();
												}

											}
										}
									}
								}
								String intervalType = task.getIntervals().getIntervalType();
								int intervalValue = task.getIntervals().getIntervalValue();

								if (task.getLastExecuted() == null) {
									zone = ZonedDateTime.ofInstant(task.getStartDate().toInstant(), id);
								} else {
									zone = ZonedDateTime.ofInstant(task.getLastExecuted().toInstant(), id);
								}
								if (intervalType.equalsIgnoreCase("Hour")) {
									zonedDate = zone.plusHours(intervalValue);
								} else if (intervalType.equalsIgnoreCase("Day")) {
									zonedDate = zone.plus(Period.ofDays(intervalValue));
								} else if (intervalType.equalsIgnoreCase("Month")) {
									zonedDate = zone.plus(Period.ofMonths(intervalValue));
								} else if (intervalType.equalsIgnoreCase("Year")) {
									zonedDate = zone.plus(Period.ofYears(intervalValue));
								} else if (intervalType.equalsIgnoreCase("Week")) {
									zonedDate = zone.plus(Period.ofWeeks(intervalValue));
								} else if (intervalType.equalsIgnoreCase("Quarter")) {
									zonedDate = zone.plus(Period.ofMonths(intervalValue * (3)));
								} else if (intervalType.equalsIgnoreCase("Half Year")) {
									zonedDate = zone.plus(Period.ofMonths(intervalValue * (6)));
								}
								if (zonedDate.truncatedTo(ChronoUnit.MINUTES).isEqual(zonedCurrentDate)) {

									if (task.getAction() != null) {
										List<Action> actions = task.getAction();
										for (Action taskAction : actions) {
											try {
												task.setLastExecuted(new Date());
												taskRepository.updateLastExecutedDate(task.getTaskId(),
														task.getCompanyId());
												create.executeCreateEntry(taskAction, task.getCompanyId(),
														task.getCreatedBy());
											} catch (Exception e) {
												e.printStackTrace();
											}

										}
									}

								}

							}
						}
					}

				}

			}

		}
	}
}
