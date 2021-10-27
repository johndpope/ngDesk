import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CompaniesService } from '../../../companies/companies.service';
import { ModulesService } from '../../modules.service';

@Component({
	selector: 'app-layouts',
	templateUrl: './layouts.component.html',
	styleUrls: ['./layouts.component.scss'],
})
export class LayoutsComponent implements OnInit {
	private moduleId: string;
	public showSideNav = true;
	public navigations = [];
	public navigationOfLayout = [];
	public matCardFirstRow: any = [];
	public matCardSecondRow: any = [];
	public module: any;
	public isLoading: boolean;

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private modulesService: ModulesService,
		private companiesService: CompaniesService
	) {}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.isLoading = true;
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.isLoading = false;
				this.module = response;
				// module sidebar navigation for module accordingly
				if (this.module.NAME === 'Chats') {
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
							NAME: 'WORKFLOWS',
							PATH: ['', 'modules', this.moduleId, 'workflows'],
						},
						{
							NAME: 'CHANNELS',
							PATH: ['', 'modules', this.moduleId, 'channels'],
						},
					];
				} else if (this.module.NAME === 'Tickets') {
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
				} else {
					{
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
				}
				// Navigation for different layouts
				this.navigationOfLayout = [
					{
						NAME: 'WEB_LIST_LAYOUT',
						PATH: ['', 'modules', this.moduleId, 'list_layouts'],
						SRC: 'view_list',
						DESCRIPTION: 'DESCRIPTION_OF_LIST_LAYOUT',
					},
					{
						NAME: 'WEB_CREATE_LAYOUT',
						PATH: ['', 'modules', this.moduleId, 'create_layouts'],
						SRC: 'tab_unselected',
						DESCRIPTION: 'DESCRIPTION_OF_CREATE_LAYOUT',
					},
					{
						NAME: 'WEB_EDIT_LAYOUT',
						PATH: ['', 'modules', this.moduleId, 'edit_layouts'],
						SRC: 'tab',
						DESCRIPTION: 'DESCRIPTION_OF_EDIT_LAYOUT',
					},
					// {
					// 	NAME: 'WEB_DETAIL_LAYOUT',
					// 	PATH: ['', 'modules', this.moduleId, 'detail_layouts'],
					// 	SRC: 'view_quilt',
					// 	DESCRIPTION: 'DESCRIPTION_OF_DETAIL_LAYOUT',
					// },
					{
						NAME: 'MOBILE_LIST_LAYOUT',
						PATH: ['', 'modules', this.moduleId, 'list_mobile_layouts'],
						SRC: 'menu',
						DESCRIPTION: 'DESCRIPTION_OF_MOBILE_LIST_LAYOUT',
					},
					{
						NAME: 'MOBILE_CREATE_LAYOUT',
						PATH: ['', 'modules', this.moduleId, 'create_mobile_layouts'],
						SRC: 'crop_portrait',
						DESCRIPTION: 'DESCRIPTION_OF_MOBILE_CREATE_LAYOUT',
					},
					{
						NAME: 'MOBILE_EDIT_LAYOUT',
						PATH: ['', 'modules', this.moduleId, 'edit_mobile_layouts'],
						SRC: 'table_chart',
						DESCRIPTION: 'DESCRIPTION_OF_MOBILE_EDIT_LAYOUT',
					},
					// {
					// 	NAME: 'MOBILE_DETAIL_LAYOUT',
					// 	PATH: ['', 'modules', this.moduleId, 'detail_mobile_layouts'],
					// 	SRC: 'view_compact',
					// 	DESCRIPTION: 'DESCRIPTION_OF_MOBILE_DETAIL_LAYOUT',
					// },
				];

				// adding the layout to list according to module
				let i = 0;
				this.navigationOfLayout.forEach((element) => {
					if (this.module.NAME === 'Chats') {
						console.log('element  ', element);
						if (
							element.NAME === 'MOBILE_LIST_LAYOUT' ||
							element.NAME === 'WEB_LIST_LAYOUT' ||
							element.NAME === 'WEB_EDIT_LAYOUT'
						) {
							this.matCardFirstRow.push(element);
							i++;
						}
					} else if (this.module.NAME === 'Tickets') {
						if (i < 3 && element.NAME !== 'WEB_DETAIL_LAYOUT') {
							this.matCardFirstRow.push(element);
							i++;
						} else if (element.NAME !== 'WEB_DETAIL_LAYOUT') {
							this.matCardSecondRow.push(element);
							i++;
						}
					} else {
						if (i < 3) {
							this.matCardFirstRow.push(element);
							i++;
						} else {
							this.matCardSecondRow.push(element);
							i++;
						}
					}
				});
			},

			(error) => console.log(error)
		);
	}

	// navigate to the layout
	public clicked(matCard) {
		this.router.navigate([
			`${matCard.PATH[1]}`,
			`${matCard.PATH[2]}`,
			`${matCard.PATH[3]}`,
		]);
	}
}
