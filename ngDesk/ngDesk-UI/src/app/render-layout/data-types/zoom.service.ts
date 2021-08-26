import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ZoomService {
	constructor() { }

	public getZoom(field) {
	return `
    <div fxFlex fxLayout="row">
    <div fxFlex [ngStyle]="{'margin-bottom':'20px'}">
	<button fxFlex matTooltip="{{'CREATE_MEETING' | translate}}" *ngIf="!context.startZoomMeeting;else elseBlock"  mat-raised-button color="primary" 
    [disabled]="!context.editAccess || !context.zoomIntegrated || isClickedOnce || context.customModulesService.fieldsDisableMap['${field.FIELD_ID}']" (click)="context.createZoomMeeting();isClickedOnce = true">
    ${field.DISPLAY_LABEL}
    </button>
    <div *ngIf="!context.zoomIntegrated" fxLayoutAlign="end center">
	<mat-icon matTooltip="{{'START_ZOOM_HINT' | translate}}">info_outline</mat-icon>
	</div>
	<ng-template #elseBlock>
    <button fxFlex matTooltip="{{'START_MEETING_HINT' | translate}}" mat-raised-button color="primary" (click)="context.startMeetingButton()">Start Zoom Meeting 
    </button>
    </ng-template>
    </div>
    </div>`;
}
}