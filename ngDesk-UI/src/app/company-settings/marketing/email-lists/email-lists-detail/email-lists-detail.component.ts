import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { ChannelsService } from 'src/app/channels/channels.service';
import { CompaniesService } from '../../../../companies/companies.service';
import { BannerMessageService } from '../../../../custom-components/banner-message/banner-message.service';
import { ConditionsComponent } from '../../../../custom-components/conditions/conditions.component';
import { CustomTableService } from '../../../../custom-table/custom-table.service';
import { Condition } from '../../../../models/condition';
import { EmailList } from '../../../../models/email-list';
import { ModulesService } from '../../../../modules/modules.service';
import { UsersService } from '../../../../users/users.service';
import { EmailListService } from '@src/app/company-settings/marketing/email-lists/email-lists.service';
import { RolesService } from '@src/app/roles/roles.service';

@Component({
	selector: 'app-email-lists-detail',
	templateUrl: './email-lists-detail.component.html',
	styleUrls: ['./email-lists-detail.component.scss'],
})
export class EmailListsDetailComponent implements OnInit {
	public emailListForm: FormGroup;
	@ViewChild(ConditionsComponent)
	private conditionsComponent: ConditionsComponent;
	public allEmailListData = [];
	public isLoading = true;
	public fields: any[] = [];
	public emailList: EmailList = new EmailList('', '', []);
	public emailListLoaded = false;
	public moduleId: any;
	public channellist: any;
	public channels: any = [];
	public fieldlist: any;
	public fieldId: any;
	public roleFieldId: any;
	public usersModule: any;

	constructor(
		private formBuilder: FormBuilder,
		private usersService: UsersService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		private customTableService: CustomTableService,
		private translateService: TranslateService,
		private companiesService: CompaniesService,
		private router: Router,
		private route: ActivatedRoute,
		private channelsService: ChannelsService,
		private emailListService: EmailListService,
		private rolesService: RolesService
	) {}

	public ngOnInit() {
		const emailListId = this.route.snapshot.params['emailListId'];
		this.emailListForm = this.formBuilder.group({
			NAME: ['', [Validators.required]],
			DESCRIPTION: [''],
			CONDITIONS: this.formBuilder.array([]),
		});

		this.onConditionChanges();

		this.customTableService.isLoading = true;
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: {
			DISPLAY: string;
			NAME: string;
		}[] = [];
		columnsHeadersObj.push(
			{
				DISPLAY: this.translateService.instant('FIRST_NAME'),
				NAME: 'FIRST_NAME',
			},
			{
				DISPLAY: this.translateService.instant('LAST_NAME'),
				NAME: 'LAST_NAME',
			},
			{
				DISPLAY: this.translateService.instant('EMAIL_ADDRESS'),
				NAME: 'EMAIL_ADDRESS',
			}
		);
		columnsHeaders.push(
			this.translateService.instant('FIRST_NAME'),
			this.translateService.instant('LAST_NAME'),
			this.translateService.instant('EMAIL_ADDRESS')
		);

		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;

		this.customTableService.sortBy = 'DATE_CREATED';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('FIRST_NAME'),
			NAME: 'NAME',
		};
		this.setDatasource(0, 10);

		if (emailListId !== 'new') {
			this.emailListService.getEmailList(emailListId).subscribe(
				(emailListResponse: any) => {
					emailListResponse = emailListResponse.EMAIL_LIST;
					this.emailList = this.convertEmailList(emailListResponse);
					this.emailListForm.get('NAME').setValue(emailListResponse.NAME);
					this.emailListForm
						.get('DESCRIPTION')
						.setValue(emailListResponse.DESCRIPTION);
				},
				(emailListError: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: emailListError.error.ERROR,
					});
				}
			);
		} else {
			this.modulesService
				.getModuleByName('Users')
				.subscribe((modulesResponse: any) => {
					this.usersModule = modulesResponse;
					this.moduleId = modulesResponse.MODULE_ID;
					this.fields = modulesResponse.FIELDS.filter(
						(field) => field.NAME !== 'PASSWORD'
					);
					this.emailListLoaded = true;
				});
		}
	}

	private convertEmailList(listLayoutObj: any): EmailList {
		const conditions: Condition[] = [];
		this.modulesService.getModuleByName('Users').subscribe(
			(modulesResponse: any) => {
				this.usersModule = modulesResponse;
				this.moduleId = modulesResponse.MODULE_ID;
				this.fields = modulesResponse.FIELDS.filter(
					(field) => field.NAME !== 'PASSWORD'
				);
				this.modulesService
					.getFields(this.moduleId)
					.subscribe((response: any) => {
						this.fieldlist = response.FIELDS;
						this.fieldlist.forEach((element) => {
							if (element.NAME === 'CHANNEL') {
								this.fieldId = element.FIELD_ID;
							}
							if (element.NAME === 'ROLE') {
								this.roleFieldId = element.FIELD_ID;
							}
						});
						this.channelsService.getAllChannels(this.moduleId).subscribe(
							(response: any) => {
								this.channellist = response.CHANNELS;
								response.CHANNELS.forEach((element) => {
									this.channels.push({
										value: element.NAME,
										viewValue: element.NAME,
									});
								});
								for (const condition of listLayoutObj.CONDITIONS) {
									if (condition.CONDITION === this.fieldId) {
										this.channellist.forEach((element) => {
											if (element.ID === condition.CONDITION_VALUE) {
												conditions.push(
													new Condition(
														condition.CONDITION,
														element.NAME,
														condition.OPERATOR,
														condition.REQUIREMENT_TYPE
													)
												);
											}
										});
									} else if (condition.CONDITION === this.roleFieldId) {
										this.rolesService
											.getRoleById(condition.CONDITION_VALUE)
											.subscribe((response: any) => {
												conditions.push(
													new Condition(
														condition.CONDITION,
														response,
														condition.OPERATOR,
														condition.REQUIREMENT_TYPE
													)
												);
											});
									} else {
										conditions.push(
											new Condition(
												condition.CONDITION,
												condition.CONDITION_VALUE,
												condition.OPERATOR,
												condition.REQUIREMENT_TYPE
											)
										);
									}
								}
								this.emailListLoaded = true;
							},
							(error) => {
								console.log(error);
							}
						);
					});
			},
			(modulesError: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: modulesError.error.ERROR,
				});
			}
		);

		const newEmailList = new EmailList(
			listLayoutObj.NAME,
			listLayoutObj.DESCRIPTION,
			conditions,
			listLayoutObj.EMAIL_LIST_ID,
			listLayoutObj.DATE_CREATED,
			listLayoutObj.DATE_UPDATED,
			listLayoutObj.LAST_UPDATED_BY,
			listLayoutObj.CREATED_BY
		);
		return newEmailList;
	}

	private onConditionChanges() {
		this.emailListForm
			.get('CONDITIONS')
			.valueChanges.subscribe((conditions) => {
				const incompleteFound = conditions.find(
					(condition) =>
						condition.CONDITION === '' ||
						condition.OPERATOR === '' ||
						condition.CONDITION_VALUE === '' ||
						condition.CONDITION === null ||
						condition.OPERATOR === null ||
						condition.CONDITION_VALUE === null
				);
				if (
					incompleteFound === undefined &&
					!incompleteFound &&
					conditions.length !== 0
				) {
					this.getEmailListData();
				} else if (conditions.length === 0) {
					this.customTableService.setTableDataSource([], 0);
				}
			});
	}

	public sortData() {
		this.getEmailListData();
	}

	public pageChangeEmit(event) {
		this.getEmailListData();
	}

	private getEmailListData() {
		this.emailList['CONDITIONS'] =
			this.conditionsComponent.transformConditions();
		this.emailList['CONDITIONS'] = this.transformCondition(
			this.emailList['CONDITIONS']
		);
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.emailListService
			.getAllEntriesWithConditions(
				this.usersModule,
				page,
				pageSize,
				sortBy,
				orderBy,
				this.emailList['CONDITIONS']
			)
			.subscribe(
				(emailListData: any) => {
					if (emailListData[0].DATA !== null) {
						if (emailListData[1].TOTAL_RECORDS > 0) {
							emailListData[0].DATA.forEach((element) => {
								element['FIRST_NAME'] = element.CONTACT.FIRST_NAME;
								element['LAST_NAME'] = element.CONTACT.LAST_NAME;
							});
							this.isLoading = false;
							this.allEmailListData = emailListData[0].DATA;
							this.customTableService.setTableDataSource(
								emailListData[0].DATA,
								emailListData[1].TOTAL_RECORDS
							);
						} else {
							this.allEmailListData = [];
						}
					}
				},
				(emailListError: any) => {
					this.bannerMessageService.errorNotifications.push({});
				}
			);
	}

	private transformCondition(conditions) {
		let transformedConditions = [];
		conditions.forEach((element) => {
			let conditionObject = {};
			conditionObject['condition'] = element['CONDITION'];
			conditionObject['operator'] = element['OPERATOR'];
			conditionObject['conditionValue'] = element['CONDITION_VALUE'];
			conditionObject['requirementType'] = element['REQUIREMENT_TYPE'];
			transformedConditions.push(conditionObject);
		});
		return transformedConditions;
	}

	private setDatasource(pageIndex, pageSize) {
		const dataSource = this.allEmailListData.slice(
			pageIndex * pageSize,
			pageIndex * pageSize + pageSize
		);
		this.customTableService.setTableDataSource(
			dataSource,
			this.allEmailListData.length
		);
	}

	public save() {
		if (this.emailListForm.valid) {
			this.emailList.name = this.emailListForm.get('NAME').value;
			this.emailList.description = this.emailListForm.get('DESCRIPTION').value;
			this.emailList.conditions =
				this.conditionsComponent.transformConditions();
			const emailListId = this.route.snapshot.params['emailListId'];
			if (emailListId !== 'new') {
				this.companiesService.putEmailList(this.emailList).subscribe(
					(putEmailList: any) => {
						this.router.navigate([`company-settings/marketing/email-lists`]);
					},
					(putEmailListError: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: putEmailListError.error.ERROR,
						});
					}
				);
			} else {
				this.companiesService.postEmailList(this.emailList).subscribe(
					(postEmailList: any) => {
						this.router.navigate([`company-settings/marketing/email-lists`]);
					},
					(postEmailListError: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: postEmailListError.error.ERROR,
						});
					}
				);
			}
		}
	}
}
