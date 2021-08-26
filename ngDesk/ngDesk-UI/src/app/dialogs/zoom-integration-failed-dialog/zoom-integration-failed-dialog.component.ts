import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';


@Component({
  selector: 'app-zoom-integration-failed-dialog',
  templateUrl: './zoom-integration-failed-dialog.component.html',
  styleUrls: ['./zoom-integration-failed-dialog.component.scss']
})
export class ZoomIntegrationFailedDialogComponent implements OnInit {

  constructor(    
    private router: Router,
    private dialog: MatDialog,
    ) { }

  public ngOnInit() {
  }
  public close(){
    this.router.navigate([`company-settings`]);
    this.dialog.closeAll();
  }

}
