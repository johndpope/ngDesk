package com.ngdesk.repositories.sla;

import java.util.Optional;

import com.ngdesk.data.sla.dao.SLAInstance;



public interface CustomSLAInstanceRepository {

	public Optional<SLAInstance> findBySlaIdAndDataId(String slaId, String dataId, String moduleId, String companyId);

}
