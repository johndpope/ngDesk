import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { ChannelsService } from '../../../channels/channels.service';
import { CompaniesService } from '../../../companies/companies.service';
import { BannerMessageService } from '../../../custom-components/banner-message/banner-message.service';
import { FacebookChannel } from '../../../models/facebook-channel';
import { ModulesService } from '../../modules.service';

@Component({
	selector: 'app-channel',
	templateUrl: './channel.component.html',
	styleUrls: ['./channel.component.scss'],
})
export class ChannelComponent implements OnInit {
	private facebookChannel: FacebookChannel = new FacebookChannel('', '', '');
	private moduleId: string;
	public module: any;
	public isLoading = true;
	public showSideNav = true;
	public navigations = [];
	// CHANGE TO FALSE
	public dialogRef: any;

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		public dialog: MatDialog,
		private channelService: ChannelsService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		private companiesService: CompaniesService
	) {}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.isLoading = false;
				this.module = response;
				if (this.module.NAME === 'Tickets') {
					this.navigations = [
						{
							NAME: 'MODULE_DETAIL',
							PATH: ['', 'modules', this.moduleId],
						},
						{
							NAME: 'FIELDS',
							PATH: ['', 'modules', this.moduleId, 'fields'],
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts'],
						},
						{
							NAME: 'VALIDATIONS',
							PATH: ['', 'modules', this.moduleId, 'validations'],
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows'],
						},
						{
							NAME: 'SLAS',
							PATH: ['', 'modules', this.moduleId, 'slas'],
						},
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
						},
						{
							NAME: 'FORMS',
							PATH: ['', 'modules', this.moduleId, 'forms'],
						},
						{
							NAME: 'PDFs',
							PATH: ['', 'modules', this.moduleId, 'pdf'],
						},
						{
							NAME: 'TASK',
							PATH: ['', 'modules', this.moduleId, 'task'],
						},
					];
				} else if (response.NAME === 'Chats') {
					this.navigations = [
						{
							NAME: 'MODULE_DETAIL',
							PATH: ['', 'modules', this.moduleId],
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts'],
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows'],
						},
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
						},
					];
				} else {
					this.navigations = [
						{
							NAME: 'MODULE_DETAIL',
							PATH: ['', 'modules', this.moduleId],
						},
						{
							NAME: 'FIELDS',
							PATH: ['', 'modules', this.moduleId, 'fields'],
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts'],
						},
						{
							NAME: 'VALIDATIONS',
							PATH: ['', 'modules', this.moduleId, 'validations'],
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows'],
						},
						{
							NAME: 'SLAS',
							PATH: ['', 'modules', this.moduleId, 'slas'],
						},
						{
							NAME: 'FORMS',
							PATH: ['', 'modules', this.moduleId, 'forms'],
						},
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
						},
						{
							NAME: 'PDFs',
							PATH: ['', 'modules', this.moduleId, 'pdf'],
						},
						{
							NAME: 'TASK',
							PATH: ['', 'modules', this.moduleId, 'task'],
						},
					];
				}
			},
			(moduleError: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: moduleError.error.ERROR,
				});
			}
		);
	}

	public navigateTo(layoutName) {
		if (layoutName === 'facebook') {
			this.channelService.getFacebookChannel(this.moduleId).subscribe(
				() => {
					this.router.navigate([
						`modules/${this.moduleId}/channels/${layoutName}/facebook-detail`,
					]);
				},
				(error) => {
					this.facebookChannel.name = 'Facebook';
					this.facebookChannel.description = '';
					this.facebookChannel.module = this.moduleId;
					this.channelService
						.postFacebookChannel(
							this.facebookChannel.module,
							this.facebookChannel
						)
						.subscribe((facebookSuccessResponse: any) => {
							this.companiesService.trackEvent(`Created Channel`, {
								CHANNEL_ID: facebookSuccessResponse.CHANNEL_ID,
								MODULE_ID: this.facebookChannel.module,
							});
							this.router.navigate([
								`modules/${this.moduleId}/channels/facebook/facebook-detail`,
							]);
						});
				}
			);
		} else if (layoutName === 'chatChannel') {
			this.router.navigate([
				`modules/${this.moduleId}/channels/chat-widgets/Chats`,
			]);
		} else {
			this.router.navigate([`modules/${this.moduleId}/channels/${layoutName}`]);
		}
	}
}
