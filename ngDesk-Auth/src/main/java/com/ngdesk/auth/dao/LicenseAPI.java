package com.ngdesk.auth.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.Global;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.repositories.ModuleEntryRepository;

@RestController
public class LicenseAPI {

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	SendMail sendMail;

	@Autowired
	Global global;

	@Autowired
	LicenseAPIService licenseAPIService;

	public LicenseKey getLicense() {
		String machineId = licenseAPIService.getMachineId();
		String macAddress = licenseAPIService.getMacAddress();
		String licenseKey = machineId.concat(macAddress);
		String hashLicensekey = licenseAPIService.hashString(licenseKey);
		return new LicenseKey(hashLicensekey);
	}

}
