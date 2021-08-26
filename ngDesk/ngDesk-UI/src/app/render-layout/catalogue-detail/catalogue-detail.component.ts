import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CatalogueServiceService } from '../catalogue-list/catalogue-service.service';

@Component({
	selector: 'app-catalogue-detail',
	templateUrl: './catalogue-detail.component.html',
	styleUrls: ['./catalogue-detail.component.scss'],
})
export class CatalogueDetailComponent implements OnInit {
	public catalogue: any = {};
	public forms: any[] = [];
	private catalogueId;

	constructor(
		private catalogueService: CatalogueServiceService,
		private route: ActivatedRoute,
		private router: Router
	) {}

	ngOnInit(): void {
		this.catalogue.name = '';
		this.catalogueId = this.route.snapshot.paramMap.get('catalogueId');
		this.catalogueService.getCatalogue(this.catalogueId).subscribe((value) => {
			console.log(value);
			this.catalogue = value.DATA;
			this.forms = this.catalogue.catalogueForms;
		});
	}

	public numberOfIterations(): any[] {
		if (this.forms.length > 0) {
			return new Array(Math.ceil(this.forms.length / 5));
		}
		return [];
	}

	public getForms(index): any[] {
		this.forms = this.forms.filter((form) => form.formId !== null);
		if (this.forms.length > 0) {
			if (index === 0) {
				return this.forms.slice(index, 5);
			} else {
				return this.forms.slice(index * 5, index * 5 + 5);
			}
		}
		return [];
	}

	public navigate(formId, moduleId) {
		this.router.navigate([
			'render',
			this.catalogueId,
			moduleId,
			'forms',
			formId,
		]);
	}
}
