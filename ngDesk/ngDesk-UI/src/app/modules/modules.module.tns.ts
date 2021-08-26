import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { NativeScriptCommonModule } from 'nativescript-angular/common';
import { ModulesMasterComponent } from '@src/app/modules/modules-master/modules-master.component';
import { ModulesDetailComponent } from '@src/app/modules/modules-detail/modules-detail.component';
import { FieldCreatorComponent } from '@src/app/modules/modules-detail/fields/field-creator/field-creator.component';
import { FieldMasterComponent } from '@src/app/modules/modules-detail/fields/field-master/field-master.component';
import { FieldViewComponent } from '@src/app/modules/modules-detail/fields/field-view/field-view.component';
import { LayoutsComponent } from '@src/app/modules/modules-detail/layouts/layouts.component';
import { LayoutsMasterComponent } from '@src/app/modules/modules-detail/layouts/layouts-master/layouts-master.component';
import { ListLayoutComponent } from '@src/app/modules/modules-detail/layouts/list-layout/list-layout.component';
import { DetailLayoutComponent } from '@src/app/modules/modules-detail/layouts/detail-layout/detail-layout.component';
import { TriggersMasterComponent } from '@src/app/modules/modules-detail/triggers/triggers-master/triggers-master.component';
import { TriggersDetailComponent } from '@src/app/modules/modules-detail/triggers/triggers-detail/triggers-detail.component';
import { SlaDetailComponent } from '@src/app/modules/modules-detail/slas/sla-detail/sla-detail.component';
import { SlaMasterComponent } from '@src/app/modules/modules-detail/slas/sla-master/sla-master.component';
import { ChannelsListComponent } from '@src/app/modules/modules-detail/channels/channels-list/channels-list.component';
import { ChannelComponent } from '@src/app/modules/modules-detail/channels/channel.component';
import { EmailChannelDialogComponent } from '@src/app/dialogs/email-channel-dialog/email-channel-dialog.component';
import { FacebookChannelDialogComponent } from '@src/app/dialogs/facebook-channel-dialog/facebook-channel-dialog.component';
import { FacebookDetailComponent } from '@src/app/modules/modules-detail/channels/facebook-detail/facebook-detail.component';
import { SMSChannelDialogComponent } from '@src/app/dialogs/sms-channel-dialog/sms-channel-dialog.component';
import { SMSChannelDisplayDialogComponent } from '@src/app/dialogs/sms-channel-display-dialog/sms-channel-display-dialog.component';
import { TwilioRequestComponent } from '@src/app/dialogs/twilio-request/twilio-request.component';
import { ValidationsMasterComponent } from '@src/app/modules/modules-detail/validations/validations-master/validations-master.component';
import { ValidationsDetailComponent } from '@src/app/modules/modules-detail/validations/validations-detail/validations-detail.component';
import { ChatBotsDetailComponent } from '@src/app/modules/modules-detail/channels/chat-bots/chat-bots-detail/chat-bots-detail.component';
import { ChatBotsMasterComponent } from '@src/app/modules/modules-detail/channels/chat-bots/chat-bots-master/chat-bots-master.component';
import { MobileListLayoutComponent } from '@src/app/modules/modules-detail/layouts/mobile-list-layout/mobile-list-layout.component';
import { MobileDetailLayoutComponent } from '@src/app/modules/modules-detail/layouts/mobile-detail-layout/mobile-detail-layout.component';
import { ChatChannelsMasterComponent } from '@src/app/modules/modules-detail/channels/chat-widgets/chat-channels-master/chat-channels-master.component';
import { ChatPromptMasterComponent } from '@src/app/modules/modules-detail/channels/chat-widgets/chat-prompt-master/chat-prompt-master.component';
import { ChatBotGettingStartedComponent } from '@src/app/modules/modules-detail/channels/chat-bots/chat-bot-getting-started/chat-bot-getting-started.component';
import { ChatBotCreateComponent } from '@src/app/modules/modules-detail/channels/chat-bots/chat-bot-create/chat-bot-create.component';
import { FormsDetailComponent } from '@src/app/modules/modules-detail/forms/forms-detail/forms-detail.component';
import { FormsMasterComponent } from '@src/app/modules/modules-detail/forms/forms-master/forms-master.component';
import { EmailDetailComponent } from '@src/app/modules/modules-detail/channels/email-detail/email-detail.component';
import { MessagingService } from '../firebase/messaging.service';
import { TriggersDetailNewComponent } from './modules-detail/triggers/triggers-detail-new/triggers-detail-new.component';
import { NodeCustomizationComponent } from './modules-detail/triggers/triggers-detail-new/node-customization/node-customization.component';
import { ConditionsDialogComponent } from './modules-detail/triggers/triggers-detail-new/conditions-dialog/conditions-dialog.component';
import { StagesComponent } from './modules-detail/triggers/triggers-detail-new/stages/stages.component';
import { WorkflowCreateComponent } from './modules-detail/triggers/workflow-create/workflow-create.component';


@NgModule({
  imports: [
    NativeScriptCommonModule
  ],
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
    ChatBotsDetailComponent,
    ChatBotsMasterComponent,
    MobileListLayoutComponent,
    MobileDetailLayoutComponent,
    ChatChannelsMasterComponent,
    ChatPromptMasterComponent,
    ChatBotGettingStartedComponent,
    ChatBotCreateComponent,
    FormsDetailComponent,
    FormsMasterComponent,
    EmailDetailComponent,
    TriggersDetailNewComponent,
    NodeCustomizationComponent,
    ConditionsDialogComponent,
    StagesComponent,
    WorkflowCreateComponent
  ],
  providers: [
  ],
  schemas: [
    NO_ERRORS_SCHEMA
  ]
})
export class ModulesModule { }
