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
export class NavigateToSchedulesService {
	private allModules: any = [];
    public navigateToSchedules : boolean;

	constructor(
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService,
		private usersService: UsersService,
	) {}

	public ngOnInit() {	
    
    }
    
}
