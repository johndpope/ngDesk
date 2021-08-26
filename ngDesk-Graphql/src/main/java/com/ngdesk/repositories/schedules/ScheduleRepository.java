package com.ngdesk.repositories.schedules;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.schedules.dao.Schedule;
import com.ngdesk.repositories.CustomNgdeskRepository;


@Repository
public interface ScheduleRepository extends CustomScheduleRepository, CustomNgdeskRepository<Schedule, String> {
	
}
