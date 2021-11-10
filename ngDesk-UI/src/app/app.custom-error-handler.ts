import { ErrorHandler, Injectable } from '@angular/core';
import { NGXLogger } from 'ngx-logger';

@Injectable()
export class CustomErrorHandler implements ErrorHandler {
	constructor(private logger: NGXLogger) {}

	public handleError(error: Error) {
		// console.error('custom error handler');
		// console.error(error);
		//this.logger.error(error);

		const chunkFailedMessage = /Loading chunk [\d]+ failed/;

		if (chunkFailedMessage.test(error.message)) {
			(<any>window).location.reload(true);
		}
	}
}
