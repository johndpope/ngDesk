package com.ngdesk.workflow.executor.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ngdesk.workflow.module.dao.Module;
import com.ngdesk.workflow.module.dao.ModuleField;
import com.ngdesk.workflow.dao.RouteCondition;

@Component
public class RouteNodeService {

	public String generateStatement(String operator, String variable, String dataType, String value, String value2) {

		String statement = "false";
		if (operator.equalsIgnoreCase("REGEX")) {
			return "inputMessage." + variable + ".match(/" + value + "/g) != null";

		} else if (operator.equalsIgnoreCase("EQUALS_TO")) {

			if (variable.equalsIgnoreCase("DAY_OF_WEEK")) {
				return "weekdays.indexOf('" + value + "') == new Date().getDay()";
			}

			if (dataType.equalsIgnoreCase("STRING")) {

				if (value == null || value.equals("null")) {
					return "inputMessage." + variable + " == " + value;
				} else {
					return "inputMessage." + variable + " == " + "'" + value + "'";
				}

			} else if (dataType.equalsIgnoreCase("INTEGER")) {
				return "inputMessage." + variable + " == " + value;
			}
		} else if (operator.equalsIgnoreCase("NOT_EQUALS_TO")) {
			if (variable.equalsIgnoreCase("DAY_OF_WEEK")) {
				return "weekdays.indexOf('" + value + "') != new Date().getDay()";
			}
			if (dataType.equalsIgnoreCase("STRING")) {
				if (value == null || value.equals("null")) {
					return "inputMessage." + variable + " != " + value;
				} else {
					return "inputMessage." + variable + " != " + "'" + value + "'";
				}
			} else if (dataType.equalsIgnoreCase("INTEGER")) {
				return "inputMessage." + variable + " != " + value;
			}
		} else if (operator.equalsIgnoreCase("CONTAINS")) {
			return "inputMessage." + variable + ".indexOf('" + value + "') != -1";
		} else if (operator.equalsIgnoreCase("DOES_NOT_CONTAIN")) {
			return "inputMessage." + variable + ".indexOf('" + value + "') == -1";
		} else if (operator.equalsIgnoreCase("LESS_THAN")) {
			if (variable.equalsIgnoreCase("TIME_OF_DAY")) {
				return "getTime('" + value + "') < getTime()";
			} else {
				return "inputMessage." + variable + " < " + value;
			}
		} else if (operator.equalsIgnoreCase("GREATER_THAN")) {
			if (variable.equalsIgnoreCase("TIME_OF_DAY")) {
				return "getTime('" + value + "') > getTime()";
			} else {
				return "inputMessage." + variable + " > " + value;
			}
		} else if (operator.equalsIgnoreCase("EXISTS")) {
			return "inputMessage.hasOwnProperty('" + variable + "')";

		} else if (operator.equalsIgnoreCase("BETWEEN")) {
			if (variable.equalsIgnoreCase("DAY_OF_WEEK")) {
				return "isInBetween('" + value + "','" + value2 + "')";
			} else if (variable.equalsIgnoreCase("TIME_OF_DAY")) {
				return "isInBetweenTime('" + value + "','" + value2 + "')";
			}
		}
		return statement;

	}

	public String JavascriptFunctions() {
		String javascriptFunctions = "var weekdays = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];"
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

		return javascriptFunctions;
	}

	public String getVariableName(Module module, String field) {

		for (ModuleField moduleField : module.getFields()) {
			if (moduleField.getFieldId().equalsIgnoreCase(field)) {
				field = moduleField.getName();
			}
		}

		return field;
	}

	public String generateJavascriptCondition(List<RouteCondition> conditions, String variable, String dataType,
			String javaScriptCode) {
		for (int i = 0; i < conditions.size(); i++) {

			String fieldName = variable;
			String op = null;

			// GETTING SPECIFIC CONDITION
			RouteCondition condition = conditions.get(i);
			String statement = null;
			String value = condition.getValue();

			// IF NOT LAST CONDITION
			if (condition.getOperator() != null) {
				op = condition.getOperator();
			}
			if (op.equalsIgnoreCase("between")) {
				String[] val = value.split(",");
				String value1 = val[1];
				String value2 = val[2];
				statement = generateStatement(op, fieldName, dataType, value1, value2);
			} else {
				statement = generateStatement(op, fieldName, dataType, value, null);
			}

			if (conditions.size() == 1) {
				javaScriptCode += "nextNode = '" + condition.getToNode() + "';";
			} else {
				if (i == 0) {
					javaScriptCode += "if(" + statement + "){";
					javaScriptCode += "nextNode = '" + condition.getToNode() + "';";
					javaScriptCode += "}";
				} else {
					if (i == conditions.size() - 1) {
						// ELSE PART
						javaScriptCode += "else {";
						javaScriptCode += "nextNode = '" + condition.getToNode() + "';";
						javaScriptCode += "}";
					} else {
						// ELSE IF PART
						javaScriptCode += "else if(" + statement + "){";
						javaScriptCode += "nextNode ='" + condition.getToNode() + "';";
						javaScriptCode += "}";
					}
				}
			}
			javaScriptCode += System.lineSeparator();
		}
		return javaScriptCode;
	}
}
