package com.ngdesk;

import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONObject;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class CustomLayout extends LayoutBase<ILoggingEvent> {

	public String doLayout(ILoggingEvent event) {
		StringBuffer sbuf = new StringBuffer(128);

		JSONObject log = new JSONObject();
		log.put("@timestamp", new Timestamp(new Date().getTime()));
		log.put("@version", "1");
		log.put("level", event.getLevel());
		log.put("@class", event.getLoggerName());
		log.put("message", event.getFormattedMessage());
		for (String key : event.getMDCPropertyMap().keySet()) {
			log.put(key, event.getMDCPropertyMap().get(key));
		}
		sbuf.append(log.toString());
		sbuf.append(CoreConstants.LINE_SEPARATOR);
		return sbuf.toString();
	}
}