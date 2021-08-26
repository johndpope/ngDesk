import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UsersService } from '@src/app/users/users.service';
import { CatalogueServiceService } from './catalogue-service.service';
@Component({
	selector: 'app-catalogue-list',
	templateUrl: './catalogue-list.component.html',
	styleUrls: ['./catalogue-list.component.scss'],
})
export class CatalogueListComponent implements OnInit {
	public catalogues: any[] = [];
	public totalCount = 0;
	public roleName: string;
	public subdomain: string;

	constructor(
		private catalogueService: CatalogueServiceService,
		private router: Router,
		private usersService: UsersService
	) {}

	ngOnInit(): void {
		this.subdomain = this.usersService.getSubdomain();
		this.catalogueService
			.getAllCatalogues(0, 10, 'name', 'Asc')
			.subscribe((value) => {
				this.roleName = value[0].NAME;
				this.catalogues = value[1].DATA;
				this.totalCount = value[1].TOTAL_RECORDS;
			});
	}

	public numberOfIterations(): any[] {
		if (this.catalogues.length > 0) {
			return new Array(Math.ceil(this.catalogues.length / 5));
		}
		return [];
	}

	public loadMore() {
		const pageNumber = Math.floor(this.catalogues.length / 10);
		this.catalogueService
			.getAllCatalogues(pageNumber, 10, 'name', 'Asc')
			.subscribe((value) => {
				if (value && value !== null) {
					this.catalogues = this.catalogues.concat(value[1].DATA);
					this.totalCount = value[1].TOTAL_RECORDS;
				}
			});
	}

	public getCatalogues(index): any[] {
		if (this.catalogues.length > 0) {
			if (index === 0) {
				return this.catalogues.slice(index, 5);
			} else {
				return this.catalogues.slice(index * 5, index * 5 + 5);
			}
		}
		return [];
	}

	public navigate(catalogueId) {
		this.router.navigate(['render', 'catalogue', catalogueId]);
	}

	public navigateToCompanySettings() {
		this.router.navigate(['company-settings', 'catalogues']);
	}
}
