package com.ngdesk.auth.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.auth.company.dao.Role;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class LicenseAPIService {

	@Autowired
	SendMail sendMail;

	@Autowired
	Global global;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	LicenseAPIService licenseAPIService;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	RoleRepository roleRepository;

	public Integer getUsersCount(String companyId) {
		Optional<Role> optionalCustomers = roleRepository.findRoleByName("Customers", "roles_" + companyId);
		Role customersRole = optionalCustomers.get();
		return (int) moduleEntryRepository.getCountOfPayingUsers(customersRole.getId(), "Users_" + companyId);
	}

	public String getMachineId() {
		String data = "";
		try {
			File licenseFile = new File("/etc/machine-id");
			if (licenseFile.exists()) {
				Scanner readFile = new Scanner(licenseFile);
				while (readFile.hasNextLine()) {
					data = readFile.nextLine();
				}
				return data;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		throw new BadRequestException("MACHINE_ID_NOT_FOUND", null);
	}

	public String getMacAddress() {
		try {
			final NetworkInterface netInf = NetworkInterface.getNetworkInterfaces().nextElement();
			final byte[] mac = netInf.getHardwareAddress();
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			if (sb.toString() != null) {
				return sb.toString();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		throw new BadRequestException("MAC_ADDRESS_NOT_FOUND", null);
	}

	public String hashString(String name) {
		String hashedString = "";
		if (name.isBlank()) {
			return "";
		}
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(name.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			hashedString = bigInt.toString(16);
			return hashedString;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		throw new BadRequestException("HASH_STRING_INVALID", null);
	}

}
