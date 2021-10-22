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
      CHAT_CHANNEL: getChatChannel(name: "Chat") {
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
		return this.http.post(`${this.globals.graphqlUrl}`, channelQuery);
	}
}
