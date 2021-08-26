package com.ngdesk.websocket.modules.dao;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngdesk.data.dao.SingleWorkflowPayload;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.websocket.companies.dao.Company;

@Service
public class ButtonTypeService {

	private final Logger log = LoggerFactory.getLogger(ButtonTypeService.class);

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	CompaniesRepository companyRepository;

	public void executeWorkflow(SingleWorkflowPayload payload, String subdomain, String userId) {

		Optional<Company> optionalCompany = companyRepository.findCompanyBySubdomain(subdomain);
		if (optionalCompany.isPresent()) {

			Company company = optionalCompany.get();
			payload.setCompanyId(company.getId());
			payload.setUserId(userId);
			payload.setDateCreated(new Date());
			
			rabbitTemplate.convertAndSend("execute-single-workflow", payload);

		}

	}

}
