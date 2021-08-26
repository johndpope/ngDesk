import { Injectable } from '@angular/core';
import { CacheService } from '@src/app/cache.service';
import { DataApiService } from '@ngdesk/data-api';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap,map, debounce } from 'rxjs/operators';
import { GuideService } from '../guide.service';

@Injectable({
  providedIn: 'root'
})
export class ArticlesDataService {

  public authorSubjects = new Subject<any>();
  public authorStore:any = [];

  public teamsSubject = new Subject<any>();    // for visibility access
  public teamStore:any = [];
  constructor(
    private cacheService: CacheService,
    private dataService: DataApiService,
    private guideService :GuideService
  ) { }



  // To get Authors list onload())
  //To get Authors onScroll()
  // To get Authors onSearch()

  // TODO: Search is hard coded based onemail id Need to change this 

  public initializeAuthors() {
		this.authorSubjects
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value,search])=>{
         const moduleId = this.cacheService.moduleNamesToIds['Users'];
					let searchValue = null;
					if (value !== '') {
						searchValue = 'EMAIL_ADDRESS' + '=' + value;
					}
					let page = 0;
					if (this.authorStore && !search) {
						page = Math.ceil(this.authorStore.length / 10);
					}
					return this.dataService
						.getAllData(moduleId, searchValue, page, 10, [
							'EMAIL_ADDRESS',
							'Asc',
          	])
						.pipe(
							map((results: any) => {
								if (search) {
									this.authorStore = results.content;
								} else if (results.content.length > 0) {
                  this.authorStore = this.authorStore.concat(results.content);
								}
								return results.content;
							})
						);
				})
			).subscribe();

  }
  

  public intitalizeVisibleTo() {
    this.teamsSubject
    .pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(([value,search])=>{
        const moduleId = this.cacheService.moduleNamesToIds['Teams'];
        let searchValue = null;
        if (value !== '') {
          searchValue = 'NAME' + '=' + value;
        }
        let page = 0;
        if (this.teamStore && !search) {
          page = Math.ceil(this.teamStore.length / 10);
        }
        return this.dataService
        .getAllData(moduleId, searchValue, page, 10, [
          'NAME',
          'Asc',
        ])
        .pipe(
          map((results: any) => {
            if (search) {
              this.teamStore = results.content;
            } else if (results.content.length > 0) {
              this.teamStore = this.teamStore.concat(results.content);
            }
            return results.content;
          })
        );
      })
    ).subscribe();
  }
  
}
