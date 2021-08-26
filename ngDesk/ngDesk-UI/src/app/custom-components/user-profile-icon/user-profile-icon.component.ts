import { Component, Input, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
@Component({
  selector: 'app-user-profile-icon',
  templateUrl: './user-profile-icon.component.html',
  styleUrls: ['./user-profile-icon.component.scss']
})
export class UserProfileIconComponent implements OnInit {

  @Input() public user: any;

  constructor(private translateService: TranslateService) { }

  public ngOnInit() {

  }

  public setStatusStyles(status) {
    let background;
    switch (status) {
      case this.translateService.instant('ONLINE'):
        background = '#2ECC40';
        break;
      case this.translateService.instant('OFFLINE'):
        background = '#AAAAAA';
        break;
      case this.translateService.instant('AWAY'):
        background = '#FFDC00';
        break;
      case this.translateService.instant('BUSY'):
        background = '#FF4136';
        break;
    }

    const styles = {
      'margin-left': window.navigator.userAgent.indexOf('Firefox') !== -1 ? '23px' : '11px',
      'margin-top': window.navigator.userAgent.indexOf('Firefox') !== -1 ? '23px' : '11px',
      'background-color': background
    };
    return styles;
  }
}
