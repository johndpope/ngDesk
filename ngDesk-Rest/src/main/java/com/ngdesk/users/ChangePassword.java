package com.ngdesk.users;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangePassword {
	@JsonProperty("OLD_PASSWORD")
	@NotNull(message = "PASSWORD_NOT_NULL")
	@Size(min = 1, message = "PASSWORD_NOT_EMPTY")
	@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "INCORRECT_OLD_PASSWORD")
	private String oldPassword;

	@JsonProperty("NEW_PASSWORD")
	@NotNull(message = "PASSWORD_NOT_NULL")
	@Size(min = 1, message = "PASSWORD_NOT_EMPTY")
	@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "PASSWORD_INVALID")
	private String newPassword;

	public ChangePassword() {
	}

	public ChangePassword(
			@NotNull(message = "PASSWORD_NOT_NULL") @Size(min = 1, message = "PASSWORD_NOT_EMPTY") @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "INCORRECT_OLD_PASSWORD") String oldPassword,
			@NotNull(message = "PASSWORD_NOT_NULL") @Size(min = 1, message = "PASSWORD_NOT_EMPTY") @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "PASSWORD_INVALID") String newPassword) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

}
