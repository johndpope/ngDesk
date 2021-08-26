package com.ngdesk.company.uifaillogs.dao;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.company.dao.Company;
import com.ngdesk.repositories.CompanyRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RefreshScope
public class UIFailLogApi {

	@Autowired
	private CompanyRepository companyRepository;

	@Operation(summary = "Post UI fail log", description = "Post UI fail log if error status is 500")
	@PostMapping("company/uifail")
	public UIFailLog putAccountLevelAccess(@RequestBody UIFailLog uiFailLog) {
		try {
			String companySubdomain = (String) uiFailLog.getCompanySubdomain();
			Optional<Company> optionalCompany = companyRepository.findByCompanySubdomain(companySubdomain);
			Company company = optionalCompany.get();
			if (company != null) {
				uiFailLog.setDateCreated(new Date());
				return	companyRepository.saveUIFailLog(uiFailLog, "failed_ui_apis");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
