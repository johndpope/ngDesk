package com.ngdesk.module.slas.dao;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.field.dao.DataType;
import com.ngdesk.module.field.dao.FieldAPI;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.repositories.SLARepository;

@Component
public class SLAService {

	@Autowired
	AuthManager authManager;

	@Autowired
	FieldAPI fieldAPI;

	@Autowired
	SLARepository slaRepository;

	public SLA setDefaultFields(SLA sla, SLA existingSla, String moduleId) {

		String companyId = authManager.getUserDetails().getCompanyId();
		sla.setCompanyId(companyId);
		sla.setModuleId(moduleId);
		if (existingSla == null) {
			sla.setDateCreated(new Date());
			sla.setDateUpdated(new Date());
			sla.setCreatedBy(authManager.getUserDetails().getUserId());
			sla.setLastUpdatedBy(authManager.getUserDetails().getUserId());
			sla.setDeleted(false);
		} else {
			sla.setDateUpdated(new Date());
			sla.setLastUpdatedBy(authManager.getUserDetails().getUserId());
			sla.setDateCreated(existingSla.getDateCreated());
			sla.setCreatedBy(existingSla.getCreatedBy());
			sla.setDeleted(existingSla.getDeleted());
		}
		return sla;
	}

	public void postSlaField(SLA sla, String moduleId) {

		String slaName = sla.getName();
		slaName = slaName.trim();
		String slaFieldName = slaName.toUpperCase();
		ModuleField slaModuleField = new ModuleField();
		slaModuleField.setName(slaFieldName);
		DataType dataType = new DataType();
		dataType.setBackend("Date");
		dataType.setDisplay("Date/Time");
		slaModuleField.setDataType(dataType);
		slaModuleField.setRequired(false);
		slaModuleField.setDisplayLabel(sla.getName());
		slaModuleField.setVisibility(true);
		slaModuleField.setInternal(true);
		slaModuleField.setNotEditable(true);
		fieldAPI.postField(slaModuleField, moduleId);

	}

	public void isSlaNameChanged(SLA sla) {

		Optional<SLA> optionalExistingSla = slaRepository.findSlaBySlaId(sla.getSlaId(), sla.getCompanyId(),
				sla.getModuleId());
		SLA existingSla = optionalExistingSla.get();
		if (!existingSla.getName().equalsIgnoreCase(sla.getName())) {
			throw new BadRequestException("SLA_NAME_CANNOT_UPDATE", null);
		}

	}
}
