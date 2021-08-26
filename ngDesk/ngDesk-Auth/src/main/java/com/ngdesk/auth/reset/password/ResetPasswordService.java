package com.ngdesk.auth.reset.password;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ngdesk.auth.forgot.password.InviteTracking;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.repositories.InviteTrackingRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

@Component
public class ResetPasswordService {

	@Autowired
	InviteTrackingRepository inviteTrackingRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	public void isLinkExpired(String companyId, ResetPassword resetPassword, InviteTracking trackingData) {

		if (trackingData.getType().toString().equals("RESET")) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(trackingData.getDate().getTime());
			calendar.add(Calendar.HOUR, 24);

			if (new Date().after(calendar.getTime())) {
				throw new BadRequestException("LINK_EXPIRED", null);
			}
		}

	}

	public void resetPassword(String companyId, ResetPassword resetPassword, Map<String, Object> optionalUser,
			InviteTracking trackingData) {

		String email = optionalUser.get("EMAIL_ADDRESS").toString().toLowerCase().replaceAll("@", "*") + "*"
				+ resetPassword.getSubDomain();
		String hashPassword = email + ":" + resetPassword.getSubDomain() + ".ngdesk.com:" + resetPassword.getPassword();
		String passwordHashed = passwordHash(hashPassword);
		Map<String, Object> newEnrty = optionalUser;
		newEnrty.put("PASSWORD", passwordHashed);
		newEnrty.put("LOGIN_ATTEMPTS", 0);
		if (trackingData.getType().toString().equalsIgnoreCase("INVITE")) {
			newEnrty.put("INVITE_ACCEPTED", true);

		}
		moduleEntryRepository.updateUserEntry(newEnrty, "Users_" + companyId);
	}

	private String passwordHash(String pwd) {

		String hashedPassword = "";
		if (pwd == "") {
			return "";
		}
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(pwd.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			hashedPassword = bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return hashedPassword;
	}

}
