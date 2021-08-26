import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root'
})
export class ChannelsService {
	public twilioSupportedCountries = [
		{
			COUNTRY_NAME: 'Argentina',
			COUNTRY_CODE: 'AR',
			COUNTRY_FLAG: 'ar.svg'
		},
		{
			COUNTRY_NAME: 'Australia',
			COUNTRY_CODE: 'AU',
			COUNTRY_FLAG: 'au.svg'
		},
		{
			COUNTRY_NAME: 'Austria',
			COUNTRY_CODE: 'AT',
			COUNTRY_FLAG: 'at.svg'
		},
		{
			COUNTRY_NAME: 'Bahrain',
			COUNTRY_CODE: 'BH',
			COUNTRY_FLAG: 'bh.svg'
		},
		{
			COUNTRY_NAME: 'Belgium',
			COUNTRY_CODE: 'BE',
			COUNTRY_FLAG: 'be.svg'
		},
		{
			COUNTRY_NAME: 'Bolivia',
			COUNTRY_CODE: 'BO',
			COUNTRY_FLAG: 'bo.svg'
		},
		{
			COUNTRY_NAME: 'Brazil',
			COUNTRY_CODE: 'BR',
			COUNTRY_FLAG: 'br.svg'
		},
		{
			COUNTRY_NAME: 'Bulgaria',
			COUNTRY_CODE: 'BG',
			COUNTRY_FLAG: 'bg.svg'
		},
		{
			COUNTRY_NAME: 'Colombia',
			COUNTRY_CODE: 'CO',
			COUNTRY_FLAG: 'co.svg'
		},
		{
			COUNTRY_NAME: 'Czech Republic',
			COUNTRY_CODE: 'CZ',
			COUNTRY_FLAG: 'cz.svg'
		},
		{
			COUNTRY_NAME: 'Denmark',
			COUNTRY_CODE: 'DK',
			COUNTRY_FLAG: 'dk.svg'
		},
		{
			COUNTRY_NAME: 'Dominican Republic',
			COUNTRY_CODE: 'DO',
			COUNTRY_FLAG: 'do.svg'
		},
		{
			COUNTRY_NAME: 'Egypt',
			COUNTRY_CODE: 'EG',
			COUNTRY_FLAG: 'eg.svg'
		},
		{
			COUNTRY_NAME: 'Estonia',
			COUNTRY_CODE: 'EE',
			COUNTRY_FLAG: 'ee.svg'
		},
		{
			COUNTRY_NAME: 'Finland',
			COUNTRY_CODE: 'FI',
			COUNTRY_FLAG: 'fi.svg'
		},
		{
			COUNTRY_NAME: 'France',
			COUNTRY_CODE: 'FR',
			COUNTRY_FLAG: 'fr.svg'
		},
		{
			COUNTRY_NAME: 'Germany',
			COUNTRY_CODE: 'DE',
			COUNTRY_FLAG: 'de.svg'
		},
		{
			COUNTRY_NAME: 'Ghana',
			COUNTRY_CODE: 'GH',
			COUNTRY_FLAG: 'gh.svg'
		},
		{
			COUNTRY_NAME: 'Greece',
			COUNTRY_CODE: 'GR',
			COUNTRY_FLAG: 'gr.svg'
		},
		{
			COUNTRY_NAME: 'Hong Kong',
			COUNTRY_CODE: 'HK',
			COUNTRY_FLAG: 'hk.svg'
		},
		{
			COUNTRY_NAME: 'Hungary',
			COUNTRY_CODE: 'HU',
			COUNTRY_FLAG: 'hu.svg'
		},
		{
			COUNTRY_NAME: 'Iceland',
			COUNTRY_CODE: 'IS',
			COUNTRY_FLAG: 'is.svg'
		},
		{
			COUNTRY_NAME: 'Ireland',
			COUNTRY_CODE: 'IE',
			COUNTRY_FLAG: 'ie.svg'
		},
		{
			COUNTRY_NAME: 'Italy',
			COUNTRY_CODE: 'IT',
			COUNTRY_FLAG: 'it.svg'
		},
		{
			COUNTRY_NAME: 'Japan',
			COUNTRY_CODE: 'JP',
			COUNTRY_FLAG: 'jp.svg'
		},
		{
			COUNTRY_NAME: 'Lithuania',
			COUNTRY_CODE: 'LT',
			COUNTRY_FLAG: 'lt.svg'
		},
		{
			COUNTRY_NAME: 'Luxembourg',
			COUNTRY_CODE: 'LU',
			COUNTRY_FLAG: 'lu.svg'
		},
		{
			COUNTRY_NAME: 'Malaysia',
			COUNTRY_CODE: 'MY',
			COUNTRY_FLAG: 'my.svg'
		},
		{
			COUNTRY_NAME: 'Mexico',
			COUNTRY_CODE: 'MX',
			COUNTRY_FLAG: 'mx.svg'
		},
		{
			COUNTRY_NAME: 'Moldova',
			COUNTRY_CODE: 'MD',
			COUNTRY_FLAG: 'md.svg'
		},
		{
			COUNTRY_NAME: 'Netherlands',
			COUNTRY_CODE: 'NL',
			COUNTRY_FLAG: 'nl.svg'
		},
		{
			COUNTRY_NAME: 'New Zealand',
			COUNTRY_CODE: 'NZ',
			COUNTRY_FLAG: 'nz.svg'
		},
		{
			COUNTRY_NAME: 'Nigeria',
			COUNTRY_CODE: 'NG',
			COUNTRY_FLAG: 'ng.svg'
		},
		{
			COUNTRY_NAME: 'Norway',
			COUNTRY_CODE: 'NO',
			COUNTRY_FLAG: 'no.svg'
		},
		{
			COUNTRY_NAME: 'Poland',
			COUNTRY_CODE: 'PL',
			COUNTRY_FLAG: 'pl.svg'
		},
		{
			COUNTRY_NAME: 'Portugal',
			COUNTRY_CODE: 'PT',
			COUNTRY_FLAG: 'pt.svg'
		},
		{
			COUNTRY_NAME: 'Qatar',
			COUNTRY_CODE: 'QA',
			COUNTRY_FLAG: 'qa.svg'
		},
		{
			COUNTRY_NAME: 'Romania',
			COUNTRY_CODE: 'RO',
			COUNTRY_FLAG: 'ro.svg'
		},
		{
			COUNTRY_NAME: 'Saudi Arabia',
			COUNTRY_CODE: 'SA',
			COUNTRY_FLAG: 'sa.svg'
		},
		{
			COUNTRY_NAME: 'Singapore',
			COUNTRY_CODE: 'SG',
			COUNTRY_FLAG: 'sg.svg'
		},
		{
			COUNTRY_NAME: 'Slovakia',
			COUNTRY_CODE: 'SK',
			COUNTRY_FLAG: 'sk.svg'
		},
		{
			COUNTRY_NAME: 'Slovenia',
			COUNTRY_CODE: 'SI',
			COUNTRY_FLAG: 'si.svg'
		},
		{
			COUNTRY_NAME: 'South Africa',
			COUNTRY_CODE: 'ZA',
			COUNTRY_FLAG: 'za.svg'
		},
		{
			COUNTRY_NAME: 'South Korea',
			COUNTRY_CODE: 'KR',
			COUNTRY_FLAG: 'kr.svg'
		},
		{
			COUNTRY_NAME: 'Spain',
			COUNTRY_CODE: 'ES',
			COUNTRY_FLAG: 'es.svg'
		},
		{
			COUNTRY_NAME: 'Sweden',
			COUNTRY_CODE: 'SE',
			COUNTRY_FLAG: 'se.svg'
		},
		{
			COUNTRY_NAME: 'Switzerland',
			COUNTRY_CODE: 'CH',
			COUNTRY_FLAG: 'ch.svg'
		},
		{
			COUNTRY_NAME: 'Turkey',
			COUNTRY_CODE: 'TR',
			COUNTRY_FLAG: 'tr.svg'
		},
		{
			COUNTRY_NAME: 'United Arab Emirates',
			COUNTRY_CODE: 'AE',
			COUNTRY_FLAG: 'ae.svg'
		},
		{
			COUNTRY_NAME: 'United Kingdom',
			COUNTRY_CODE: 'GB',
			COUNTRY_FLAG: 'gb.svg'
		},
		{
			COUNTRY_NAME: 'Venezuela',
			COUNTRY_CODE: 'VE',
			COUNTRY_FLAG: 've.svg'
		},
		{
			COUNTRY_NAME: 'Vietnam',
			COUNTRY_CODE: 'VN',
			COUNTRY_FLAG: 'vn.svg'
		}
	];
	constructor(private http: HttpClient, private globals: AppGlobals) {}

	// EMAIL CHANNEL
	public getEmailChannel(moduleId, channelName) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/email/${channelName}`
		);
	}

	public getAllChannels(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels`
		);
	}

	public postTicketEmailChannel(moduleId, channel) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/email`,
			channel
		);
	}

	public putEmailChannel(moduleId, channel) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/email/${channel.NAME}`,
			channel
		);
	}

	public postEmailVerify(moduleId, channel) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/email/${channel.NAME}/forwarding/send`,
			channel
		);
	}

	// FACEBOOK Channel
	public getFacebookChannel(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/facebook`
		);
	}

	public postFacebookChannel(moduleId, channel) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/facebook`,
			channel
		);
	}

	public putFacebookChannel(moduleId, channel) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/facebook`,
			channel
		);
	}

	public unlinkFacebookPage(moduleId, pageId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/facebook/pages/${pageId}`
		);
	}

	public getFacebookChannelData(channelId: string) {
		return this.http.get(
			`${this.globals.baseRestUrl}/facebook/channel/${channelId}/pages`
		);
	}
	public getFacebookPages(channelId: String, moduleId: String) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/facebook/${channelId}/pages`
		);
	}

	// SMS REST CALLS

	public getSMSChannel(moduleId, channelId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/sms/${channelId}`
		);
	}

	public postSMSChannel(moduleId, channel) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/sms`,
			channel
		);
	}

	public putSMSChannel(moduleId, channel) {
		return this.http.put(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/sms`,
			channel
		);
	}

	// COMMON REST CALLS
	public getChannels(
		moduleId,
		channelType,
		pageSize?,
		page?,
		sortBy?,
		orderBy?
	) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/${channelType}`,
			{ params: httpParams }
		);
	}

	public deleteChannel(moduleId, channelType, channelId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/${channelType}/${channelId}`
		);
	}

	public getChatChannels() {
		return this.http.get(`${this.globals.baseRestUrl}/channels/chat`);
	}

	public getChatChannel(chatName) {
		return this.http.get(
			`${this.globals.baseRestUrl}/channels/chat/${chatName}`
		);
	}

	public putChatChannel(chatChannel) {
		return this.http.put(
			`${this.globals.baseRestUrl}/channels/chat/${chatChannel.NAME}`,
			chatChannel
		);
	}
	//
	public postPrompt(prompt, chatName) {
		return this.http.post(
			`${this.globals.baseRestUrl}/channels/chat/${chatName}/prompt`,
			prompt
		);
	}

	public putPrompt(prompt, chatName, promptId) {
		return this.http.put(
			`${this.globals.baseRestUrl}/channels/chat/${chatName}/prompt/${promptId}`,
			prompt
		);
	}
	public deletePrompt(promptId, chatName) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/channels/chat/${chatName}/prompt/${promptId}`
		);
	}

	public getPrompt(chatName, promptId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/channels/chat/${chatName}/prompt/${promptId}`
		);
	}

	public getPrompts(chatName) {
		return this.http.get(
			`${this.globals.baseRestUrl}/channels/chat/${chatName}/prompt`
		);
	}

	// SEND TWILIO REUEST
	public sendEmailForTwilioRequest(moduleId, twilioRequest) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/sms/request`,
			twilioRequest
		);
	}

	public getTwilioSupportedCountries(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/sms/phone_numbers/countries`
		);
	}

	public getTwilioPhoneNumbers(moduleId, countryCode) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/sms/phone_numbers/countries/${countryCode}`
		);
	}

	public postWhatsappRequest(moduleId, channelId) {
		return this.http.post(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/sms/${channelId}/request/whatsapp`,
			{}
		);
	}

	public getFaqs() {
		return this.http.get(`${this.globals.baseRestUrl}/faqs`);
	}

	public getFaq(faqid) {
		return this.http.get(`${this.globals.baseRestUrl}/faqs/${faqid}`);
	}

	public postFaqs(chatFaq) {
		return this.http.post(`${this.globals.baseRestUrl}/faqs`, chatFaq);
	}

	public putFaqs(faqid, chatFaq) {
		return this.http.put(`${this.globals.baseRestUrl}/faqs/${faqid}`, chatFaq);
	}

	public deleteFaqs(faqid) {
		return this.http.delete(`${this.globals.baseRestUrl}/faqs/${faqid}`);
	}

	public getEmailChannelsByModule(moduleId) {
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/email`
		);
	}

	public emailToDevelopers(chatChannel, developerEmails) {
		return this.http.post(
			`${this.globals.baseRestUrl}/channels/chat/${chatChannel.NAME}/email`,
			developerEmails
		);
	}
	//
	public referralEmail(emails) {
		return this.http.post(
			`${this.globals.baseRestUrl}/company/refer/email`,
			emails
		);
	}

	public getFromEmail(moduleId) {
		const httpParams = new HttpParams()
			.set('sort', 'EMAIL_ADDRESS')
			.set('order', 'asc');
		return this.http.get(
			`${this.globals.baseRestUrl}/modules/${moduleId}/channels/email/from`,
			{ params: httpParams }
		);
	}
}
