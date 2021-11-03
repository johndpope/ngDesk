import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppGlobals } from '../../app.globals';
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
		const url = this.globals.graphqlUrl;
		return this.makeGraphQLCall(url, channelQuery);
	}

	public getChatsByUserId(moduleID: string, filterConditions: [], search: any) {
		const query = `{ DATA: getChats( moduleId: "${moduleID}" pageNumber: 0 pageSize: 10 sortBy: "DATE_CREATED" orderBy: "asc" ) { 
			DATA_ID: _id 
			STATUS
			
			REQUESTOR{
				FIRST_NAME
				LAST_NAME
				DATA_ID: _id 
				    USER{
				    EMAIL_ADDRESS
					
				    	}
				    }
		}
	}`;
		let payload: any = {
			query: query,
			conditions: filterConditions,
		};
		// const url = this.globals.graphqlEmailListsUrl;

		const url = this.globals.graphqlEmailListsUrl;
		return this.makeGraphQLCall(url, payload);
	}

	private makeGraphQLCall(url, query) {
		return this.http.post(`${url}`, query);
	}
}
