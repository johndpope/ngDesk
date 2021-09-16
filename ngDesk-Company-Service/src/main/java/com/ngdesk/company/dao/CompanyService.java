package com.ngdesk.company.dao;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.company.settings.dao.ChatSettings;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.GalleryRepository;
import com.ngdesk.repositories.LoginRepository;
import com.ngdesk.repositories.PluginRepository;
import com.ngdesk.repositories.TrackerRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

@Component
public class CompanyService {

	@Value("${email.host}")
	private String host;

	@Value("${email.port}")
	private int emailPort;

	@Autowired
	private LoginRepository loginRepository;

	@Autowired
	GalleryRepository galleryRepository;

	@Autowired
	TrackerRepository trackerRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	PluginRepository pluginRepository;

	@Autowired
	SendMail sendmail;

	@Value("${sendgrid.api.key}")
	private String sgKey;

	/*
	 * This function generates new object id, used in case of company
	 */
	public ObjectId getNewObjectId() {
		ObjectId objectId = null;
		while (objectId == null) {
			objectId = new ObjectId();
			Optional<Company> optionalCompany = companyRepository.findById(objectId.toString(), "companies");
			if (optionalCompany.isPresent()) {
				objectId = null;
			}
		}
		return objectId;
	}

	public Company setTracking(Company company, String utmSource, String utmTerm, String utmMedium, String utmContent,
			String utmCampaign) {
		Tracking tracking = new Tracking();
		tracking.setUtmSource(utmSource);
		tracking.setUtmTeam(utmTerm);
		tracking.setUtmMedium(utmMedium);
		tracking.setUtmContent(utmContent);
		tracking.setUtmCampaign(utmCampaign);
		company.setTracking(tracking);
		return company;
	}

	public InviteMessage getInviteMessage(Company company) {

		InviteMessage message = new InviteMessage();
		String subject = "Welcome to ngDesk for " + company.getCompanySubdomain();
		message.setSubject(subject);

		String message1 = "Hello first_name last_name,<br/><br/>You have been invited to join ngDesk for "
				+ company.getCompanySubdomain() + ".";
		message.setMessage1(message1);

		String message2 = "Regards,<br/>The ngDesk Support Team<br/><a href=\"mailto:support@ngdesk.com\">support@ngdesk.com</a>";
		message.setMessage2(message2);

		message.setFromAddress("support@" + company.getCompanySubdomain().toLowerCase() + ".ngdesk.com");

		return message;
	}

	public ForgotPasswordMessage getForgotPasswordMessage(Company company) {
		ForgotPasswordMessage message = new ForgotPasswordMessage();
		message.setMessage1("Hello first_name last_name,<br/>\n" + "<br/>");
		message.setMessage2("Regards,<br/>\n" + "The ngDesk Support Team<br/>\n"
				+ "<a href=\"mailto:support@ngdesk.com\">support@ngdesk.com</a>");

		message.setSubject("ngDesk Password Reset");
		message.setFromAddress("support@" + company.getCompanySubdomain().toLowerCase() + ".ngdesk.com");

		message.setFromAddress("support@" + company.getCompanySubdomain() + ".ngdesk.com");
		return message;
	}

	public SignUpMessage getSignupMessage(Company company) {
		SignUpMessage message = new SignUpMessage();
		message.setSubject("Welcome to ngDesk");
		message.setMessage("Welcome to ngDesk");
		message.setFromAddress("support@" + company.getCompanySubdomain() + ".ngdesk.com");
		return message;
	}

	public void setCustomLogin(Company company) {
		try {
			Optional<CustomLogin> optionalLogin = loginRepository.findLoginTemplate("custom_login_templates");
			if (optionalLogin.isEmpty()) {
				return;
			}
			CustomLogin login = optionalLogin.get();
			login.setCompanyId(company.getCompanyId());
			login.setHeader("Welcome to " + company.getCompanySubdomain());

			loginRepository.save(login, "custom_login_page");
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("CUSTOM_LOGIN_MESSAGE_POST_FAILED", null);
		}
	}

	public void setDefaultNgdeskLogo(Company company) {
		try {
			Gallery imgGallery = new Gallery();

			Image image = new Image();
			image.setFile(
					"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAR4AAAE4CAYAAACNPJz9AAAACXBIWXMAAC4jAAAuIwF4pT92AAALSGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDUgNzkuMTYzNDk5LCAyMDE4LzA4LzEzLTE2OjQwOjIyICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOnBob3Rvc2hvcD0iaHR0cDovL25zLmFkb2JlLmNvbS9waG90b3Nob3AvMS4wLyIgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIiB4bWxuczpleGlmPSJodHRwOi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxNyAoV2luZG93cykiIHhtcDpDcmVhdGVEYXRlPSIyMDE3LTA2LTA2VDE1OjU3OjE4LTA1OjAwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDE5LTA3LTIzVDExOjE0OjE4KzA1OjMwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAxOS0wNy0yM1QxMToxNDoxOCswNTozMCIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9InNSR0IgSUVDNjE5NjYtMi4xIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo5Yjc0NjhhNS05NjkxLWMzNGUtYmM4Yy01YzRiOTA5YmYwNTkiIHhtcE1NOkRvY3VtZW50SUQ9ImFkb2JlOmRvY2lkOnBob3Rvc2hvcDozNmUzN2UzZS1jNTgwLTVlNGYtYTg0YS1kMjdhOWY5MjAzZjkiIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDphZWYxZTA4Ni04NGVlLTA0NGYtOTA0ZC01ZTRjMDFjMTBlOGUiIHRpZmY6T3JpZW50YXRpb249IjEiIHRpZmY6WFJlc29sdXRpb249IjMwMDAwMDAvMTAwMDAiIHRpZmY6WVJlc29sdXRpb249IjMwMDAwMDAvMTAwMDAiIHRpZmY6UmVzb2x1dGlvblVuaXQ9IjIiIGV4aWY6Q29sb3JTcGFjZT0iMSIgZXhpZjpQaXhlbFhEaW1lbnNpb249IjEwODAiIGV4aWY6UGl4ZWxZRGltZW5zaW9uPSIzMjIiPiA8cGhvdG9zaG9wOkRvY3VtZW50QW5jZXN0b3JzPiA8cmRmOkJhZz4gPHJkZjpsaT5hZG9iZTpkb2NpZDpwaG90b3Nob3A6YmQ2YjllMzQtNGFmMC0xMWU3LWFjNDEtOTQ4ZmUxYTllNTIzPC9yZGY6bGk+IDwvcmRmOkJhZz4gPC9waG90b3Nob3A6RG9jdW1lbnRBbmNlc3RvcnM+IDx4bXBNTTpIaXN0b3J5PiA8cmRmOlNlcT4gPHJkZjpsaSBzdEV2dDphY3Rpb249ImNyZWF0ZWQiIHN0RXZ0Omluc3RhbmNlSUQ9InhtcC5paWQ6YWVmMWUwODYtODRlZS0wNDRmLTkwNGQtNWU0YzAxYzEwZThlIiBzdEV2dDp3aGVuPSIyMDE3LTA2LTA2VDE1OjU3OjE4LTA1OjAwIiBzdEV2dDpzb2Z0d2FyZUFnZW50PSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxNyAoV2luZG93cykiLz4gPHJkZjpsaSBzdEV2dDphY3Rpb249InNhdmVkIiBzdEV2dDppbnN0YW5jZUlEPSJ4bXAuaWlkOmU2YWE4YmFmLTdiMjAtNTI0Ni1iZjg5LWVlMjEwM2Y4MmJlNCIgc3RFdnQ6d2hlbj0iMjAxNy0wNi0wNlQxNjowMTo0MS0wNTowMCIgc3RFdnQ6c29mdHdhcmVBZ2VudD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTcgKFdpbmRvd3MpIiBzdEV2dDpjaGFuZ2VkPSIvIi8+IDxyZGY6bGkgc3RFdnQ6YWN0aW9uPSJzYXZlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDoxN2YyNGRmYy0zNDY4LWI4NGQtYWY0Zi1mNzQ2ZTgzNDYzNWYiIHN0RXZ0OndoZW49IjIwMTktMDctMjNUMTE6MTQ6MTgrMDU6MzAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE5IChXaW5kb3dzKSIgc3RFdnQ6Y2hhbmdlZD0iLyIvPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0iY29udmVydGVkIiBzdEV2dDpwYXJhbWV0ZXJzPSJmcm9tIGFwcGxpY2F0aW9uL3ZuZC5hZG9iZS5waG90b3Nob3AgdG8gaW1hZ2UvcG5nIi8+IDxyZGY6bGkgc3RFdnQ6YWN0aW9uPSJkZXJpdmVkIiBzdEV2dDpwYXJhbWV0ZXJzPSJjb252ZXJ0ZWQgZnJvbSBhcHBsaWNhdGlvbi92bmQuYWRvYmUucGhvdG9zaG9wIHRvIGltYWdlL3BuZyIvPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0ic2F2ZWQiIHN0RXZ0Omluc3RhbmNlSUQ9InhtcC5paWQ6OWI3NDY4YTUtOTY5MS1jMzRlLWJjOGMtNWM0YjkwOWJmMDU5IiBzdEV2dDp3aGVuPSIyMDE5LTA3LTIzVDExOjE0OjE4KzA1OjMwIiBzdEV2dDpzb2Z0d2FyZUFnZW50PSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOSAoV2luZG93cykiIHN0RXZ0OmNoYW5nZWQ9Ii8iLz4gPC9yZGY6U2VxPiA8L3htcE1NOkhpc3Rvcnk+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjE3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");
			image.setFileName("ngdesk.png");
			image.setHeader("ngDesk");
			imgGallery.setLogo(image);

			imgGallery.setCompanyId(company.getCompanyId());
			galleryRepository.save(imgGallery, "image_gallery");
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("NGDESK_LOGO_POST_FAILED", null);
		}

	}

	public void setUserTracker(Company company) {
		try {
			Tracker usrTracker = new Tracker();
			Step step = new Step();
			step.setCreatedFirstTicket(false);
			step.setInvitedUser(false);
			usrTracker.setSteps(step);
			trackerRepository.save(usrTracker, "users_tracking_" + company.getCompanyId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("USER_TRACKER_POST_FAILED", null);
		}
	}

	public void sendVerificationEmail(@Valid Company company) {
		// HARDCODED AS USER UUID NEVER CHANGES
		String userUUID = "8f3ce655-95f8-4ec8-b55b-0e05b60ec449";

		String subdomain = company.getCompanySubdomain();
		String emailAddress = company.getEmailAddress();

		String emailTo = emailAddress.toLowerCase();
		String from = "support@" + subdomain + ".ngdesk.com";
		String subject = "Verify your email address";
		String body = "<html><body>Hello " + company.getFirstName() + " " + company.getLastName()
				+ ",<br><br>Thank you for signing up with ngDesk.<br><br>To complete the setup, <a href=\"https://"
				+ subdomain + ".ngdesk.com/email/verify?email=" + emailAddress + "&uuid=" + userUUID
				+ "\">click here to verify your email address</a>.<br><br>After you're verified, you can sign in to your ngDesk account at <a href=\"https://"
				+ subdomain + ".ngdesk.com\">https://" + subdomain
				+ ".ngdesk.com</a>. Please bookmark this URL now.<br><br>ngDesk is also available for mobile. Visit the <a href=\"https://play.google.com/store/apps/details?id=com.ngdesk.ngdesk2\">Google Play Store</a> or <a href=\"https://apps.apple.com/us/app/ngdesk-support/id1444147469\">Apple App Store</a> to download the app on your phone. <br><br>If you have questions, contact our team at <a href=\"mailto:support@ngdesk.com\">support@ngdesk.com</a><br>Your ngDesk team</body></html>";
		try {
			HtmlEmail email = new HtmlEmail() {
				protected MimeMessage createMimeMessage(Session session) {
					return new MimeMessage(session) {
						protected void updateHeaders() {
							try {
								super.updateHeaders();
								super.setHeader("Message-ID", UUID.randomUUID().toString() + "@ngdesk.com");
							} catch (MessagingException e) {
								e.printStackTrace();
							}
						}
					};
				}
			};

			email.setHostName(host);
			email.setSmtpPort(emailPort);
			email.addTo(emailTo);
			email.setFrom(from);
			email.setSubject(subject);
			email.setHtmlMsg(body);
			email.setCharset("utf-8");

			email.send();

		} catch (EmailException e) {
			e.printStackTrace();
		}

	}

	public void notifyEmailToSpencerAndSandra(Company company) {

		String subdomain = company.getCompanySubdomain();
		String emailAddress = company.getEmailAddress();
		String phoneNumber = company.getPhone().getPhoneNumber();
		String dialCode = company.getPhone().getDialCode();
		String from = "support@" + subdomain + ".ngdesk.com";

		String subject = company.getCompanySubdomain() + " selected " + company.getPricing() + " as Pricing category";

		String body = "<html> <body> Hi REPLACE_NAME,<br> <br> subdomain have signed up for ngdesk and have selected  "
				+ company.getPricing() + " as pricing category.<br> <br> Contact Details <br> <br> NAME:"
				+ company.getFirstName() + " " + company.getLastName() + " <br> Company Name:"
				+ company.getCompanyName() + " <br> Company Subdomain: " + company.getCompanySubdomain()
				+ "<br>	Phone Number:" + dialCode + " " + phoneNumber + "<br>    Email Address:"
				+ company.getEmailAddress() + " <br><br>	Thanks, <br>ngDesk Team.    </body></html>";

		body = body.replace("REPLACE_NAME", "Spencer Fontein");

		sendmail.send("spencer@allbluesolutions.com", from, subject, body);

	}

	public void sendEmailFromSendGrid(Company company) {

		Email from = new Email("support@ngdesk.com");

		Email to = new Email(company.getEmailAddress());
		String subject = "Welcome to ngDesk";
		Content content = new Content("text/html", "Welcome to ngDesk");

		Mail mail = new Mail(from, subject, to, content);

		// TODO: REPLACE THIS WITH KEY IN APP PROPERTIES
		SendGrid sg = new SendGrid(this.sgKey);

		Request request = new Request();

		Personalization personalization = new Personalization();

		personalization.addDynamicTemplateData("First_Name", company.getFirstName()); // FIRST NAME OF CUSTOMER

		personalization.addTo(to);

		mail.addPersonalization(personalization);

		mail.setTemplateId("d-0a7a9d71a44e40fe9618923babdd7719");

		try {
			request.setMethod(Method.POST);

			request.setEndpoint("mail/send");

			request.setBody(mail.build());

			Response response = sg.api(request);

		} catch (IOException e1) {

			e1.printStackTrace();
		}

	}

	public ChatSettings setChatSettings(Company company) {
		if (company.getChatSettings() != null) {
			ChatSettings chatSettings = company.getChatSettings();
			if (chatSettings.getMaxChatsPerAgent() <= 0 || chatSettings.getMaxChatsPerAgent() > 5) {
				throw new BadRequestException("MAX_NUMBER_OF_CHATS_PER_AGENT", null);
			}
			return chatSettings;
		} else {
			ChatSettings chatSettings = new ChatSettings();
			chatSettings.setMaxChatsPerAgent(5);
			return chatSettings;

		}

	}

}
