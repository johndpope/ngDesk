import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss'],
})
export class ConfirmDialogComponent {
  public dialogs: any;
  constructor(
  @Inject(MAT_DIALOG_DATA) public data: any,
	public dialogRef: MatDialogRef<ConfirmDialogComponent>
  ) {
  }

  public onNoClick(): void {
	this.dialogRef.close('cancel');
  }
}
