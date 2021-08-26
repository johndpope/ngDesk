package com.ngdesk.repositories.sla;

import org.springframework.stereotype.Repository;
import com.ngdesk.graphql.slas.dao.SLA;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface SLARepository extends CustomSLARepository, CustomNgdeskRepository<SLA, String> {

}