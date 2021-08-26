import {
	Directive,
	EventEmitter,
	Input,
	OnDestroy,
	Output,
} from '@angular/core';
import { MatAutocomplete } from '@angular/material/autocomplete';
import { Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';

export interface AutoCompleteScrollEvent {
	autoComplete: MatAutocomplete;
	scrollEvent: Event;
}

@Directive({
	selector: 'mat-autocomplete[appAutocompleteScroll]',
})
export class AutocompleteScrollDirective implements OnDestroy {
	@Input() public thresholdPercent = 0.99;
	@Output('appAutocompleteScroll')
	public scroll = new EventEmitter<AutoCompleteScrollEvent>();
	public _onDestroy = new Subject();
	constructor(public autoComplete: MatAutocomplete) {
		this.autoComplete.opened
			.pipe(
				tap(() => {
					setTimeout(() => {
						this.removeScrollEventListener();
						this.autoComplete?.panel?.nativeElement.addEventListener(
							'scroll',
							this.onScroll.bind(this)
						);
					});
				}),
				takeUntil(this._onDestroy)
			)
			.subscribe();

		this.autoComplete.closed
			.pipe(
				tap(() => this.removeScrollEventListener()),
				takeUntil(this._onDestroy)
			)
			.subscribe();
	}

	private removeScrollEventListener() {
		if (this.autoComplete && this.autoComplete.panel) {
			this.autoComplete.panel.nativeElement.removeEventListener(
				'scroll',
				this.onScroll
			);
		}
	}

	public ngOnDestroy() {
		this._onDestroy.next();
		this._onDestroy.complete();

		this.removeScrollEventListener();
	}

	public onScroll(event) {
		if (this.thresholdPercent === undefined) {
			this.scroll.next({ autoComplete: this.autoComplete, scrollEvent: event });
		} else {
			const threshold =
				(this.thresholdPercent * 100 * event.target.scrollHeight) / 100;
			const current = event.target.scrollTop + event.target.clientHeight;
			if (current > threshold) {
				this.scroll.next({
					autoComplete: this.autoComplete,
					scrollEvent: event,
				});
			}
		}
	}
}
