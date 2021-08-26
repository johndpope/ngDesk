package com.ngdesk.data.dao;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class Prometheus {

	MeterRegistry meterRegistry;

	AtomicInteger elasticRecordsForUpdate = new AtomicInteger(0);

	public void Prometheus(MeterRegistry meterRegistry) {
		try {
			this.meterRegistry = meterRegistry;
			this.meterRegistry.gauge("elastic_records_in_queue", elasticRecordsForUpdate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void increment() {
		elasticRecordsForUpdate.getAndIncrement();
	}

	public void decrement() {
		elasticRecordsForUpdate.decrementAndGet();
	}

}
