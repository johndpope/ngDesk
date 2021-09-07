import { Injectable } from "@angular/core";
import { HttpClient } from '@angular/common/http';
import { AppGlobals } from '@src/app/app.globals';
import { UsersService } from '../users/users.service';


@Injectable({
    providedIn: 'root',
})
export class ToolbarService {
    public showAcceptChat: boolean = true;

    public authorizedUsersForChat = [];
    constructor(
        private http: HttpClient,
        private globals: AppGlobals,
        public usersService: UsersService,
    ) {
    }

    public updateShowAcceptChat() {
        let authorizedUsers = [];
        this.getAuthorizedUsersForChat().subscribe((usersResponse: any) => {
            if (usersResponse['DATA']['USERS'].length > 0) {
                usersResponse['DATA']['USERS'].forEach(user => {
                    authorizedUsers.push(user.EMAIL_ADDRESS);
                });
                this.authorizedUsersForChat = authorizedUsers;
                if (
                    !this.authorizedUsersForChat.includes(this.usersService.user.EMAIL_ADDRESS)
                ) {
                    this.showAcceptChat = false;
                } else {
                    this.showAcceptChat = true;
                }
            } else {
                this.showAcceptChat = false;
                this.authorizedUsersForChat = [];
            }
        });

    }

    public getAuthorizedUsersForChat() {

        let query = `{
		DATA:getAuthorizedUserForChat {
			USERS: users {
			ID:	_id
			    EMAIL_ADDRESS
			  }
			}
		  }
		  `;
        return this.http.post(`${this.globals.graphqlUrl}`, query);

    }

}