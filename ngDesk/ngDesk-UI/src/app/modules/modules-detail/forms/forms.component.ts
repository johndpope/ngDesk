import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ModulesService } from '../../modules.service';

@Component({
	selector: 'app-forms',
	templateUrl: './forms.component.html',
	styleUrls: ['./forms.component.scss'],
})
export class FormsComponent implements OnInit {
	private moduleId: String;
	public module: any;
	public isLoading = true;
	public navigations: any[];
	public showSideNav = true;

	constructor(
		public route: ActivatedRoute,
		public modulesService: ModulesService,
		private router: Router
	) {}

	ngOnInit(): void {
		this.moduleId = this.route.snapshot.params['moduleId'];
		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.module = response;
				this.isLoading = false;
				console.log(response);

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
						NAME: 'PDF',
						PATH: ['', 'modules', this.moduleId, 'pdf'],
					},
					{
						NAME: 'TASK',
						PATH: ['', 'modules', this.moduleId, 'task'],
					},
				];
			},
			(error: any) => {
				console.log(error.error.ERROR);
			}
		);
	}

	public navigate(navigation: String): void {
		if (navigation === 'forms') {
			this.router.navigate(['', 'modules', this.moduleId, 'forms', 'external']);
		} else {
			this.router.navigate(['', 'modules', this.moduleId, navigation]);
		}
	}
}
