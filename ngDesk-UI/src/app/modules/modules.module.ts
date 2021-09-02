import { DragDropModule } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EmailChannelDialogComponent } from '../dialogs/email-channel-dialog/email-channel-dialog.component';
import { FacebookChannelDialogComponent } from '../dialogs/facebook-channel-dialog/facebook-channel-dialog.component';
import { SMSChannelDialogComponent } from '../dialogs/sms-channel-dialog/sms-channel-dialog.component';
import { SMSChannelDisplayDialogComponent } from '../dialogs/sms-channel-display-dialog/sms-channel-display-dialog.component';
import { TwilioRequestComponent } from '../dialogs/twilio-request/twilio-request.component';
import { SharedModule } from '../shared-module/shared.module';
import { ChannelComponent } from './modules-detail/channels/channel.component';
import { ChannelsListComponent } from './modules-detail/channels/channels-list/channels-list.component';
import { ChatBotCreateComponent } from './modules-detail/channels/chat-bots/chat-bot-create/chat-bot-create.component';
import { ChatBotGettingStartedComponent } from './modules-detail/channels/chat-bots/chat-bot-getting-started/chat-bot-getting-started.component';
import { ChatBotsDetailComponent } from './modules-detail/channels/chat-bots/chat-bots-detail/chat-bots-detail.component';
import { FilterDefaultFieldsPipe } from './modules-detail/channels/chat-bots/chat-bots-detail/chat-bots-detail.component';
import { ChatBotsMasterComponent } from './modules-detail/channels/chat-bots/chat-bots-master/chat-bots-master.component';
import { ChatChannelsMasterComponent } from './modules-detail/channels/chat-widgets/chat-channels-master/chat-channels-master.component';
import { ChatPromptMasterComponent } from './modules-detail/channels/chat-widgets/chat-prompt-master/chat-prompt-master.component';
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
import { ModulesRoutingModule } from './modules-routing.module';
import { EmailDetailComponent } from './modules-detail/channels/email-detail/email-detail.component';
import { FieldFilterDialogComponent } from '../dialogs/field-filter-dialog/field-filter-dialog.component';
import { InspectorComponent } from './modules-detail/triggers/triggers-detail-new/inspector/inspector.component';
import { MessageInspectorComponent } from './modules-detail/triggers/triggers-detail-new/inspector/message-inspector/message-inspector.component';
import { LabelInspectorComponent } from './modules-detail/triggers/triggers-detail-new/inspector/label-inspector/label-inspector.component';
import { LinkInspectorComponent } from './modules-detail/triggers/triggers-detail-new/inspector/link-inspector/link-inspector.component';
import { TriggersDetailNewComponent } from './modules-detail/triggers/triggers-detail-new/triggers-detail-new.component';
import { NodeCustomizationComponent } from './modules-detail/triggers/triggers-detail-new/node-customization/node-customization.component';
import { ConditionsDialogComponent } from './modules-detail/triggers/triggers-detail-new/conditions-dialog/conditions-dialog.component';
import { StagesComponent } from './modules-detail/triggers/triggers-detail-new/stages/stages.component';
import { EventBusService } from './modules-detail/triggers/triggers-detail-new/services/event-bus.service';
import { TriggersDetailService } from './modules-detail/triggers/triggers-detail-new/triggers-detail.service';
import { WorkflowCreateComponent } from './modules-detail/triggers/workflow-create/workflow-create.component';
import { OwlMomentDateTimeModule } from '@danielmoncada/angular-datetime-picker';
import { TaskMasterComponent } from './modules-detail/task/task-master/task-master.component';
import { TaskDetailComponent } from './modules-detail/task/task-detail/task-detail.component';
import { FormsComponent } from './modules-detail/forms/forms.component';
import { ServiceCatalogueComponent } from './modules-detail/forms/service-catalogue/service-catalogue.component';
import { ServiceCatalogueDetailComponent } from './modules-detail/forms/service-catalogue-detail/service-catalogue-detail.component';
import { OrderByPipe } from './modules-detail/layouts/list-layout/order-by.pipe';

@NgModule({
	declarations: [
		ModulesMasterComponent,
		ModulesDetailComponent,
		FieldCreatorComponent,
		FieldMasterComponent,
		FieldViewComponent,
		LayoutsComponent,
		LayoutsMasterComponent,
		ListLayoutComponent,
		DetailLayoutComponent,
		TriggersMasterComponent,
		TriggersDetailComponent,
		PdfDetailComponent,
		PdfMasterComponent,
		SlaDetailComponent,
		SlaMasterComponent,
		ChannelsListComponent,
		ChannelComponent,
		EmailChannelDialogComponent,
		FacebookChannelDialogComponent,
		FacebookDetailComponent,
		SMSChannelDialogComponent,
		SMSChannelDisplayDialogComponent,
		TwilioRequestComponent,
		ValidationsMasterComponent,
		ValidationsDetailComponent,
		TaskMasterComponent,
		TaskDetailComponent,
		ChatBotsDetailComponent,
		ChatBotsMasterComponent,
		MobileListLayoutComponent,
		MobileDetailLayoutComponent,
		ChatChannelsMasterComponent,
		ChatPromptMasterComponent,
		FilterDefaultFieldsPipe,
		ChatBotGettingStartedComponent,
		ChatBotCreateComponent,
		FormsDetailComponent,
		FormsMasterComponent,
		EmailDetailComponent,
		FieldFilterDialogComponent,
		InspectorComponent,
		MessageInspectorComponent,
		LabelInspectorComponent,
		LinkInspectorComponent,
		TriggersDetailNewComponent,
		NodeCustomizationComponent,
		ConditionsDialogComponent,
		StagesComponent,
		WorkflowCreateComponent,
		FormsComponent,
		ServiceCatalogueComponent,
		ServiceCatalogueDetailComponent,
  OrderByPipe,
	],
	imports: [
		CommonModule,
		SharedModule,
		FlexLayoutModule,
		FormsModule,
		ReactiveFormsModule,
		MatButtonModule,
		MatCardModule,
		MatCheckboxModule,
		MatChipsModule,
		MatIconModule,
		MatTabsModule,
		MatInputModule,
		MatFormFieldModule,
		MatRadioModule,
		MatSelectModule,
		ModulesRoutingModule,
		MatTooltipModule,
		DragDropModule,
		MatDividerModule,
		// CompileModule,
		MatDialogModule,
		MatSlideToggleModule,
		MatButtonToggleModule,
		MatExpansionModule,
		MatPaginatorModule,
		MatProgressSpinnerModule,
		MatTableModule,
		MatMenuModule,
		OwlMomentDateTimeModule,
	],
	providers: [EventBusService, TriggersDetailService],
	// entryComponents: [
	// 	EmailChannelDialogComponent,
	// 	FacebookChannelDialogComponent,
	// 	SMSChannelDialogComponent,
	// 	SMSChannelDisplayDialogComponent,
	// 	TwilioRequestComponent,
	// 	FieldFilterDialogComponent,
	// ],
})
export class ModulesModule {}
