import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-unsubscribe-dialog',
  templateUrl: './unsubscribe-dialog.component.html',
  styleUrls: ['./unsubscribe-dialog.component.scss'],
})
export class UnsubscribeDialogComponent {
  public dialogs: any;
  constructor(
  @Inject(MAT_DIALOG_DATA) public data: any,
	public dialogRef: MatDialogRef<UnsubscribeDialogComponent>
  ) {
  }

  public onNoClick(): void {
	this.dialogRef.close('cancel');
  }
}