import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AppGlobals } from '../../app.globals';

@Injectable({
  providedIn: 'root'
})
export class ImportService {
  constructor(private http: HttpClient, private globals: AppGlobals) {}
  public postImportZendesk(zendeskUser, tickets, attachments) {
    return this.http.post(
      `${
        this.globals.baseRestUrl
      }/migration/zendesk?tickets=${tickets}&attachments=${attachments}`,
      zendeskUser
    );
  }
}
