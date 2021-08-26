import { ErrorHandler, Injectable } from '@angular/core';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
	public handleError(error: any): void {
		const chunkFailedMessage = /Loading chunk [\d]+ failed/;
		console.error(error);
		if (chunkFailedMessage.test(error.message)) {
			(<any>window).location.reload(true);
		}
	}
}
