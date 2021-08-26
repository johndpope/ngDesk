import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RenderListLayoutNewComponent } from './render-list-layout-new/render-list-layout-new.component';
import { RenderDetailNewComponent } from '@src/app/render-layout/render-detail-new/render-detail-new.component';
//import { MobileRenderListLayoutComponent } from './mobile-render-list-layout/mobile-render-list-layout.component';
import { EventsComponent } from '@src/app/render-layout/events/events.component';
import { CatalogueDetailComponent } from './catalogue-detail/catalogue-detail.component';
import { CatalogueListComponent } from './catalogue-list/catalogue-list.component';

const routes: Routes = [
	{path: 'render/catalgoue', component:CatalogueListComponent},
	{path: 'render/catalogue/:catalogueId', component:CatalogueDetailComponent},
	{ path: ':moduleId', component: RenderListLayoutNewComponent},
	{ path: ':moduleId/:type/:dataId', component: RenderDetailNewComponent },
	{ path: 'render/:moduleId/:type/:dataId/events', component: EventsComponent },
	// { path: ':moduleId/:layoutId' ,component: MobileRenderListLayoutComponent}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
	schemas: [NO_ERRORS_SCHEMA],
})
export class RenderLayoutRoutingModule {}
