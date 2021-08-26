import { Component, Inject, OnInit, Optional } from '@angular/core';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { UsersService } from '@src/app/users/users.service';

@Component({
	selector: 'app-approval-reject-dialog',
	templateUrl: './approval-reject-dialog.component.html',
	styleUrls: ['./approval-reject-dialog.component.scss'],
})
export class ApprovalRejectDialogComponent implements OnInit {
	public comment = '';
	constructor(public dialogRef: MatDialogRef<ApprovalRejectDialogComponent>) {}

	public ngOnInit() {}

	public setComment() {
		if (this.comment !== '') {
			this.dialogRef.close(this.comment);
		}
	}
}
