
package com.ngdesk.notifications;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthProxy;
import com.ngdesk.notifications.dao.Notification;
import com.ngdesk.notifications.dao.NotificationAPI;

import brave.Tracer;

@ExtendWith(SpringExtension.class)

@WebMvcTest(NotificationAPI.class)

@ContextConfiguration(classes = { ApplicationTest.class })
class NotificationsAPITest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private NotificationAPI notificationAPI;

	@MockBean
	AuthProxy authProxy;

	@MockBean
	Tracer tracer;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testNotificationPostSuccess() throws Exception {

		Notification notification = new Notification("2", "12", null, "2", null, null, null, "success");

		// PREPARE STUB
		given(notificationAPI.addNotification(any(Notification.class))).willReturn(notification);

		// PERFORM MOCK TEST
		String notificationString = mockMvc
				.perform(post("/notification").content(mapper.writeValueAsString(notification))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		Notification response = mapper.readValue(notificationString, Notification.class);
	}

	@Test
	public void testNotificationPostMessageNotEmpty() throws Exception {

		Notification notification = new Notification(null, null, null, "5", null, null, null, null);

		// PREPARE STUB
		given(notificationAPI.addNotification(any(Notification.class))).willReturn(notification);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/notification").content(mapper.writeValueAsString(notification)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void testNotificationPostRecipientIdNotEmpty() throws Exception {

		Notification notification = new Notification(null, null, null, null, null, null, null, "test");

		// PREPARE STUB
		given(notificationAPI.addNotification(any(Notification.class))).willReturn(notification);

		// PERFORM MOCK TEST
		mockMvc.perform(
				post("/notification").content(mapper.writeValueAsString(notification)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void testputNotificationSuccess() throws Exception {
		Notification notification = new Notification("2", "12", null, "2", null, null, null, "test");

		given(notificationAPI.updateNotification(any(Notification.class))).willReturn(notification);
		String notificationString = mockMvc
				.perform(put("/notification").content(mapper.writeValueAsString(notification))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	}

	@Test
	public void putNotificationForMessageNotEmpty() throws Exception {
		Notification notification = new Notification(null, "12", null, null, null, null, null, null);

		given(notificationAPI.updateNotification(any(Notification.class))).willReturn(notification);
		mockMvc.perform(
				put("/notification").content(mapper.writeValueAsString(notification)).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}
