import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ModulesService } from './modules/modules.service';
import { UsersService } from './users/users.service';

@Injectable({
	providedIn: 'root',
})
export class GraphqlListLayoutService {
	private allModules: any = [];

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService,
		private usersService: UsersService,
	) {}

	public ngOnInit() {	
		 this.modulesService.getModules().pipe(
			map((modules) => {
				this.allModules = modules['MODULES'];
			})
		 );
	}

	public getListLayoutData(moduleId,module,listLayoutId, page, pageSize, sortBy, orderBy): Observable<any> {
		let defaultListLayout;
		let fieldsToShow;
		const moduleName = module.NAME.replace(/\s+/g, '_');
		defaultListLayout = module.LIST_LAYOUTS.find(
			(layout) =>
				layout.ROLE === this.usersService.user.ROLE && layout.IS_DEFAULT
		);
		if (typeof window !== 'undefined') {
			fieldsToShow = defaultListLayout.COLUMN_SHOW.FIELDS;
		} else {
			fieldsToShow = defaultListLayout.FIELDS;
		}
		let fieldsQuery = 'DATA_ID: _id' + '\n';
		fieldsToShow.forEach((fieldId) => {
			const moduleField = module.FIELDS.find(
				(field) => field.FIELD_ID === fieldId
			);
			if (moduleField.DATA_TYPE.DISPLAY === 'Relationship') {
				const relatedModule = this.allModules.filter(
					(module) => module['MODULE_ID'] === moduleField.MODULE
				);
				if (relatedModule[0]) {
					const primaryDisplayField = relatedModule[0].FIELDS.filter(
						(field) => field['FIELD_ID'] === moduleField.PRIMARY_DISPLAY_FIELD
					);
					if (primaryDisplayField[0]) {
						const relationshipQuery = `${moduleField.NAME} {
							DATA_ID: _id
							PRIMARY_DISPLAY_FIELD: ${primaryDisplayField[0].NAME}
						}`;
						fieldsQuery += relationshipQuery + '\n';
					}
				}
			} else if (moduleField.DATA_TYPE.DISPLAY === 'Phone') {
				fieldsQuery +=
					`PHONE_NUMBER{
					COUNTRY_CODE 
					DIAL_CODE
					PHONE_NUMBER
					COUNTRY_FLAG
				}` + '\n';
			} else {
				fieldsQuery += moduleField.NAME + '\n';
			}
		});

		const query = `{
			DATA: get${moduleName} (moduleId: "${moduleId}", layoutId: "${listLayoutId}", pageNumber: ${page}, 
			pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				${fieldsQuery}
			}
			TOTAL_RECORDS: count(moduleId: "${moduleId}", layoutId: "${listLayoutId}")
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
    }
    
}
