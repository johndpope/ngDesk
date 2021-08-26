import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoaderService {
	public isLoading = false;
	public isLoading2 = false;
	public isLoading3 = false;

	constructor() {
		//   public setSpinner(showSpinner: boolean) {
		//     this.subject.next(showSpinner);
		//   }
		//   public getSpinner(): Observable<any> {
		//     return this.subject.asObservable();
		//   }
	}
}
