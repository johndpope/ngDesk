import { Injectable } from '@angular/core';
import { Events } from 'backbone';
import { Observable, Subject, Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';

interface SharedEvent {
	name: string;
	value: any;
}

@Injectable({
	providedIn: 'root',
})
export class EventBusService {
	constructor() {
		Object.assign(this, Events);
	}

	private _events = new Subject<SharedEvent>();

	events(): Observable<SharedEvent> {
		return this._events.asObservable();
	}

	emit(eventName: string, value?: any): void {
		this._events.next({ name: eventName, value: value });
	}

	on(eventName: string, callback: any): Subscription {
		return this._events
			.pipe(
				filter((e) => e.name === eventName),
				map((e) => e.value)
			)
			.subscribe(callback);
	}
}
