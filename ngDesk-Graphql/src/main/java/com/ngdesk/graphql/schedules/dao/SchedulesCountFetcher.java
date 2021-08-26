package com.ngdesk.graphql.schedules.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.schedules.ScheduleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SchedulesCountFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ScheduleRepository scheduleRepository; 

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		return scheduleRepository.getCount("schedules_" + authManager.getUserDetails().getCompanyId());
	}

}
