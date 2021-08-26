import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AppGlobals } from '../app.globals';

@Injectable({
	providedIn: 'root',
})
export class AttachmentsService {
	constructor(private http: HttpClient, private globals: AppGlobals) {}

	// POST call for attachments
	public postAttachments(attachments) {
		return this.http.post(
			`${this.globals.baseRestUrl}/attachments`,
			attachments
		);
	}
}
