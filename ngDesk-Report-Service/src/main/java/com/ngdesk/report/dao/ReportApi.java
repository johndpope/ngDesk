package com.ngdesk.report.dao;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ReportRepository;

import io.swagger.v3.oas.annotations.Parameter;

@Component
@RestController
public class ReportApi {

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private AuthManager authManager;

	@DeleteMapping("/reports")
	public void deleteReport(
			@Parameter(description = "Report ID", required = true) @RequestParam("report_id") String id) {
		String companyId = authManager.getUserDetails().getCompanyId();
		String collectionName = "reports_" + companyId;
		Optional<Report> optionalReport = reportRepository.findById(id, collectionName);

		if (optionalReport.isEmpty()) {
			throw new NotFoundException("REPORT_NOT_FOUND", null);
		}
		reportRepository.deleteById(id, "reports_" + authManager.getUserDetails().getCompanyId());
	}

	@PostMapping("/reports")
	public Report postReport(@Valid @RequestBody Report report) {
		String companyId = authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();
		String collectionName = "reports_" + companyId;

		report.setDateCreated(new Date());
		report.setDateUpdated(new Date());
		report.setCreatedBy(userId);
		report.setLastUpdated(userId);
		report.setReportId(new ObjectId().toString());
		return reportRepository.save(report, collectionName);
	}

	@PutMapping("/reports")
	public Report putReport(@Valid @RequestBody Report report) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String userId = authManager.getUserDetails().getUserId();
		String collectionName = "reports_" + companyId;
		Optional<Report> optionalReport = reportRepository.findById(report.getReportId(), collectionName);

		if (optionalReport.isEmpty()) {
			throw new NotFoundException("DAO_NOT_FOUND", null);
		}

		Report existingReport = optionalReport.get();
		report.setDateCreated(existingReport.getDateCreated());
		report.setCreatedBy(existingReport.getCreatedBy());
		report.setDateUpdated(new Date());
		report.setLastUpdated(userId);

		return reportRepository.save(report, collectionName);

	}

	@GetMapping("/reports/download")
	public ResponseEntity<Resource> getCsvFile(@RequestParam("reportName") String reportName) {
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportName + ".csv");
			String content = new String(Files.readAllBytes(Paths.get("/opt/ngdesk/reports/" + reportName + ".csv")));

			InputStream targetStream = IOUtils.toInputStream(content, "UTF-8");
			return ResponseEntity.ok().headers(headers)
					.contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(new InputStreamResource(targetStream));

		} catch (Exception e) {
			e.printStackTrace();
		}

		throw new InternalErrorException("INTERNAL_ERROR");
	}

}