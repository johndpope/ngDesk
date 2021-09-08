package com.ngdesk.data.csvimport.dao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.csvimport.CsvImportRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class CsvImportApi {

	@Autowired
	AuthManager authManager;

	@Autowired
	CsvImportRepository csvImportRepository;

	@Autowired
	RolesService roleService;

	@Autowired
	Validator validator;

	@Autowired
	ModulesRepository modulesRepository;

	@PostMapping(value = "/modules/{module_id}/csv")
	@Operation(summary = "Post imported csv data", description = "Post imported csv data")
	public CsvImport importFromCsv(
			@Parameter(description = "Company ID", required = false) @RequestParam(value = "company_id", required = false) String companyId,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@RequestBody CsvImportData csvImportData) {
		String role = authManager.getUserDetails().getRole();
		companyId = authManager.getUserDetails().getCompanyId();
		String createdById = authManager.getUserDetails().getUserId();

		if (role != null) {
			if (!roleService.isSystemAdmin(role)) {
				if (!roleService.isAuthorizedForRecord(role, "POST", moduleId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
			}
		}

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		List<String> logs = new ArrayList<String>();
		CsvImport entry = new CsvImport(null, "QUEUED", csvImportData, moduleId, logs, companyId,
				csvImportData.getFileName(), new Date(), createdById);

		entry = csvImportRepository.save(entry, "csv_import");
		return entry;
	}

	@PostMapping(value = "/modules/{module_id}/csvheaders")
	@Operation(summary = "Post csvheaders", description = "Post csvheaders")
	public List<String> generateCsvHeaders(
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@RequestBody CsvImportData csvImportData) {
		InputStream is = null;
		BufferedReader br = null;
		try {
			String file = csvImportData.getFile();
			Base64.Decoder dec = Base64.getDecoder();
			byte[] decbytes = dec.decode(file);
			is = new ByteArrayInputStream(decbytes);
			List<String> headers = new ArrayList<String>();

			if (csvImportData.getFileType().equals("xlsx")) {
				// DECODING IF XLSX
				Workbook workbook = new XSSFWorkbook(is);
				Sheet datatypeSheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = datatypeSheet.iterator();
				while (iterator.hasNext()) {
					Row currentRow = iterator.next();
					Iterator<Cell> cellIterator = currentRow.iterator();
					while (cellIterator.hasNext()) {
						Cell currentCell = cellIterator.next();
						headers.add(currentCell.toString());
					}
					workbook.close();
					break;
				}
			} else if (csvImportData.getFileType().equals("xls")) {
				Workbook workbook = new HSSFWorkbook(is);
				Sheet datatypeSheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = datatypeSheet.iterator();
				while (iterator.hasNext()) {
					Row currentRow = iterator.next();
					Iterator<Cell> cellIterator = currentRow.iterator();
					while (cellIterator.hasNext()) {
						Cell currentCell = cellIterator.next();
						headers.add(currentCell.toString());
					}
					workbook.close();
					break;
				}
			} else if (csvImportData.getFileType().equals("csv")) {
				// DECODING THE BYTE STRING SENT FROM FRONT-END
				br = new BufferedReader(new InputStreamReader(is));
				String line = "";
				int i = 0;

				while ((line = br.readLine()) != null) {
					if (i == 0) {
						// SPLITTING THE FIRST LINE OF THE FILE TO GET HEADERS
						headers = Arrays.asList(line.split(","));
						i++;
					}
				}
			}

			return headers;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
}
