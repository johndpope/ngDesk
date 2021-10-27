import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { ModulesService } from 'src/app/modules/modules.service';

@Component({
	selector: 'app-chat-bot-getting-started',
	templateUrl: './chat-bot-getting-started.component.html',
	styleUrls: ['./chat-bot-getting-started.component.scss'],
})
export class ChatBotGettingStartedComponent implements OnInit {
	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private modulesService: ModulesService,
		public customTableService: CustomTableService,
		public bannerMessageService: BannerMessageService
	) {}
	public isLoading = true;
	public moduleId: any;
	public navigations = [];
	public module: any;
	public allChatBots;
	public ngOnInit() {
		this.isLoading = true;
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.module = response;

				if (response.NAME === 'Tickets') {
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
						{
							NAME: 'CHAT_BOTS',
							PATH: ['', 'modules', this.moduleId, 'chatbots'],
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
						// {
						//   NAME: 'CHANNELS',
						//   PATH: [
						//     '',
						//     {
						//       outlets: {
						//         main: ['modules', this.moduleId, 'channels']
						//       }
						//     }
						//   ]
						// }
					];
				}
				this.isLoading = false;
			},

			(moduleError: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: moduleError.error.ERROR,
				});
				this.isLoading = false;
			}
		);
	}

	public createNew() {
		this.router.navigate([`modules/${this.moduleId}/chatbots/new`]);
	}
	public createWithTemplate() {
		this.router.navigate([`modules/${this.moduleId}/chatbots/templates`]);
	}
}
