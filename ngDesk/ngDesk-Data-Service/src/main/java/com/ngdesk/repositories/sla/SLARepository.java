package com.ngdesk.repositories.sla;

import com.ngdesk.data.sla.dao.SLA;
import com.ngdesk.repositories.CustomNgdeskRepository;

public interface SLARepository extends CustomNgdeskRepository<SLA, String>, CustomSLARepository {

}
