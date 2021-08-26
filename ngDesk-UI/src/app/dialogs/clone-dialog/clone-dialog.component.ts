import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-clone-dialog',
  templateUrl: './clone-dialog.component.html',
  styleUrls: ['./clone-dialog.component.scss'],
})
export class CloneDialogComponent {
  public dialogs: any;
  constructor(
  @Inject(MAT_DIALOG_DATA) public data: any,
	public dialogRef: MatDialogRef<CloneDialogComponent>
  ) {
  }

  public onNoClick(): void {
	this.dialogRef.close('cancel');
  }
}