package com.ngdesk.module.task.dao;

import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.repositories.task.TaskRepository;

@Component
public class BeforeSaveListner extends AbstractMongoEventListener<Task> {
	@Autowired
	TaskRepository taskRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Task> event) {

		Task task = event.getSource();
		checkForDuplicates(task);

		checkValidModuleId(task.getModuleId(), "modules_" + task.getCompanyId());

		checkValidationForFields(task, "modules_" + task.getCompanyId());

		checkValidationForRecurrence(task);

	}

	public void checkValidationForFields(Task task, String collectionName) {

		List<Action> actions = task.getAction();

		for (Action taskAction : actions) {

			CreateEntry createEntry = (CreateEntry) taskAction;
			Optional<Module> existingField = taskRepository.findByModuleId(createEntry.getModuleId(), collectionName);
			if (existingField.isEmpty()) {

				throw new BadRequestException("NOT_VALID_MODULE_ID", null);
			}
			List<ModuleField> moduleFields = existingField.get().getFields();
			List<Fields> taskFields = createEntry.getFields();
			List<String> fieldIds = new ArrayList<String>();

			for (Fields taskField : taskFields) {
				fieldIds.add(taskField.getFieldId());
				Optional<ModuleField> fieldId = moduleFields.stream()
						.filter(moduleField -> moduleField.getFieldId().equals(taskField.getFieldId())).findAny();
				if (!fieldId.isPresent()) {
					throw new BadRequestException("INVALID_FIELDS_ID", null);
				}
			}

			for (ModuleField moduleField : moduleFields) {
				if (moduleField.getRequired()) {
					if ((moduleField.getDefaultValue() == null || moduleField.getDefaultValue().isBlank())) {
						String displayType = moduleField.getDataType().getDisplay();
						String moduleFieldId = moduleField.getFieldId();
						if (!fieldIds.contains(moduleFieldId) && !displayType.equalsIgnoreCase("Auto Number")) {
							String[] var = { moduleField.getDisplayLabel() };
							throw new BadRequestException("CREATE_ENTRY_REQUIRED_FIELD_MISSING", var);
						}
					}
				}
			}

		}

	}

	public String checkValidModuleId(String moduleId, String companyId) {

		Optional<Module> module_Id = taskRepository.findByModuleId(moduleId, companyId);

		if (!module_Id.isEmpty()) {

			String moduleName = module_Id.get().getName();

			return moduleName;
		}
		if (module_Id.isEmpty()) {

			throw new BadRequestException("NOT_VALID_MODULE_ID", null);
		}
		return null;
	}

	public void checkValidationForRecurrence(Task task) {
		final ZoneId id = ZoneId.of(task.getTimezone());
		ZonedDateTime now = ZonedDateTime.now().toInstant().atZone(id);
		ZonedDateTime zonedCurrentDate = now.truncatedTo(ChronoUnit.MINUTES);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss a");
		ZonedDateTime zonedStartDate = ZonedDateTime
				.ofInstant(task.getStartDate().toInstant().truncatedTo(ChronoUnit.MINUTES), id);
		if (task.isRecurrence() == true) {
			if (task.getStopDate() == null || task.getStopDate().toString().isBlank()) {
				throw new BadRequestException("STOP_DATE_REQUIRED", null);

			}
			ZonedDateTime zonedStopDate = ZonedDateTime
					.ofInstant(task.getStopDate().toInstant().truncatedTo(ChronoUnit.MINUTES), id);

			Pattern intervalTpyePattern = Pattern.compile("Hour|Day|Week|Month|Quarter|Half Year|Year");
			Matcher intervalMatcher = intervalTpyePattern.matcher(task.getIntervals().getIntervalType());

			if (!intervalMatcher.find()) {

				throw new BadRequestException("INTERVAL_TYPE_REQUIRED", null);
			}

			if (task.getIntervals().getIntervalValue() <= 0) {

				throw new BadRequestException("INTERVAL_VALUE_REQUIRED", null);
			}
			ZonedDateTime date = dateConversion(task);
			if (zonedStopDate.isBefore(zonedCurrentDate) || zonedStopDate.equals(zonedCurrentDate)) {
				Date covertedDate = Date.from(date.toInstant());
				String[] vars = { covertedDate.toInstant().atZone(id).format(formatter) + "["
						+ task.getTimezone().toString() + "]" };
				throw new BadRequestException("STOP_DATE_NOT_VALID", vars);
			}

			if ((task.getLastExecuted() != null) || (task.getDateCreated() != null)) {
				if (ZonedDateTime.ofInstant(task.getStopDate().toInstant(), id) != null) {
					if (ZonedDateTime.ofInstant(task.getStopDate().toInstant(), id).isBefore(date)) {
						Date convertedDate = Date.from(date.toInstant());
						String[] vars = { convertedDate.toInstant().atZone(id).format(formatter) + "["
								+ task.getTimezone().toString() + "]" };
						throw new BadRequestException("STOP_DATE_NOT_VALID", vars);
					}
				}
			}

		}
		if (zonedStartDate.equals(zonedCurrentDate) || zonedStartDate.isBefore(zonedCurrentDate)) {
			Date date = Date.from(zonedCurrentDate.toInstant());
			String[] vars = {
					date.toInstant().atZone(id).format(formatter) + "[" + task.getTimezone().toString() + "]" };
			throw new BadRequestException("START_DATE_NOT_VALID", vars);
		}

	}

	public void checkForDuplicates(Task task) {

		if (task.getTaskId() == null) {
			Optional<Task> optional = taskRepository.findTaskByName(task.getTaskName(), task.getCompanyId(),
					task.getModuleId(), "tasks");

			if (optional.isPresent()) {
				String[] variables = { "TASK", "NAME" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		} else {
			Optional<Task> optional = taskRepository.findOtherTaskWithDuplicateName(task.getTaskName(),
					task.getCompanyId(), task.getTaskId(), task.getModuleId(), "tasks");
			if (optional.isPresent()) {
				String[] variables = { "TASK", "NAME" };

				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		}

	}

	public ZonedDateTime dateConversion(Task task) {
		ZonedDateTime zone = null;
		final ZoneId id = ZoneId.of(task.getTimezone());
		ZonedDateTime zonedTime = null;
		String intervalType = task.getIntervals().getIntervalType();

		int intervalValue = task.getIntervals().getIntervalValue();

		if (task.getLastExecuted() == null) {
			zone = ZonedDateTime.ofInstant(task.getStartDate().toInstant(), id);

		} else {
			zone = ZonedDateTime.ofInstant(task.getLastExecuted().toInstant(), id);
		}
		if (intervalType.equalsIgnoreCase("Hour")) {
			zonedTime = zone.plusHours(intervalValue);
		} else if (intervalType.equalsIgnoreCase("Day")) {
			zonedTime = zone.plus(Period.ofDays(intervalValue));
		} else if (intervalType.equalsIgnoreCase("Month")) {
			zonedTime = zone.plus(Period.ofMonths(intervalValue));
		} else if (intervalType.equalsIgnoreCase("Year")) {
			zonedTime = zone.plus(Period.ofYears(intervalValue));
		} else if (intervalType.equalsIgnoreCase("Week")) {
			zonedTime = zone.plus(Period.ofWeeks(intervalValue));
		} else if (intervalType.equalsIgnoreCase("Quarter")) {
			zonedTime = zone.plus(Period.ofMonths(intervalValue * (3)));
		} else if (intervalType.equalsIgnoreCase("Half Year")) {
			zonedTime = zone.plus(Period.ofMonths(intervalValue * (6)));
		}
		return zonedTime;

	}

}
