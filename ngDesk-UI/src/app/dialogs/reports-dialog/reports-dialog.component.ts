
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { ModulesService } from '../../modules/modules.service';

@Component({
  selector: 'app-reports-dialog',
  templateUrl: './reports-dialog.component.html',
  styleUrls: ['./reports-dialog.component.scss']
})
export class ReportsDialogComponent implements OnInit {
  // TODO: set model for modules
  private modules: any;
  public filteredOptions: Observable<any>;
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<ReportsDialogComponent>,
    private modulesService: ModulesService,
  ) { }

  public ngOnInit() {
    this.modulesService.getModules().subscribe(
      (moduleResponse: any) => {
        this.modules = moduleResponse.MODULES.sort((a, b) =>
        a.NAME.localeCompare(b.NAME)
      );
        this.filteredOptions = this.data.reportForm.controls.MODULE.valueChanges
          .pipe(
            startWith(''),
            map(value => this.filterModules(value))
          );
      },
      (error: any) => {
        console.log(error.error.ERROR);
      }
    );
  }

  private filterModules(value) {
    const filteredModules = [];
    let filterValue = value;
    if (typeof value === 'string') {
      filterValue = value.toLowerCase();
    }
    this.modules.forEach(module => {
      if (module.NAME.toLowerCase().includes(filterValue)) {
        filteredModules.push(module);
      }
    });
    return filteredModules;
  }
}
