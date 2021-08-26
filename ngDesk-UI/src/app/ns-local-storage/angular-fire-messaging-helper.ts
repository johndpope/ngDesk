import { Injectable } from '@angular/core';
import { AngularFireMessaging } from '@angular/fire/messaging';

@Injectable()
export class  AngularFireMessagingHelper{


    constructor(
        public angularFireMessaging: AngularFireMessaging
        ){
            this.angularFireMessaging.messaging.subscribe(_messaging => {
                _messaging.onMessage = _messaging.onMessage.bind(_messaging);
                _messaging.onTokenRefresh = _messaging.onTokenRefresh.bind(_messaging);
            });
        }
   
    }