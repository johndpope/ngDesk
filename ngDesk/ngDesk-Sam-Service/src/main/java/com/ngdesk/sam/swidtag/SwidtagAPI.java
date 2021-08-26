package com.ngdesk.sam.swidtag;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.websocket.SendResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.SwidtagRepository;

@RestController
@RefreshScope
public class SwidtagAPI {

	private final Logger log = LoggerFactory.getLogger(SwidtagAPI.class);

	@Autowired
	SwidtagRepository swidtagRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	SwidtagService swidtagService;

	@Autowired 
	DataProxy dataProxy;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@PostMapping("/swidtags")
	public Swidtags postSwidtags(@Valid @RequestBody Swidtags swidtags) {
		try {
			SwidtagPayload swidtagPayload = new SwidtagPayload();

			for (Swidtag swidtag : swidtags.getSwidtags()) {
				if (!swidtag.getFileName().endsWith(".swidtag")) {
					continue;
				}
				Optional<Map<String, Object>> optionalAsset = swidtagService.validateAsset(swidtag.getAssetId(),
						"Assets_" + authManager.getUserDetails().getCompanyId());
				if (optionalAsset.isEmpty()) {
					continue;
				}
				Optional<Swidtag> optionalSwidtag = swidtagRepository.findExistingSwidtag(swidtag.getFileName(),
						swidtag.getCompanyId(), swidtag.getAssetId(), "swidtag_files");
				if (optionalSwidtag.isPresent()) {
					Swidtag existingSwidtag = optionalSwidtag.get();
					existingSwidtag.setFileContent(swidtag.getFileContent());
					existingSwidtag.setDateUpdated(new Date());
					existingSwidtag.setLastUpdatedBy(authManager.getUserDetails().getUserId());
					swidtagPayload.setUser(authManager.getUserDetails());
					swidtagPayload.setSwidtag(existingSwidtag);
				} else {
					swidtag.setDateCreated(new Date());
					swidtag.setCompanyId(authManager.getUserDetails().getCompanyId());
					swidtag.setLastUpdatedBy(authManager.getUserDetails().getUserId());
					swidtag.setCreatedBy(authManager.getUserDetails().getUserId());
					swidtagPayload.setUser(authManager.getUserDetails());
					swidtagPayload.setSwidtag(swidtag);
				}
				
				rabbitTemplate.convertAndSend("post-swidtag-and-software", swidtagPayload);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return swidtags;
	}

}
