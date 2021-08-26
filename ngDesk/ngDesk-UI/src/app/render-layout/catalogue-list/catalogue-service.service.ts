import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { RolesService } from '@src/app/roles/roles.service';
import { UsersService } from '@src/app/users/users.service';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
@Injectable({
	providedIn: 'root',
})
export class CatalogueServiceService {
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private rolesService: RolesService,
		private usersService: UsersService
	) {}

	public getAllCatalogues(page, pageSize, sortBy, orderBy): Observable<any[]> {
		const roleResponse = this.rolesService.getRole(this.usersService.user.ROLE);
		const query = `{
			DATA: getCatalogues(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				name
				description
        		catalogueId
        		displayImage
			}
			TOTAL_RECORDS: getCatalogueCount
		}`;
		const cataloguesResponse = this.http
			.post(`${this.globals.graphqlUrl}`, query)
			.pipe(
				map((cataloguesResponse) => {
					return cataloguesResponse;
				})
			);

		return forkJoin([roleResponse, cataloguesResponse]);
	}

	public getCatalogue(catalogueId): Observable<any> {
		const query = `{
			DATA: getCatalogue(catalogueId:"${catalogueId}") {
				
            name,
        	description,
        	  catalogueForms {
              moduleId,
              formId{
				        formId
                name
                description
                displayImage
			  }
				    },
        	}
   		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
