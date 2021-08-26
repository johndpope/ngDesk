package com.ngdesk.sam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.InternalErrorException;

@RestController
@Component
public class DownloadInstallerService {
	
	@Value("${sam.installer.path}")
	String buildInstallerPath;
	
	@GetMapping("/sam/installer/download")
	public ResponseEntity<Resource> getInstaller(HttpServletRequest request,
			@RequestParam("platform") String platform) {
		try {
			
			if (!Global.validInstallerPlatforms.contains(platform)) {
				throw new BadRequestException("INSTALLER_PLATFORM_NOT_SUPPORTED");
			}
			
			String subdomain = request.getAttribute("SUBDOMAIN").toString();
			
			String fileType = null;
			
			if (platform.equals("windows")) {
				fileType = ".exe";
			} else if (platform.equals("windows-x64")) {
				fileType = ".exe";
			} else if (platform.equals("linux")) {
				fileType = ".run";
			} else if (platform.equals("linux-x64")) {
				fileType = ".run";
			} else if (platform.equals("osx")) {
				fileType = ".app.zip";
			}
			File file = new File(buildInstallerPath + System.getProperty("file.separator") + "ngDesk_Controller_"
					+ subdomain + fileType);
	        InputStreamResource targetStream = new InputStreamResource(new FileInputStream(file));
			
	        HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=" + "ngDesk_Controller_"
							+ subdomain + fileType);
			
			return ResponseEntity.ok().headers(headers)
					.contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(targetStream);
	        
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}
	
}
