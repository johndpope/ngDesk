import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from '../../../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../../../dialogs/confirm-dialog/confirm-dialog.component';
import { ModulesService } from '../../../../modules.service';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { CompaniesService } from 'src/app/companies/companies.service';

@Component({
	selector: 'app-chat-bots-master',
	templateUrl: './chat-bots-master.component.html',
	styleUrls: ['./chat-bots-master.component.scss']
})
export class ChatBotsMasterComponent implements OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public chatBotActions = {
		actions: [
			{
				NAME: '',
				ICON: 'delete',
				PERMISSION_NAME: 'DELETE'
			}
		]
	};
	public showSideNav = true;
	public navigations = [];
	public moduleId: string;
	public allChatBots;
	public module: any;
	public isLoading = true;
	public totalRecords = 1;
	public chatTemplate: any;
	public botTemplateList1: any=[];
	public botTemplateList2: any=[];
	public backGroundColor: any;

	constructor(
		private translateService: TranslateService,
		private dialog: MatDialog,
		private router: Router,
		private companiesService:CompaniesService,
		private route: ActivatedRoute,
		public customTableService: CustomTableService,
		private bannerMessageService: BannerMessageService,
		private modulesService: ModulesService,
		private matIconRegistry: MatIconRegistry,
		private domSanitizer: DomSanitizer
	) {}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.companiesService.getTheme().subscribe(
            (response: any) => {
                this.backGroundColor=response.SECONDARY_COLOR;
                
            });
		this.modulesService
			.getModuleById(this.moduleId)
			.subscribe((response: any) => {
				this.module = response;
				this.isLoading = false;
				if (response.NAME === 'Tickets') {
					this.navigations = [
						{
							NAME: 'MODULE_DETAIL',
							PATH: ['', 'modules', this.moduleId]
						},
						{
							NAME: 'FIELDS',
							PATH: ['', 'modules', this.moduleId, 'fields']
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts']
						},
						{
							NAME: 'VALIDATIONS',
							PATH: ['', 'modules', this.moduleId, 'validations']
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows']
						},
						{
							NAME: 'SLAS',
							PATH: ['', 'modules', this.moduleId, 'slas']
						},
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels']
						}
					];
				} else if (response.NAME === 'Chat') {
					this.navigations = [
						{
							NAME: 'MODULE_DETAIL',
							PATH: ['', 'modules', this.moduleId]
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts']
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows']
						},
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels']
						},
						{
							NAME: 'CHAT_BOTS',
							PATH: ['', 'modules', this.moduleId, 'chatbots']
						}
					];
				} else {
					this.navigations = [
						{
							NAME: 'MODULE_DETAIL',
							PATH: ['', 'modules', this.moduleId]
						},
						{
							NAME: 'FIELDS',
							PATH: ['', 'modules', this.moduleId, 'fields']
						},
						{
							NAME: 'LAYOUTS',
							PATH: ['', 'modules', this.moduleId, 'layouts']
						},
						{
							NAME: 'VALIDATIONS',
							PATH: ['', 'modules', this.moduleId, 'validations']
						},
						{
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows']
						},
						{
							NAME: 'SLAS',
							PATH: ['', 'modules', this.moduleId, 'slas']
						}
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
			});
		this.customTableService.isLoading = true;
		this.getChatBotTemplates();
	}

	private getChatBotTemplates() {
		const moduleId = this.route.snapshot.params['moduleId'];

		this.modulesService.getChatBotsTemplates(moduleId).subscribe(
			(chatBotsResponce: any) => {
				
				this.allChatBots = chatBotsResponce.CHAT_BOTS;
                let index=0;
                this.allChatBots.forEach(element => {
                    
                    if(index<=3){
                        this.botTemplateList1.push(element);
                    }else{
                        this.botTemplateList2.push(element);
                    }
                    index++;
                });

			},
			(chatBotError: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: chatBotError.error.ERROR
				});
			}
		);
	}

	public rowClicked(rowData): void {
		const templateName = rowData.NAME;
		this.modulesService.settemplate(templateName);
		this.router.navigate([`modules/${this.moduleId}/chatbots/new`]);
	}
}
