import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-loading-dialog',
  templateUrl: './loading-dialog.component.html',
  styleUrls: ['./loading-dialog.component.scss']
})
export class LoadingDialogComponent implements OnInit {
  private timer;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any, public dialogRef: MatDialogRef<LoadingDialogComponent>) { }

  public ngOnInit() {
    this.startTimer(this.data.loadingTimer);
  }

  private startTimer(timeLeft) {
    this.timer = setInterval(() => {
      if (timeLeft > 0) {
        timeLeft--;
      } else {
        clearInterval(this.timer);
        this.dialogRef.close('cancel');
      }
    }, 1000);
  }

  public onNoClick(): void {
    this.dialogRef.close('cancel');
  }
}
