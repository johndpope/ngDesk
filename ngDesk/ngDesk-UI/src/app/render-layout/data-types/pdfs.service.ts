import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class PdfsService {
	constructor() {}

	public getPdfs(field) {
		return `<div fxLayout="row" fxLayoutGap="10px" class="pointer">
        <div *ngFor="let entry of context.entry['${field.NAME}']" fxLayoutAlign="start center" [ngStyle]="{'color': '#1f73b7', 'border-radius': '5px'}">
            <a 
            [ngStyle]="{'color': '#1f73b7', 'text-decoration': 'none'}"
            class="mat-body" 
            fxLayout="row"
            fxLayoutAlign="center center" 
            [attr.href]="context.downloadAttachmentForPdfs(entry.ATTACHMENT_UUID, '${field.FIELD_ID}', '${field.DATA_TYPE.DISPLAY}')"
            target="blank"
            download="entry.FILE_NAME" >
                <mat-icon>attach_file</mat-icon>
                    {{entry.FILE_NAME}}                
            </a>
        </div>
    </div>
`;
	}
}
