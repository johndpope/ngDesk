package com.ngdesk.repositories.sla;

import com.ngdesk.data.sla.dao.SLAInstance;
import com.ngdesk.repositories.CustomNgdeskRepository;

public interface SLAInstanceRepository
		extends CustomNgdeskRepository<SLAInstance, String>, CustomSLAInstanceRepository {

}
