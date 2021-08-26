import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
// import { ModulesService } from '@src/app/modules/modules.service';
// import { ActivatedRoute, Router } from '@angular/router';
// import { CustomTableService } from '@src/app/custom-table/custom-table.service';

@Component({
  selector: 'app-mobile-render-list-layout',
  templateUrl: './mobile-render-list-layout.component.html',
  styleUrls: ['./mobile-render-list-layout.component.css']
})
export class MobileRenderListLayoutComponent implements OnInit {


  public form:FormGroup;
  // public moduleId: string;
  // public layoutId:string;
  // public entryData:any =[];
  // public isloadingData=true;
  // public mobileTitle:string=' ';
  // public recordName: string;
  // public moduleResponse:any={};
  // public displayTitle:string='';
  // public displaySubTitle:string='';
  // public displayValue:string;

  constructor(
    // private modulesService:ModulesService,
    // private route: ActivatedRoute,
    // private customTableService: CustomTableService,
    private router:Router,
    private formBuilder:FormBuilder
  ) { }

  ngOnInit() {
    this.form = this.formBuilder.group({
      name:['',Validators.required]
    })
    // this.moduleId = this.route.snapshot.params.moduleId;
    // this.layoutId = this.route.snapshot.params.layoutId;
    // this.getListLayout();
  }

  public click(){
    console.log("hello");
    console.log(this.form.value.name);
    this.router.navigate(['/login']);
  }
  // public getListLayout(){
  //   this.modulesService.getModuleById(this.moduleId).subscribe((response :any)=>{
  //     this.moduleResponse=response;
  //     this.mobileTitle=response.NAME;
  //     this.recordName= response.PLURAL_NAME.toLowerCase();
  
  //    let listlayoutFields: any={};
  //    listlayoutFields = response.LIST_MOBILE_LAYOUTS.filter(L=>L.LAYOUT_ID === this.layoutId);
  //     let titleField= this.moduleResponse.FIELDS.filter(F => F.FIELD_ID ===listlayoutFields[0].FIELDS[0]);
  //    let subtitleField = this.moduleResponse.FIELDS.filter(F => F.FIELD_ID ===listlayoutFields[0].FIELDS[1]);

  //    this.displayTitle = titleField[0].NAME;
  //     if(listlayoutFields[0].FIELDS.length === 1){
  //       this.displaySubTitle ='';
  //     }else {
  //       this.displaySubTitle = subtitleField[0].NAME;
  //     }
  //     this.getEntries();
      
  //   });

  // }

  // public getEntries(){
  
	// 	const sortBy = this.customTableService.sortBy;
	// 	const orderBy = this.customTableService.sortOrder;
	// 	let page = this.customTableService.pageIndex;
	// 	const pageSize = this.customTableService.pageSize;
		
	// 	this.modulesService
	// 	.getEntriesByLayoutId(
	// 		this.moduleId,
	// 		this.layoutId,
	// 		sortBy,
	// 		orderBy,
	// 		page + 1,
	// 		pageSize
	// 	)
	// 	.subscribe(
	// 		(entriesResponse: any) => {
  //       this.entryData = entriesResponse.DATA;
	// 			this.isloadingData=false;
		
  //     });
  // }

  // public onRecordClick(entry){
  //   this.router.navigate([`render/${this.moduleId}/detail/${entry.DATA_ID}`])
  // }

}

