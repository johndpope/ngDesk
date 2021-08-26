package com.ngdesk.repositories;

import com.ngdesk.company.dao.Tracker;

public interface CustomTrackerRepository {

	public Tracker getFirstTracker(String collectionName);
}
