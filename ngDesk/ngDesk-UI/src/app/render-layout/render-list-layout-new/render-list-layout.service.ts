import { Injectable, ViewChild } from '@angular/core';
import { ModulesService } from '@src/app/modules/modules.service';
import { CustomTableService } from '@src/app/custom-table/custom-table.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { UsersService } from '@src/app/users/users.service';

@Injectable({
	providedIn: 'root',
})
export class RenderListLayoutService {
	// @ViewChild(MatSort, { static: true }) private sort: MatSort;
	// @ViewChild(MatPaginator, { static: true }) private paginator: MatPaginator;
	constructor(
		private modulesService: ModulesService,
		private customTableService: CustomTableService,
		private http: HttpClient,
		private globals: AppGlobals,
		private usersService: UsersService
	) { }
	public getListLayoutId(module) {
		const moduleId = module['MODULE_ID'];
		const currentRole = this.usersService.user.ROLE;
		const layoutInLocalStorage = JSON.parse(
			localStorage.getItem(`${moduleId}_LIST_LAYOUT`)
		);
		if (layoutInLocalStorage && layoutInLocalStorage !== null) {
			const layoutId = layoutInLocalStorage['LAYOUT_ID'];
			const listLayout = module.LIST_LAYOUTS.find(
				(layout) => (layout.LAYOUT_ID === layoutId && layout.ROLE === currentRole)
			);
			if (listLayout && listLayout !== null) {
				return listLayout.LAYOUT_ID;
			}
		} else {
			const listLayout = module.LIST_LAYOUTS.find(
				(layout) => (layout.IS_DEFAULT && layout.ROLE === currentRole)
			);
			if (listLayout && listLayout !== null) {
				return listLayout.LAYOUT_ID;
			}
		}
	}

	public getPageSize(module) {
		const moduleId = module['MODULE_ID'];
		const layoutInLocalStorage = JSON.parse(
			localStorage.getItem(`${moduleId}_LIST_LAYOUT`)
		);
		if (layoutInLocalStorage && layoutInLocalStorage !== null) {
			return layoutInLocalStorage['PAGE_SIZE'];
		} else {
			return 20;
		}
	}

	public getPageNumber(module) {
		const moduleId = module['MODULE_ID'];
		const layoutInLocalStorage = JSON.parse(
			localStorage.getItem(`${moduleId}_LIST_LAYOUT`)
		);
		if (layoutInLocalStorage && layoutInLocalStorage !== null) {
			return layoutInLocalStorage['PAGE'];
		} else {
			return 0;
		}
	}

	public getSearchQuey(module) {
		const moduleId = module['MODULE_ID'];
		if (typeof window !== 'undefined') {
		const layoutInLocalStorage = JSON.parse(
			localStorage.getItem(`${moduleId}_LIST_LAYOUT`)
		);
		if (layoutInLocalStorage && layoutInLocalStorage !== null) {
			return layoutInLocalStorage['SEARCH'];
		} else {
			return '';
		}	
	}
		 {
			return '';
		}
	}

	public getSortBy(module) {
		const moduleId = module['MODULE_ID'];
		const layoutInLocalStorage = JSON.parse(
			localStorage.getItem(`${moduleId}_LIST_LAYOUT`)
		);
		if (layoutInLocalStorage && layoutInLocalStorage !== null) {
			const layoutId = layoutInLocalStorage['LAYOUT_ID'];
			const listLayout = module.LIST_LAYOUTS.find(
				(layout) => layout.LAYOUT_ID === layoutId
			);
			if (listLayout && listLayout !== null) {
				const field = module['FIELDS'].find(
					(field) => field.FIELD_ID === listLayout['ORDER_BY']['COLUMN']
				);
				if (field) {
					return field['NAME'];
				}
			}
		} else {
			const listLayout = module.LIST_LAYOUTS.find(
				(layout) => layout.IS_DEFAULT
			);
			if (listLayout && listLayout !== null) {
				const field = module['FIELDS'].find(
					(field) => field.FIELD_ID === listLayout['ORDER_BY']['COLUMN']
				);
				if (field) {
					return field['NAME'];
				}
			}
		}
	}

	public getOrderBy(module) {
		const moduleId = module['MODULE_ID'];
		const layoutInLocalStorage = JSON.parse(
			localStorage.getItem(`${moduleId}_LIST_LAYOUT`)
		);
		if (layoutInLocalStorage && layoutInLocalStorage !== null) {
			const layoutId = layoutInLocalStorage['LAYOUT_ID'];
			const listLayout = module.LIST_LAYOUTS.find(
				(layout) => layout.LAYOUT_ID === layoutId
			);
			if (listLayout && listLayout !== null) {
				return listLayout['ORDER_BY']['ORDER'].toLowerCase();
			}
		} else {
			const listLayout = module.LIST_LAYOUTS.find(
				(layout) => layout.IS_DEFAULT
			);
			if (listLayout && listLayout !== null) {
				return listLayout['ORDER_BY']['ORDER'].toLowerCase();
			}
		}
	}

	public convertSearchString(searchParams?: any[]): string | null {
		let searchString: string = '';
		// reloading table based on params from search
		if (searchParams && searchParams.length > 0) {
			// build search string for either global search or field search
			searchParams.forEach((param, index) => {
				if (param['TYPE'] === 'field' && searchParams[index + 1]) {
					const field = param['NAME'];
					let value = searchParams[index + 1]['VALUE'];

					if (
						searchParams[index + 1]['DATA_ID'] &&
						searchParams[index + 1]['DATA_ID'] !== null
					) {
						value = searchParams[index + 1]['DATA_ID'];
					}

					if (searchString === '') {
						searchString = `${field}=${value}`;
					} else {
						searchString += `~~${field}=${value}`;
					}
				} else if (param['TYPE'] === 'global') {
					searchString = param['VALUE'];
				}
			});
			// make get entries call with search param
			if (searchString !== '') {
				return searchString;
			} else {
				return null;
			}
		} else {
			// when all params are cleared, return all entries
			return null;
		}
	}

	public getListLayoutEntries(query: string) {
		return this.http.post(
			`${this.globals.graphqlUrl}`,
			query
		);
	}
}
