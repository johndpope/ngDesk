package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.auth.dao.LicenseInformation;

public interface CustomLicenseKeyRepository {

	public Optional<LicenseInformation> findLicenseKeyBySubdomain(String subdomain, String collectionName);

	public Optional<LicenseInformation> findUserCount(String collectionName, LicenseInformation licenseInformation);

}