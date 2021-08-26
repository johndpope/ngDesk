import { Component, Inject, OnInit, Optional } from '@angular/core';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
	selector: 'app-approval-reject-information-dialog',
	templateUrl: './approval-reject-information-dialog.component.html',
	styleUrls: ['./approval-reject-information-dialog.component.scss'],
})
export class ApprovalRejectInformationDialogComponent implements OnInit {
	public deniedBy = [];
	constructor(
		public dialogRef: MatDialogRef<ApprovalRejectInformationDialogComponent>,
		@Optional() @Inject(MAT_DIALOG_DATA) public modalData: any
	) {}

	public ngOnInit() {
		this.deniedBy = this.modalData.deniedBy;
	}

	public close() {
			this.dialogRef.close();
	}
}
