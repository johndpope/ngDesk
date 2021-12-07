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
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.data.modules.dao.ModuleField;
import com.ngdesk.data.roles.dao.RolesService;
import com.ngdesk.data.validator.Validator;
import com.ngdesk.repositories.csvimport.CsvImportRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.opencsv.CSVReader;

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
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@RequestBody CsvImport csvImport) {

		if (authManager.getUserDetails().getRole() != null) {
			if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
				if (!roleService.isAuthorizedForRecord(authManager.getUserDetails().getRole(), "POST", moduleId)) {
					throw new ForbiddenException("FORBIDDEN");
				}
			}
		}

		if (!validator.isValidObjectId(moduleId)) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Optional<Module> optionalModule = modulesRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		List<ModuleField> moduleFields = module.getFields();
		CsvImportData csvImportData = csvImport.getCsvImportData();

		List<String> defaultFields = List.of("DATE_CREATED", "DATE_UPDATED", "EFFECTIVE_TO", "EFFECTIVE_FROM",
				"CREATED_BY", "LAST_UPDATED_BY", "DELETED");
		List<CsvHeaders> csvHeaders = csvImportData.getHeaders();
		for (CsvHeaders csvHeader : csvHeaders) {
			String fieldId = csvHeader.getFieldId();
			ModuleField moduleField = moduleFields.stream().filter(field -> field.getFieldId().equals(fieldId))
					.findFirst().orElse(null);
			if (defaultFields.contains(moduleField.getName())) {
				String[] vars = new String[] { moduleField.getDisplayLabel()};
				throw new BadRequestException("INVALID_FIELD_SELECTED", vars);
			}
		}

		List<CsvImportLog> logs = new ArrayList<CsvImportLog>();
		CsvFormat csvFormat = csvImport.getCsvFormat();
		CsvImport entry = new CsvImport(null, "QUEUED", csvImportData, moduleId, logs,
				authManager.getUserDetails().getCompanyId(), csvImportData.getFileName(), csvFormat, 0, 0, new Date(),
				authManager.getUserDetails().getUserId());

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
		String file = csvImportData.getFile();
		Base64.Decoder dec = Base64.getDecoder();
		byte[] decbytes = dec.decode(file);
		is = new ByteArrayInputStream(decbytes);
		List<String> headers = new ArrayList<String>();
		String vars[] = { csvImportData.getFileName() };

		if (csvImportData.getFileType().equals("xlsx") || csvImportData.getFileType().equals("xls")) {
			Workbook workbook = null;
			Sheet datatypeSheet;
			try {
				if (csvImportData.getFileType().equals("xlsx")) {
					workbook = new XSSFWorkbook(is);
				} else {
					workbook = new HSSFWorkbook(is);
				}
				datatypeSheet = workbook.getSheetAt(0);
				workbook.close();
			} catch (IOException e) {
				throw new BadRequestException("FILE_CORRUPTED", vars);
			}
			Iterator<Row> iterator = datatypeSheet.iterator();
			while (iterator.hasNext()) {
				Row currentRow = iterator.next();
				Iterator<Cell> cellIterator = currentRow.iterator();
				while (cellIterator.hasNext()) {
					Cell currentCell = cellIterator.next();
					headers.add(currentCell.toString());
				}
				break;
			}
		} else if (csvImportData.getFileType().equals("csv")) {
			br = new BufferedReader(new InputStreamReader(is));
			CSVReader csvReader = new CSVReader(br);
			List<String[]> list = new ArrayList<>();
			try {
				list = csvReader.readAll();
				csvReader.close();
			} catch (IOException e) {
				throw new BadRequestException("FILE_CORRUPTED", vars);
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
			}
			String[] headersArray = Arrays.stream(list.get(0)).map(String::trim).toArray(String[]::new);
			headers = Arrays.asList(headersArray);
			boolean bool = headers.stream().anyMatch(header -> (header == null || header.equals("")));
			if (bool) {
				throw new BadRequestException("HEADER_CANNOT_BE_EMPTY", null);
			}
		} else {
			vars = new String[] { csvImportData.getFileType() };
			throw new BadRequestException("FILE_FORMAT_NOT_SUPPORTED", vars);
		}
		return headers;
	}
}
