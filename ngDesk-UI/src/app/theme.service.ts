import { HttpClient } from '@angular/common/http'; 
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { AppGlobals } from './app.globals';

@Injectable()
export class ThemeService {
	private _primaryColor = new Subject<string>();
	public primaryColor = this._primaryColor.asObservable();

	constructor(private http: HttpClient, private globals: AppGlobals) {}

	public setColorTheme(color: string): void {
		// tslint:disable-next-line: prefer-switch
		if (color === '#3f51b5') {
			this._primaryColor.next('blue-theme');
		} else if (color === '#43a047') {
			this._primaryColor.next('green-theme');
			// tslint:disable-next-line: prefer-switch
		} else if (color === '#f44336') {
			this._primaryColor.next('red-theme');
		} else if (color === '#f90200') {
			this._primaryColor.next('red1-theme');
		} else if (color === '#ffea00') {
			this._primaryColor.next('yellow-theme');
		} else if (color === '#9c27b0') {
			this._primaryColor.next('purple-theme');
		} else if (color === '#000000') {
			this._primaryColor.next('black-theme');
		}
	}

	public getThemeColor() {
		return this.http.get(`${this.globals.baseRestUrl}/companies/themes`);
	}
}
