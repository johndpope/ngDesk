package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.schedule.dao.Schedule;

public interface CustomScheduleRepository {
	
	public Optional<Schedule> findScheduleByName(String name, String collection);
}
