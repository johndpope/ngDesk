package com.ngdesk.channels.whatsapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.Authentication;
import com.ngdesk.roles.RoleService;

@RestController
@Component
public class WhatsappChannelService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@Autowired
	RoleService roleService;

}
