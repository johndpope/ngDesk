package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.module.slas.dao.SLAInstance;

public interface CustomSLAInstanceRepository {

	public Optional<SLAInstance> findBySlaInstanceId(String slaId, String dataId);

	public void findEntryAndUpdate(String slaId, String dataId, String fieldName, Object date);

	public void deleteBySlaId(String slaId, String dataId, String moduleId, String companyId);
}
