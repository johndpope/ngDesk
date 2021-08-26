import { Injectable } from '@angular/core';
import * as CryptoJS from 'crypto-js';
import { UsersService } from '@src/app/users/users.service';

@Injectable({
	providedIn: 'root',
})
export class PasswordEncryptionDecryptionService {
    private encryptSecretKey: any;
	constructor(
        private userService: UsersService,
	) {}
    public encryptData(data) {
        this.encryptSecretKey = this.userService.getSubdomain();
        try {
          return CryptoJS.AES.encrypt(JSON.stringify(data), this.encryptSecretKey).toString();
        } catch (e) {
          console.log(e);
        }
      }
    
    public decryptData(data) {
        this.encryptSecretKey = this.userService.getSubdomain();
        try {
          const bytes = CryptoJS.AES.decrypt(data, this.encryptSecretKey);
          if (bytes.toString()) {
            return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
          }
          return data;
        } catch (e) {
          console.log(e);
        }
      }
}
