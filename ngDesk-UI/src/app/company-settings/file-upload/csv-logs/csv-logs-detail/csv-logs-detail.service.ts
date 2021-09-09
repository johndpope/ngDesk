import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class CsvLogsService {
	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private cacheService: CacheService,
		private translateService: TranslateService
	) {}

	// Get all call in order to fetch all csv-Imports.
	public getAllCsvImports(page, pageSize, sortBy, orderBy): Observable<any> {
		const query = `{
			DATA: getCsvImports(pageNumber: ${page}, pageSize: ${pageSize}, sortBy: "${sortBy}", orderBy: "${orderBy}") {
				status
				csvImportId
				name
				dateCreated
			  }
			TOTAL_RECORDS: getCsvImportsCount
		}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}

	// Get individual entry based on id.
	public getCsvImport(id): Observable<any> {
		const query = ` {
			DATA: getCsvImport(csvImportId: "${id}") {
                status
                csvImportData{
                    file
                    fileType
                    fileName
                    headers{
                        fieldId
                        headerName
                    }
                }
                moduleId
				logs {
					lineNumber
					errorMessage
				}
                companyId
                name
                dateCreated
				createdBy {
					DATA_ID: _id
					CONTACT {
					  PRIMARY_DISPLAY_FIELD: FULL_NAME
					  DATA_ID: _id
					}
				  }
				}
   			}`;
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
