package com.ngdesk.resources;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class JavascriptFunctions {
	public String javascriptFunctions;
	private static final Logger logger = LogManager.getLogger(JavascriptFunctions.class);

	public JavascriptFunctions() {
		javascriptFunctions = "var weekdays = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];"
				+ "var nextNode;" + "function isInBetween(startDay,endDay){"
				+ "		var startIndex = weekdays.indexOf(startDay);"
				+ "		var endIndex = weekdays.indexOf(endDay);" + "		var currIndex = new Date().getDay();"
				+ "		if(startIndex > endIndex){" + "			endIndex += 7;" + "			if(currIndex < startIndex){"
				+ "				currIndex += 7;" + "			}" + "		}"
				+ "		if(currIndex >= startIndex && currIndex <= endIndex){" + "			return true;"
				+ "		} else {" + "			return false;" + "		}" + "}" + "function getTime(stringTime){"
				+ "		var returnTime = '';" + "		if(stringTime){"
				+ "			returnTime = parseFloat(stringTime.split(':')[0]);"
				+ "			returnTime += parseFloat(stringTime.split(':')[1] / 60);" + "		} else {"
				+ "			returnTime = new Date().getHours();"
				+ "			returnTime += (new Date().getMinutes() / 60);" + "		}" + "		return returnTime;"
				+ "}" + "function isInBetweenTime(startTime,endTime){"
				+ "		var st = parseFloat(startTime.split(':')[0]);"
				+ "		st += parseFloat(startTime.split(':')[1] / 60);"
				+ "		var end = parseFloat(endTime.split(':')[0]);"
				+ "		end += parseFloat(endTime.split(':')[1] / 60);" + "		var currDate = new Date();"
				+ "		var curr = currDate.getHours();" + "		curr += currDate.getMinutes()/60;"
				+ "		if(curr >= st && curr <= end){" + "			return true;" + "		} else {"
				+ "			return false;" + "		}" + "}	" + "function isWeekDay(startTime,endTime,type){"
				+ "		var st = parseFloat(startTime.split(':')[0])+parseFloat(startTime.split(':')[1]/60);"
				+ "		var end = parseFloat(endTime.split(':')[0])+parseFloat(endTime.split(':')[1]/60);"
				+ "		var currTime = new Date().getHours() + (new Date().getMinutes()/60);"
				+ "		if(type=='weekdays'){"
				+ "			if(weekdays.indexOf(new Date().getDay()) == 0 || weekdays.indexOf(new Date().getDay()) == 6) {"
				+ "				return false;" + "			}" + "		} else {"
				+ "			if(currTime >= st && currTime <= end) {" + "				return true;"
				+ "			} else {" + "				return false;" + "			}" + "		}" + "}"
				+ "function checkRegex(regex,value){" + "		if(value.match(regex)){" + "			return true;"
				+ "		} else {" + "			return false;" + "		}" + "}";
	}

	public String getJavascriptFunctions() {
		return javascriptFunctions;
	}

	public void setJavascriptFunctions(String javascriptFunctions) {
		this.javascriptFunctions = javascriptFunctions;
	}

}
