package com.ngdesk.report.dao;

import java.util.List;
import java.util.Optional;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.report.module.dao.Module;
import com.ngdesk.report.module.dao.ModuleField;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.ReportRepository;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<Report> {

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private ModulesRepository moduleRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Report> event) {
		Report report = event.getSource();
		uniqueName(report, event.getCollectionName());
		validModule(report);
		validEmails(report);
	}

	private void uniqueName(Report report, String collectionName) {
		if (report.getReportId() == null) {
			Optional<Report> optional = reportRepository.findReportByName(report.getReportName(), collectionName);
			if (optional.isPresent()) {
				String[] vars = { "REPORT", "name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
			}
		} else {
			Optional<Report> optional = reportRepository.findOtherReportWithDuplicateName(report.getReportName(),
					report.getReportId(), collectionName);
			if (optional.isPresent()) {
				String[] vars = { "REPORT", "name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
			}
		}
	}

	private void validModule(Report report) {
		Optional<Module> optionalModule = moduleRepository.findById(report.getModule(),
				"modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {
			String[] vars = {};
			throw new BadRequestException("MODULE_INVALID", vars);
		}
		validReportField(report, optionalModule.get());
		validReportFilter(report, optionalModule.get());
		validSortBy(report, optionalModule.get());
	}

	private void validReportField(Report report, Module module) {
		List<ModuleField> fields = module.getFields();
		List<ReportField> reportFields = report.getFields();
		for (ReportField reportField : reportFields) {

			String[] arrayOfFieldIds = reportField.getFieldId().split("[.]");
			Optional<ModuleField> optionalField = fields.stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(arrayOfFieldIds[0])).findFirst();

			if (optionalField.isEmpty()) {
				String[] vars = { module.getName() };
				throw new BadRequestException("FIELD_INVALID", vars);
			}

			validateRelatedOneToManyField(optionalField.get(), arrayOfFieldIds);
		}
	}

	private void validateRelatedOneToManyField(ModuleField field, String[] arrayOfFieldIds) {
		if (field.getDataType().getDisplay().equalsIgnoreCase("Relationship")
				&& field.getRelationshipType().equalsIgnoreCase("one to many")) {

			Optional<Module> optionalRelatedModule = moduleRepository.findById(field.getModule(),
					"modules_" + authManager.getUserDetails().getCompanyId());
			if (optionalRelatedModule.isEmpty()) {
				String[] vars = {};
				throw new BadRequestException("INVALID_RELATED_MODULE", vars);
			}
			Optional<ModuleField> optionalRelatedField = optionalRelatedModule.get().getFields().stream()
					.filter(relatedField -> relatedField.getFieldId().equalsIgnoreCase(arrayOfFieldIds[1])).findFirst();
			if (optionalRelatedField.isEmpty()) {
				String[] vars = { optionalRelatedModule.get().getName() };
				throw new BadRequestException("FIELD_INVALID", vars);
			}
		}
	}

	private void validReportFilter(Report report, Module module) {
		List<ModuleField> fields = module.getFields();
		List<Filter> reportFilters = report.getFilters();
		for (Filter reportFilter : reportFilters) {
			String[] arrayOfFieldIds = reportFilter.getField().getFieldId().split("[.]");

			Optional<ModuleField> optionalField = fields.stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(arrayOfFieldIds[0])).findFirst();
			if (optionalField.isEmpty()) {
				String[] vars = { module.getName() };
				throw new BadRequestException("INVALID_FIELD_FILTER", vars);
			}
			validateRelatedOneToManyField(optionalField.get(), arrayOfFieldIds);
		}
	}

	private void validSortBy(Report report, Module module) {
		List<ModuleField> fields = module.getFields();
		if (report.getSortBy() != null) {
			String[] arrayOfFieldIds = report.getSortBy().getFieldId().split("[.]");

			Optional<ModuleField> optionalField = fields.stream()
					.filter(field -> field.getFieldId().equalsIgnoreCase(arrayOfFieldIds[0])).findFirst();
			if (optionalField.isEmpty()) {
				String[] vars = { module.getName() };
				throw new BadRequestException("INVAID_SORT_BY_FIELD", vars);
			}
			validateRelatedOneToManyField(optionalField.get(), arrayOfFieldIds);

		}
	}

	private void validEmails(Report report) {
		if (report.getSchedules() != null) {
			ReportSchedule schedules = report.getSchedules();
			List<String> emails = schedules.getEmails();
			for (String email : emails) {
				if (!EmailValidator.getInstance().isValid(email)) {
					throw new BadRequestException("EMAIL_INVALID", null);
				}
			}
		}

	}

}
