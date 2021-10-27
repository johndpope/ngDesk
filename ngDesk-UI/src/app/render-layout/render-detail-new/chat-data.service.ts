import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '@src/app/app.globals';

@Injectable({
	providedIn: 'root',
})
export class ChatDataService {
	constructor(private http: HttpClient, private globals: AppGlobals) {}

	// to get chat channel

	public getChatChannel() {
		const channelQuery = `{
      CHAT_CHANNEL: getChatChannel(name: "Chats") {
        name
        description
        sourceType
        title
        subTitle
        file
        color
        textColor
        senderBubbleColor
        receiverBubbleColor
        senderTextColor
        receiverTextColor
        }
    }`;
		return this.makeGraphQLCall(channelQuery);
	}

	public getUsersForAgent() {
		const requesterQuery = `
    {
      getEntriesByAgentAndStatus {
      REQUESTOR{
      FULL_NAME
      USER{
      EMAIL_ADDRESS
      }
      }
      _id
      STATUS
      }
      }
      `;
		return this.makeGraphQLCall(requesterQuery);
	}

	private makeGraphQLCall(query) {
		return this.http.post(`${this.globals.graphqlUrl}`, query);
	}
}
