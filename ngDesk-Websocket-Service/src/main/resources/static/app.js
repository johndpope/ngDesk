
var ws;
function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
	if (connected) {
		$("#conversation").show();
	}
	else {
		$("#conversation").hide();
	}
	$("#greetings").html("");
}

function connect() {
	ws = new WebSocket('ws://localhost:8088/ngdesk-websocket?authentication_token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuYWdhcmFteWF0aGEua290aHVydUBhbGxibHVlc29sdXRpb25zLmNvbSIsImlhdCI6MTYwMzI0ODg1NywiZXhwIjoxNjA1ODQwODU3LCJVU0VSIjoie1wiTEFOR1VBR0VcIjogXCJlblwiLCBcIk5PVElGSUNBVElPTl9TT1VORFwiOiBcImFsYXJtX2NsYXNzaWNcIiwgXCJERUxFVEVEXCI6IGZhbHNlLCBcIkRJU0FCTEVEXCI6IGZhbHNlLCBcIlJPTEVcIjogXCI1ZjhlODA3NzAxZDc1ZTBmOGI3YzgwNWFcIiwgXCJFTUFJTF9WRVJJRklFRFwiOiBmYWxzZSwgXCJFTUFJTF9BRERSRVNTXCI6IFwibmFnYXJhbXlhdGhhLmtvdGh1cnVAYWxsYmx1ZXNvbHV0aW9ucy5jb21cIiwgXCJERUZBVUxUX0NPTlRBQ1RfTUVUSE9EXCI6IFwiRW1haWxcIiwgXCJURUFNU1wiOiBbXCI1ZjhlODA3ODAxZDc1ZTBmOGI3YzgwNjJcIiwgXCI1ZjhlODA3ODAxZDc1ZTBmOGI3YzgwNjNcIiwgXCI1ZjhlODA3YTAxZDc1ZTBmOGI3YzgwNmJcIl0sIFwiVVNFUl9VVUlEXCI6IFwiOGYzY2U2NTUtOTVmOC00ZWM4LWI1NWItMGUwNWI2MGVjNDQ5XCIsIFwiTE9HSU5fQVRURU1QVFNcIjogMCwgXCJJTlZJVEVfQUNDRVBURURcIjogdHJ1ZSwgXCJDT05UQUNUXCI6IFwiNWY4ZTgwN2EwMWQ3NWUwZjhiN2M4MDZlXCIsIFwiTEFTVF9TRUVOXCI6IHtcIiRkYXRlXCI6IDE2MDMxOTMxNDAxNTl9LCBcIkRBVEFfSURcIjogXCI1ZjhlODA3YjAxZDc1ZTBmOGI3YzgwNzFcIiwgXCJGSVJTVF9OQU1FXCI6IFwibmFnYVwiLCBcIkxBU1RfTkFNRVwiOiBcInJhbXlhdGhhXCJ9IiwiQ09NUEFOWV9VVUlEIjoiNDA4NjMwNjktMWE0MC00MWE3LTgyYmEtNmQ0YTZjMjg1ZWI5IiwiU1VCRE9NQUlOIjoiZGV2MSIsIkNPTVBBTllfSUQiOiI1ZjhlODA3NjAxZDc1ZTBmOGI3YzgwNTgifQ.aLNMHztSjvEaqqpPRwIFemaLZODCW911MrRIMNbEgdxVwBJFUaBUiYYWn9jt-6qC5quT6TF9RkwhEPN82txDfg');
	ws.onmessage = function(data) {
		showGreeting(data.data);
	}
	console.log("Connected")
	setConnected(true);
}

function disconnect() {
	if (ws != null) {
		ws.close();
	}
	setConnected(false);
	console.log("Disconnected");
}

function sendName() {
console.log("sendName");
	var payload = {
	"CONTROLLER_ID":"",
	"RULE_ID":""
		
	}
	var data = JSON.stringify(payload)
	ws.send(data);
}

function showGreeting(message) {
	var payload = JSON.parse(message);
	$("#greetings").append("<tr><td> " + payload.MESSAGE + "</td></tr>");
}

$(function() {
	$("form").on('submit', function(e) {
		e.preventDefault();
	});
	$("#connect").click(function() { connect(); });
	$("#disconnect").click(function() { disconnect(); });
	$("#send").click(function() { sendName(); });
});

