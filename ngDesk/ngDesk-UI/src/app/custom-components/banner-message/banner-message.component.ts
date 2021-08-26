import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component } from '@angular/core';

import { BannerMessageService } from './banner-message.service';
@Component({
  selector: 'app-banner-message',
  templateUrl: './banner-message.component.html',
  styleUrls: ['./banner-message.component.scss'],
  animations: [trigger('fadeInOut', [
    state('void', style({
      opacity: 0,
      transform: 'scale(0.5)'
    })),
    state('displayed', style({
      transform: 'scale(1.5)',
      opacity: 1
    })),
    transition('void=>displayed', animate('500ms')),
    transition('displayed=>void', animate('500ms')),
  ])]
})
export class BannerMessageComponent {
  public timer;
  constructor(public bannerMessageService: BannerMessageService) {
      this.timer = setInterval(() => {
      this.deleteNotification('success');
      // if there are more than one consecutive empty messages remove them automatically.
      this.bannerMessageService.errorNotifications.forEach((err, index) => {
        if (index !== this.bannerMessageService.errorNotifications.length - 1 &&
          !err.message && !this.bannerMessageService.errorNotifications[index + 1].message) {
          this.deleteEmptyErrorNotification(index);
        }
      });
      this.deleteNotification('error');
    }, 5000);
  }

  private deleteNotification(type) {
    if(type === 'success'){
      this.bannerMessageService.successNotifications.shift();
    } else {
      this.bannerMessageService.errorNotifications.shift();  
    }
    if(this.bannerMessageService.successNotifications.length === 0 && this.bannerMessageService.errorNotifications.length === 0){
      clearInterval(this.timer);
    }
  }

  private deleteEmptyErrorNotification(index) {
    this.bannerMessageService.errorNotifications.splice(index, 1);
  }

  private manualClose(index){
    clearInterval(this.timer);
    this.timer = setInterval(() => {
      this.deleteNotification('success');
      this.deleteNotification('error');
    }, 5000);
    this.bannerMessageService.errorNotifications.splice(index, 1)
  }

}
