package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.schedule.dao.Schedule;


@Repository
public interface ScheduleRepository extends CustomScheduleRepository, CustomNgdeskRepository<Schedule, String> {
	
}
