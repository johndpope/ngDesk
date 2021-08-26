package com.ngdesk.gateway;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class Prometheus {
	
	MeterRegistry meterRegistry;

	public Prometheus(MeterRegistry meterRegistry) {
		try {
			this.meterRegistry = meterRegistry;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void increment(String serviceName) {
		this.meterRegistry.counter("service_not_found", "service-name", serviceName).increment();
	}
}
