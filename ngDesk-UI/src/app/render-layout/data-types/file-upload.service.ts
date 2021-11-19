import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class FileUploadService {
	constructor() {}

	public getFileUpload(field, layoutType) {
		const attachFiles = `<div style ="padding-right:20px; padding-bottom:20px;" fxFlex="100" fxLayoutGap="10px" fxLayout="row">
    <div fxLayoutAlign="center center">
    <mat-icon *ngIf="context.helpTextMap.get('${field.FIELD_ID}')" style="padding-right:8px"  
    class="color-primary"  matTooltip="${field.HELP_TEXT}">help_outline</mat-icon>
      <button mat-raised-button fxLayoutAlign="center center" (click)="fileInput.click()" style="cursor: pointer; height:48px;border-radius: 5px;">
        <div fxLayout="row" style="height:48px;">
          <div fxLayoutAlign="center center">
            <label class="mat-h4" style="margin: 0px;cursor: pointer; font-weight:500;">Attach files</label>
          </div>
          <div fxLayout="row" fxLayoutAlign="center center" fxLayoutGap="10px" [ngStyle]="{'font-size': '20px', 'font-weight':'500'}">
            <mat-spinner *ngIf="context.attachmentLoading" [diameter]="30"></mat-spinner>
            <mat-icon *ngIf="!context.attachmentLoading" inline class="pointer">
              attach_file</mat-icon><input hidden type="file" #fileInput (change)="context.onFileChangeForGeneral($event)">
          </div>
        </div>
      </button>
    </div>`;
		const downloadLink = ` <a 
    [ngStyle]="{'color': '#1f73b7', 'text-decoration': 'none'}"
    class="mat-body" 
    fxLayout="row"
    fxLayoutAlign="center center" 
    [attr.href]="context.downloadAttachmentForPdfs(entry.ATTACHMENT_UUID, '${field.FIELD_ID}', '${field.DATA_TYPE.DISPLAY}')"
    target="blank"
    download="entry.FILE_NAME" >
        <mat-icon>attach_file</mat-icon>
            {{entry.FILE_NAME}}                
    </a>`;
		if (layoutType === 'create') {
			return (
				attachFiles +
				` <div style="width: 100%;  display: flex; flex-flow: row wrap;" fxLayoutAlign="start center">
      <ng-container *ngFor="let attachment of context.generalAttachments; index as i">
        <mat-card fxLayoutAlign="center center" style="padding-top: 10px; padding-bottom: 10px; margin:10px">
          <label class="mat-body-strong">{{attachment.FILE_NAME}}</label>
          <mat-icon class="pointer" (click)="context.generalAttachments.splice(i,1)">close</mat-icon>
        </mat-card>
      </ng-container>
      </div> 
    <div *ngFor="let entry of context.entry['${field.NAME}']" fxLayoutAlign="start center" [ngStyle]="{'color': '#1f73b7', 'border-radius': '5px'}">
    <mat-card>` +
				downloadLink +
				` </mat-card>
      </div>
</div>`
			);
		} else {
			return (
				attachFiles +
				`<div style="width: 100%;  display: flex; flex-flow: row wrap;" fxLayoutAlign="start center">
        <div *ngFor="let entry of context.generalAttachments; index as i" fxLayoutAlign="start center" [ngStyle]="{'color': '#1f73b7', 'border-radius': '5px'}">
      <mat-card style="margin: 10px;">
      <div fxLayout="row" *ngIf="entry.ATTACHMENT_UUID">` +
				downloadLink +
				` <mat-icon class="pointer" (click)="context.generalAttachments.splice(i,1)">close</mat-icon>
            </div>
            <div fxLayout="row" *ngIf="!entry.ATTACHMENT_UUID" >
            {{entry.FILE_NAME}}                
            <mat-icon class="pointer" (click)="context.generalAttachments.splice(i,1)">close</mat-icon>
            </div>
            </mat-card>
        </div>
        </div>
  </div>`
			);
		}
	}

	public getImageUpload(field) {
		return `<div style ="padding-right:20px; padding-bottom:20px;" fxFlex="100" fxLayoutGap="10px" fxLayout="row">
    <div fxLayoutAlign="center center">
      <button mat-raised-button fxLayoutAlign="center center" (click)="fileInput.click()" style="cursor: pointer; height:48px;border-radius: 5px;">
        <div fxLayout="row" style="height:48px;">
          <div fxLayoutAlign="center center">
            <label class="mat-h4" style="margin: 0px;cursor: pointer; font-weight:500;">Attach Images</label>
          </div>
          <div fxLayout="row" fxLayoutAlign="center center" fxLayoutGap="10px" [ngStyle]="{'font-size': '20px', 'font-weight':'500'}">
            <mat-spinner *ngIf="context.attachmentLoading" [diameter]="30"></mat-spinner>
            <mat-icon *ngIf="!context.attachmentLoading" inline class="pointer">
              attach_file</mat-icon><input hidden type="file"  accept="image/*"  #fileInput name = "Image"(change)="context.onImageUpload($event)">
          </div>
        </div>
      </button>
    </div>
    <div fxLayoutAlign="center center">
      <ng-container *ngFor="let attachment of context.imageAttachments; index as i">
        <mat-card fxLayoutAlign="center center" style="padding-top: 10px; padding-bottom: 10px; margin-right:10px">
          <label class="mat-body-strong">{{attachment.FILE_NAME}}</label>
          <mat-icon class="pointer" (click)="context.imageAttachments.splice(i,1)">close</mat-icon>
        </mat-card>
      </ng-container>
      </div>
    </div>
`;
	}
}
