package com.ngdesk.commons;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import brave.Span;
import brave.Tracer;



@Component
@Aspect
public class LoggingAspect {

	private Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
	
	@Autowired
	Tracer tracer;

	@Before("within(com.ngdesk..*) && !within(com.ngdesk.commons.exceptions..*) && !within(com.ngdesk.data.elastic.*) && !within(com.ngdesk.auth.*)")
	public void logMethodEnter(final JoinPoint joinPoint) throws Throwable {
		Span span = null;
		try {
			final StaticPart staticPart = joinPoint.getStaticPart();
//			final String sig = "" + staticPart.getSignature() + " with args: "
//					+ Arrays.deepToString(joinPoint.getArgs());
			final String sig = "" + staticPart.getSignature();
			logger.trace("Entering method " + sig);
			span = this.tracer.nextSpan().name(sig);
			span.start();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	@After("within(com.ngdesk..*) && !within(com.ngdesk.commons.exceptions..*) && !within(com.ngdesk.data.elastic.*) && !within(com.ngdesk.auth.*)")
	public void logMethodExit(final JoinPoint joinPoint) throws Throwable {
		Span span = null;
		try {
			final StaticPart staticPart = joinPoint.getStaticPart();
//			final String sig = "" + staticPart.getSignature() + " with args: "
//					+ Arrays.deepToString(joinPoint.getArgs());
			final String sig = "" + staticPart.getSignature();
			logger.trace("Leaving method " + sig);
			span = this.tracer.nextSpan().name(sig);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			span.finish();
		}
	}
}