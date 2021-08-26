package com.ngdesk.sam.controllers.dao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerVersion {

	@Autowired
	ControllerVersion controllerVersion;

	public Version version;

	@PostConstruct
	public void init() {

		controllerVersion.version = new Version();
		controllerVersion.version.setVersion(1);

		List<SubAppVersion> subAppVersions = new ArrayList<SubAppVersion>();
		SubAppVersion softwareProbeVersion = new SubAppVersion("ngDesk-Software-Probe", 1);
		SubAppVersion assetProbeVersion = new SubAppVersion("ngDesk-Asset-Probe", 1);
		SubAppVersion patchProbeVersion = new SubAppVersion("ngDesk-Patch-Probe", 1);

		subAppVersions.add(assetProbeVersion);
		subAppVersions.add(softwareProbeVersion);
		subAppVersions.add(patchProbeVersion);

		controllerVersion.version.setSubAppVersions(subAppVersions);
	}
}
