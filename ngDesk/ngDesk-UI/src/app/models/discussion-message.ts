import { DiscussionAttachment } from './discussion-attachment';
import { DiscussionSender } from './discussion-sender';

export class DiscussionMessage {

  public MESSAGE: string;
  public MODULE: string;
  public CHAT_ID?: string;
  public COMPANY_UUID: string;
  public ENTRY_ID: string;
  public DATE_CREATED: Date;
  public SENDER: DiscussionSender;
  public ATTACHMENTS: DiscussionAttachment[];

  constructor(message, moduleId, chatId, companyUuid,
    entryId, dateCreated, sender, attachments) {
    this.MESSAGE = message;
    this.MODULE = moduleId;
    this.CHAT_ID = chatId;
    this.COMPANY_UUID = companyUuid;
    this.ENTRY_ID = entryId;
    this.DATE_CREATED = dateCreated;
    this.SENDER = sender;
    this.ATTACHMENTS = attachments;
  }

}
