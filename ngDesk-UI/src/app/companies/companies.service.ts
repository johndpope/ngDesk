import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

import { registerLocaleData } from '@angular/common';
import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class CompaniesService {
	public static translations;
	public userSidebar: any = {};
	private _locale: string;
	private adminSignup = new BehaviorSubject<any>(false);
	public eventMetaData: any = {};

	constructor(private http: HttpClient, private globals: AppGlobals) {}

	public get locale(): string {
		return this._locale || 'en-US';
	}

	public setlocale(value: string) {
		this._locale = value;
	}

	public setLocaleData() {
		/*Locale*/
		this.getGeneralSettings().subscribe(
			(settings: any) => {
				this.setlocale(settings.LOCALE);
				if (settings.LOCALE !== 'en-US') {
					import(`@angular/common/locales/${settings.LOCALE}.js`).then(
						(module) => {
							registerLocaleData(module.default);
						}
					);
				}
			},
			(error: any) => {
				// set default locale if no locale is found
				this.setlocale('en-US');
			}
		);
	}

	public getModuleSidebar() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/sidebar/role`);
	}

	public getCompanySignupMessage() {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/signup_message`
		);
	}

	public getGettingStarted(subdomain) {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/getting-started-status`,
			{ params: new HttpParams().set('company_subdomain', subdomain) }
		);
	}

	public getFirstSignin(subdomain) {
		return this.http.get(`${this.globals.baseRestUrl}/companies/first-signin`, {
			params: new HttpParams().set('company_subdomain', subdomain),
		});
	}

	public getUsageType(subdomain) {
		return this.http.get(`${this.globals.baseRestUrl}/companies/usage-type`, {
			params: new HttpParams().set('company_subdomain', subdomain),
		});
	}

	public getReferralsCount() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/referrals`);
	}

	public getAllGettingStarted() {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/getting-started/`
		);
	}
	public putGettingStarted(gettingStarted) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/getting-started`,
			gettingStarted
		);
	}

	public postGettingStartedResendEmail(user, subdomain) {
		return this.http.post(
			`${this.globals.baseRestUrl}/company/resend-email`,
			user,
			{ params: new HttpParams().set('subdomain', subdomain) }
		);
	}

	public putFirstSignin(subdomain) {
		const request = {
			SUBDOMAIN: subdomain,
		};
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/first-signin`,
			request
		);
	}

	public putGettingStartedStatus(subdomain) {
		const request = {
			SUBDOMAIN: subdomain,
		};
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/getting-started-status`,
			request
		);
	}

	public getCompanyInviteEmailMessage() {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/invite_message`
		);
	}
	public postUserInvite(users) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/users/invite`,
			users
		);
	}

	public putCompanyInviteEmailMessage(body) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/invite_message`,
			body
		);
	}

	public putCompanySignupMessage(body) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/signup_message`,
			body
		);
	}

	// GET call for company Security
	public getSecurity() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/security`);
	}

	// PUT call for company Security
	public putSecurity(security) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/security`,
			security
		);
	}

	// GET call for sidebar
	public getSidebar() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/sidebar`);
	}

	// PUT call for sidebar
	public putSidebar(sidebar) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/sidebar`,
			sidebar
		);
	}

	// GET call for themes
	public getTheme() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/themes`);
	}

	// PUT call for themes
	public putTheme(themes) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/themes`,
			themes
		);
	}

	// POST call for signup
	public postSignup(customer, captcha) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/customers`,
			customer,
			{ params: new HttpParams().set('g-recaptcha-response', captcha) }
		);
	}

	public getRoleById(roleId) {
		return this.http.get(`${this.globals.baseRestUrl}/roles/${roleId}`);
	}

	public putRoleById(roleId, body) {
		return this.http.put(`${this.globals.baseRestUrl}/roles/${roleId}`, body);
	}

	public getAllSchedules(sortBy, orderBy, page, pageSize) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(`${this.globals.baseRestUrl}/companies/schedules`, {
			params: httpParams,
		});
	}

	public getSchedules() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/schedules`);
	}

	public getSchedule(scheduleName) {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/schedules/${scheduleName}`
		);
	}

	public postSchedule(schedule) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/schedules/${schedule.name}`,
			schedule
		);
	}

	public putSchedule(schedule) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/schedules/${schedule.name}`,
			schedule
		);
	}

	public deleteSchedule(scheduleName) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/companies/schedules/${scheduleName}`
		);
	}

	public deleteSchedules(schedules) {
		return this.http.request(
			'delete',
			`${this.globals.baseRestUrl}/companies/schedules`,
			{ body: schedules }
		);
	}
	// POST call for requesting forgot password email
	public postForgotPassword(email, captcha) {
		const httpParams = new HttpParams().set('g-recaptcha-response', captcha);
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/users/forgot_password`,
			email,
			{ params: httpParams }
		);
	}

	public resetPassword(body) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/users/reset_password`,
			body
		);
	}

	public checkSubdomain(subdomain) {
		const httpParams = new HttpParams().set('company_subdomain', subdomain);
		return this.http.get(`${this.globals.baseRestUrl}/companies/subdomain`, {
			params: httpParams,
		});
	}

	public deleteCompany(companyDeleteObject) {
		return this.http.request(
			'delete',
			`${this.globals.baseRestUrl}/companies`,
			{ body: companyDeleteObject }
		);
	}

	public putCustomLogin(loginCustomization) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/custom/login_page`,
			loginCustomization
		);
	}

	public getQuestionCount() {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/question/count`
		);
	}
	public postIndustryQuestion(industry) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/industry`,
			null,
			{ params: new HttpParams().set('industry', industry) }
		);
	}

	public postDepartmentQuestion(department) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/department`,
			null,
			{ params: new HttpParams().set('department', department) }
		);
	}

	public postCompanySizeQuestion(size) {
		return this.http.post(`${this.globals.baseRestUrl}/companies/size`, null, {
			params: new HttpParams().set('size', encodeURIComponent(size)),
		});
	}

	public postUsage(usageType) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/usage_type`,
			usageType
		);
	}

	public getPendingInvites(sortBy, orderBy, page, pageSize) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/users/invite/pending`,
			{ params: httpParams }
		);
	}

	public postResendInvites(resendInvite) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/users/invite/resend`,
			resendInvite
		);
	}
	public postInterest(interest) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/interest`,
			null,
			{ params: new HttpParams().set('interest', interest) }
		);
	}

	public postPlatform(platform) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/platform`,
			null,
			{ params: new HttpParams().set('platform', platform) }
		);
	}

	public postGettingStartedClick(isClicked) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/getting_started`,
			null,
			{ params: new HttpParams().set('getting_started', isClicked) }
		);
	}

	public postCname(cname) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/dns/cname?cname=${cname}`,
			null
		);
	}

	public getCname() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/dns/cname`);
	}

	public deleteCname() {
		return this.http.delete(`${this.globals.baseRestUrl}/companies/dns/cname`);
	}

	public putGeneralSettings(generalSettings) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/general`,
			generalSettings
		);
	}

	public getGeneralSettings() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/general`);
	}

	public getLanguage(domain) {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/language?domain=${domain}`
		);
	}

	public getEnrollment() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/enrollment`);
	}

	public getAdminSignup(): Observable<any> {
		return this.adminSignup.asObservable();
	}

	public setAdminSignup(status: boolean) {
		this.adminSignup.next({ status: status });
	}

	public getBlacklistWhitelist() {
		return this.http.get(
			`${this.globals.baseRestUrl}/blacklistwhitelist/emails`
		);
	}

	public putBlacklistWhitelist(emailList) {
		return this.http.put(
			`${this.globals.baseRestUrl}/blacklistwhitelist/emails`,
			emailList
		);
	}

	public getKnowledgeBaseGeneralSettings() {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/knowledgebase/general`
		);
	}

	public putKnowledgeBaseGeneralSettings(generalSettings) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/knowledgebase/general`,
			generalSettings
		);
	}

	public getChatGeneralSettings() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/chat/general`);
	}

	public putChatGeneralSettings(generalSettings) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/chat/general`,
			generalSettings
		);
	}

	public getSpfRecords() {
		return this.http.get(`${this.globals.baseRestUrl}/spf/records/all`);
	}

	public getSpfRecordById(spfId) {
		return this.http.get(`${this.globals.baseRestUrl}/spf/${spfId}`);
	}

	public deleteSpfRecord(spfRecordId) {
		return this.http.delete(`${this.globals.baseRestUrl}/spf/${spfRecordId}`);
	}

	public postSpfRecord(domain) {
		return this.http.post(`${this.globals.baseRestUrl}/spf`, null, {
			params: new HttpParams().set('domain', domain),
		});
	}

	public putSpfRecord(spfId, domain) {
		return this.http.put(`${this.globals.baseRestUrl}/spf/${spfId}`, null, {
			params: new HttpParams().set('domain', domain),
		});
	}

	public getSocialSignInSettings() {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/security/social_sign_in`
		);
	}

	public putSocialSignInSettings(socialSignIn) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/security/social_sign_in`,
			socialSignIn
		);
	}

	public trackEvent(name: string, props?: any) {
		this.eventMetaData.EVENT_NAME = name;
		this.eventMetaData.CUSTOM_PROPERTIES = props;
		this.eventMetaData.CURRENT_URL = (<any>window).location.href;
		this.http
			.post(
				`${this.globals.baseRestUrl}/companies/track/user/event`,
				this.eventMetaData
			)
			.subscribe(
				(response: any) => {
					// POSTED SUCCESSFULLY
				},
				(error: any) => {
					console.log(error);
				}
			);
	}

	public getCampaigns() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/campaigns`);
	}

	public getAllCampaigns(sortBy, orderBy, page, pageSize) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(`${this.globals.baseRestUrl}/companies/campaigns`, {
			params: httpParams,
		});
	}

	public getCampaignById(campaignId: string) {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/campaign/${campaignId}`
		);
	}

	public postCampaign(campaign) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/campaign/${campaign.NAME}`,
			campaign
		);
	}

	public putCampaign(campaign) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/campaign/${campaign.CAMPAIGN_ID}`,
			campaign
		);
	}

	public deleteCampaign(campaignId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/companies/campaign/${campaignId}`
		);
	}

	public getEmailLists() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/email_lists`);
	}

	public getAllEmailLists(sortBy, orderBy, page, pageSize) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.get(`${this.globals.baseRestUrl}/companies/email_lists`, {
			params: httpParams,
		});
	}

	public getEmailListById(emailListId: string) {
		return this.http.get(
			`${this.globals.baseRestUrl}/companies/email_list/${emailListId}`
		);
	}

	public postEmailList(emailList) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/email_list`,
			emailList
		);
	}

	public getEmailListData(emailList, moduleId) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/email_list/${moduleId}/data`,
			emailList
		);
	}

	public getAllEmailListData(
		emailList,
		moduleId,
		sortBy,
		orderBy,
		page,
		pageSize
	) {
		const httpParams = new HttpParams()
			.set('sort', sortBy)
			.set('order', orderBy)
			.set('page', page)
			.set('page_size', pageSize);
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/email_list/${moduleId}/data`,
			emailList,
			{ params: httpParams }
		);
	}

	public putEmailList(emailList) {
		return this.http.put(
			`${this.globals.baseRestUrl}/companies/email_list/${emailList.emailListId}`,
			emailList
		);
	}

	public deleteEmailList(emailListId) {
		return this.http.delete(
			`${this.globals.baseRestUrl}/companies/email_list/${emailListId}`
		);
	}

	public sendCampaign(campaign) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/campaigns/send`,
			null,
			{ params: new HttpParams().set('campaign_id', campaign.CAMPAIGN_ID) }
		);
	}

	public sendTestEmail(campaign) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/campaigns/send/test`,
			campaign
		);
	}

	public postGalleryImage(image) {
		return this.http.post(
			`${this.globals.baseRestUrl}/companies/gallery/image`,
			image
		);
	}

	public getGallery() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/gallery`);
	}

}
