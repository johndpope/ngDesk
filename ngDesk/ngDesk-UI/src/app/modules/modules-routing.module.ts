import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ChannelComponent } from './modules-detail/channels/channel.component';
import { ChannelsListComponent } from './modules-detail/channels/channels-list/channels-list.component';
import { ChatBotCreateComponent } from './modules-detail/channels/chat-bots/chat-bot-create/chat-bot-create.component';
import { ChatBotGettingStartedComponent } from './modules-detail/channels/chat-bots/chat-bot-getting-started/chat-bot-getting-started.component';
import { ChatBotsDetailComponent } from './modules-detail/channels/chat-bots/chat-bots-detail/chat-bots-detail.component';
import { ChatBotsMasterComponent } from './modules-detail/channels/chat-bots/chat-bots-master/chat-bots-master.component';
import { ChatChannelsMasterComponent } from './modules-detail/channels/chat-widgets/chat-channels-master/chat-channels-master.component';
import { ChatGettingStartedComponent } from './modules-detail/channels/chat-widgets/chat-getting-started/chat-getting-started.component';
import { ChatPromptMasterComponent } from './modules-detail/channels/chat-widgets/chat-prompt-master/chat-prompt-master.component';
import { EmailDetailComponent } from './modules-detail/channels/email-detail/email-detail.component';
import { FacebookDetailComponent } from './modules-detail/channels/facebook-detail/facebook-detail.component';
import { FieldCreatorComponent } from './modules-detail/fields/field-creator/field-creator.component';
import { FieldMasterComponent } from './modules-detail/fields/field-master/field-master.component';
import { FieldViewComponent } from './modules-detail/fields/field-view/field-view.component';
import { FormsDetailComponent } from './modules-detail/forms/forms-detail/forms-detail.component';
import { FormsMasterComponent } from './modules-detail/forms/forms-master/forms-master.component';
import { DetailLayoutComponent } from './modules-detail/layouts/detail-layout/detail-layout.component';
import { LayoutsMasterComponent } from './modules-detail/layouts/layouts-master/layouts-master.component';
import { LayoutsComponent } from './modules-detail/layouts/layouts.component';
import { ListLayoutComponent } from './modules-detail/layouts/list-layout/list-layout.component';
import { MobileDetailLayoutComponent } from './modules-detail/layouts/mobile-detail-layout/mobile-detail-layout.component';
import { MobileListLayoutComponent } from './modules-detail/layouts/mobile-list-layout/mobile-list-layout.component';
import { ModulesDetailComponent } from './modules-detail/modules-detail.component';
import { SlaDetailComponent } from './modules-detail/slas/sla-detail/sla-detail.component';
import { SlaMasterComponent } from './modules-detail/slas/sla-master/sla-master.component';
import { PdfDetailComponent } from './modules-detail/pdf/pdf-detail/pdf-detail.component';
import { PdfMasterComponent } from './modules-detail/pdf/pdf-master/pdf-master.component';
import { TriggersDetailComponent } from './modules-detail/triggers/triggers-detail/triggers-detail.component';
import { TriggersMasterComponent } from './modules-detail/triggers/triggers-master/triggers-master.component';
import { ValidationsDetailComponent } from './modules-detail/validations/validations-detail/validations-detail.component';
import { ValidationsMasterComponent } from './modules-detail/validations/validations-master/validations-master.component';
import { ModulesMasterComponent } from './modules-master/modules-master.component';
import { TriggersDetailNewComponent } from './modules-detail/triggers/triggers-detail-new/triggers-detail-new.component';
import { WorkflowCreateComponent } from './modules-detail/triggers/workflow-create/workflow-create.component';
import { TaskMasterComponent } from './modules-detail/task/task-master/task-master.component';
import { TaskDetailComponent } from './modules-detail/task/task-detail/task-detail.component';
import { FormsComponent } from './modules-detail/forms/forms.component';
import { ServiceCatalogueComponent } from './modules-detail/forms/service-catalogue/service-catalogue.component';
import { ServiceCatalogueDetailComponent } from './modules-detail/forms/service-catalogue-detail/service-catalogue-detail.component';

const routes: Routes = [
	{ path: '', component: ModulesMasterComponent },
	{ path: ':moduleId', component: ModulesDetailComponent },
	{
		path: ':moduleId/chatbots',
		component: ChatBotCreateComponent,
	},
	{
		path: ':moduleId/chatbots/create',
		component: ChatBotGettingStartedComponent,
	},
	{
		path: ':moduleId/chatbots/templates',
		component: ChatBotsMasterComponent,
	},

	{ path: ':moduleId/fields', component: FieldMasterComponent },
	{
		path: ':moduleId/field/field-creator/:dataType',
		component: FieldCreatorComponent,
	},
	{ path: ':moduleId/field/field-creator', component: FieldCreatorComponent },
	{ path: ':moduleId/field/:fieldId', component: FieldViewComponent },
	{ path: ':moduleId/channels', component: ChannelComponent },
	{ path: ':moduleId/layouts', component: LayoutsComponent },
	{ path: ':moduleId/workflows', component: TriggersMasterComponent },
	{ path: ':moduleId/slas', component: SlaMasterComponent },
	{ path: ':moduleId/pdf', component: PdfMasterComponent },
	{ path: ':moduleId/validations', component: ValidationsMasterComponent },

	{ path: ':moduleId/forms', component: FormsComponent },
	{ path: ':moduleId/forms/external', component: FormsMasterComponent },
	{ path: ':moduleId/service-catalogue', component: ServiceCatalogueComponent },
	{
		path: ':moduleId/service-catalogue/:serviceCatalogueId',
		component: ServiceCatalogueDetailComponent,
	},
	{ path: ':moduleId/forms/:formId', component: FormsDetailComponent },
	{ path: ':moduleId/task', component: TaskMasterComponent },
	{ path: ':moduleId/task/:taskId', component: TaskDetailComponent },
	{ path: ':moduleId/:layoutType', component: LayoutsMasterComponent },
	{
		path: ':moduleId/workflows/create-new',
		component: WorkflowCreateComponent,
	},
	{
		path: ':moduleId/workflows/:moduleWorkflowId',
		component: TriggersDetailNewComponent,
	},
	{
		path: ':moduleId/:layoutType/mobile',
		component: LayoutsMasterComponent,
	},
	{
		path: ':moduleId/list_layouts/:listLayoutId',
		component: ListLayoutComponent,
	},
	{
		path: ':moduleId/list_mobile_layouts/:listMobileLayoutId',
		component: MobileListLayoutComponent,
	},
	{ path: ':moduleId/slas/:slaId', component: SlaDetailComponent },
	{ path: ':moduleId/pdf/:pdfId', component: PdfDetailComponent },
	{
		path: ':moduleId/validations/:validationId',
		component: ValidationsDetailComponent,
	},
	{
		path: `:moduleId/channels/chat-widgets/:chatName/prompt/:promptId`,
		component: ChatPromptMasterComponent,
	},
	{
		path: ':moduleId/chatbots/:chatBotId',
		component: ChatBotsDetailComponent,
	},
	{
		path: ':moduleId/channels/chat-widgets/:chatName',
		component: ChatGettingStartedComponent,
	},
	{
		path: ':moduleId/channels/facebook/facebook-detail',
		component: FacebookDetailComponent,
	},
	{
		path: ':moduleId/channels/chat-widgets',
		component: ChatChannelsMasterComponent,
	},
	{
		path: ':moduleId/channels/:channelType',
		component: ChannelsListComponent,
	},
	{
		path: ':moduleId/edit_mobile_layouts/:mobileLayoutId',
		component: MobileDetailLayoutComponent,
	},
	{
		path: ':moduleId/create_mobile_layouts/:mobileLayoutId',
		component: MobileDetailLayoutComponent,
	},
	{
		path: ':moduleId/detail_mobile_layouts/:mobileLayoutId',
		component: MobileDetailLayoutComponent,
	},
	{ path: ':moduleId/:layoutType/:layoutId', component: DetailLayoutComponent },
	{
		path: ':moduleId/channels/email/:emailId',
		component: EmailDetailComponent,
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
})
export class ModulesRoutingModule {}
