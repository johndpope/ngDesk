import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BannerMessageService {
  public successNotifications = [];
  public errorNotifications = [];
  constructor() { }
}
