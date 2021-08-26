import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class SortedListLayoutEntryService {
    public sortedEntries = [];
	constructor(
		private http: HttpClient,
		private globals: AppGlobals
    ) { }

}
