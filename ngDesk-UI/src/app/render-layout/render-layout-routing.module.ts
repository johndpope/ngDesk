import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EventsComponent } from '@src/app/render-layout/events/events.component';
import { RenderDetailNewComponent } from '@src/app/render-layout/render-detail-new/render-detail-new.component';
import { RenderListLayoutNewComponent } from '@src/app/render-layout/render-list-layout-new/render-list-layout-new.component';
import { RenderFormsComponent } from './render-forms/render-forms.component';
import { CatalogueDetailComponent } from './catalogue-detail/catalogue-detail.component';
import { CatalogueListComponent } from './catalogue-list/catalogue-list.component';

const routes: Routes = [
	{ path: 'catalogue', component: CatalogueListComponent },
	{ path: 'catalogue/:catalogueId', component: CatalogueDetailComponent },
	{ path: ':moduleId', component: RenderListLayoutNewComponent },
	{ path: ':moduleId/:type/:dataId', component: RenderDetailNewComponent },
	{ path: 'render/:moduleId/:type/:dataId/events', component: EventsComponent },
	{
		path: ':catalogueId/:moduleId/forms/:formId',
		component: RenderFormsComponent,
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
})
export class RenderLayoutRoutingModule {}
