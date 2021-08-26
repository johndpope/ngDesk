package com.ngdesk.repositories.sla;

import java.util.List;

import com.ngdesk.data.sla.dao.SLA;

public interface CustomSLARepository {

	public List<SLA> findAllSlaByModuleId(String moduleId, String companyId);
}
