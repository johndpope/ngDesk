
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';
import { CacheService } from '@src/app/cache.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { forkJoin, Observable } from 'rxjs';
import { map, mergeMap } from 'rxjs/operators';

@Injectable({
	providedIn: 'root',
})
export class EventsService {

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
    private modulesService: ModulesService,
    private cacheService : CacheService
	) {}

	// Calling graphql call to fetch a metaData
  public getRequiredData(moduleId,entryId): Observable<any[]> {
    const moduleResponse = this.cacheService.getModule(moduleId);
    const entryResponse = this.modulesService.getModuleById(moduleId).pipe(
     map((allModuleResponse) => {
       const moduleName = JSON.parse(JSON.stringify(allModuleResponse))
         .NAME;
       return moduleName;
     }),
     mergeMap((moduleName) => {
       const query = `{
        entry: get${moduleName.replace(/ /g, '_')}Entry(id: "${entryId}") 
        {
          META_DATA {
                EVENTS {
                  MESSAGE
                  DATE_CREATED
                  MESSAGE_ID
                  SENDER {
                    FIRST_NAME
                    LAST_NAME
                    ROLE {
                      roleId
                    }
                    USER_UUID
                  }
                  MESSAGE_TYPE
                  ATTACHMENTS {
                    FILE_NAME
                    FILE_EXTENSION
                    HASH
                    ATTACHMENT_UUID
                  }
                }
            }
          }
        }`;
       return this.http.post(`${this.globals.graphqlUrl}`, query);
     })
   );
  return forkJoin([moduleResponse,entryResponse]);
  
  }

}

