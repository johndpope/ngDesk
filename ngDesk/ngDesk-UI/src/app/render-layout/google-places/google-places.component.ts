/// <reference types="@types/googlemaps" />
import {
	AfterViewInit,
	Component,
	EventEmitter,
	Input,
	OnInit,
	Output,
	ViewChild
} from '@angular/core';

@Component({
	// tslint:disable-next-line: component-selector
	selector: 'GooglePlaceComponent',
	templateUrl: './google-places.component.html'
})
export class GooglePlaceComponent implements OnInit, AfterViewInit {
	@Input() public addressType: string;
	@Input() public layoutStyle: string;
	@Output() public setAddress: EventEmitter<any> = new EventEmitter();
	@ViewChild('addresstext', { static: true }) public addresstext: any;

	constructor() {}

	public ngOnInit() {}

	public ngAfterViewInit() {
		this.getPlaceAutocomplete();
	}

	private getPlaceAutocomplete() {
		const autocomplete = new google.maps.places.Autocomplete(
			this.addresstext.nativeElement,
			{
				componentRestrictions: { country: 'US' },
				types: [this.addressType] // 'establishment' / 'address' / 'geocode'
			}
		);
		google.maps.event.addListener(autocomplete, 'place_changed', () => {
			const place = autocomplete.getPlace();
			this.invokeEvent(place);
		});
	}

	// tslint:disable-next-line: ban-types
	public invokeEvent(place: Object) {
		this.setAddress.emit(place);
	}
}
