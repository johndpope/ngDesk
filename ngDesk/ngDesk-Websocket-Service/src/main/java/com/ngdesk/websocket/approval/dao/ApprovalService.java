package com.ngdesk.websocket.approval.dao;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.models.User;
import com.ngdesk.data.dao.WorkflowPayload;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.WebSocketService;
import com.ngdesk.websocket.modules.dao.Module;

@Component
public class ApprovalService {

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository moduleRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	WebSocketService webSocketService;

	public void getApprovalDetailsAndExecute(Approval approval, User user) {
		Optional<Company> optionalCompany = companiesRepository.findById(user.getCompanyId(), "companies");
		if (!optionalCompany.isPresent()) {
			return;
		}

		Optional<Module> optionalModule = moduleRepository.findById(approval.getModuleId(),
				"modules_" + user.getCompanyId());

		if (optionalModule.isEmpty()) {
			return;
		}

		Module module = optionalModule.get();
		Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryById(approval.getDataId(),
				getCollectionName(module) + "_" + user.getCompanyId());
		if (optionalEntry.isEmpty()) {
			return;
		}


		Map<String, Object> entry = optionalEntry.get();

		Map<String, Object> approvalData = (Map<String, Object>) entry.get("APPROVAL");
		if (approval.getApproved() != null) {
			if (approval.getApproved()) {
				approvalData.put("APPROVED_BY", user.getUserId());
			} else {
				approvalData.put("DENIED_BY", new DeniedBy(user.getUserId(), approval.getComments()));
			}
			
			entry.put("APPROVAL", approvalData);

			WorkflowPayload workflowPayload = new WorkflowPayload(user.getUserId(), module.getModuleId(),
					user.getCompanyId(), entry.get("_id").toString(), entry, "PUT", new Date());
			webSocketService.addToQueue(workflowPayload);
		}

	}

	private String getCollectionName(Module module) {
		return module.getName().replaceAll("\\s+", "_");
	}

}
