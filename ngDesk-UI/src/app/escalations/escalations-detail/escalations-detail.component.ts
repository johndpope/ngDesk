import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import {
	MatAutocomplete,
	MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { FilterRuleOptionPipe } from '@src/app/custom-components/conditions/filter-rule-option/filter-rule-option.pipe';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { EscalationsService } from '@src/app/escalations/escalations.service';
import { User } from '@src/app/models/role';
import { Schedule } from '@src/app/models/schedule';
import { AppGlobals } from '@src/app/app.globals';
import {
	EscalateTo,
	Escalation,
	EscalationApiService,
	EscalationRule,
} from '@ngdesk/escalation-api';
import { CacheService } from '@src/app/cache.service';
import { Subscription } from 'rxjs/internal/Subscription';
import { HttpClient } from '@angular/common/http';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	mergeMap,
	switchMap,
} from 'rxjs/operators';
import { ModulesService } from '@src/app/modules/modules.service';

@Component({
	selector: 'app-escalations-detail',
	templateUrl: './escalations-detail.component.html',
	styleUrls: ['./escalations-detail.component.scss'],
})
export class EscalationsDetailComponent implements OnInit {
	public escalation: Escalation = {
		NAME: '',
		DESCRIPTION: '',
		RULES: [],
	} as Escalation;
	public escalationForm: FormGroup;

	public module: any;
	public separatorKeysCodes: number[] = [ENTER, COMMA];
	public errorMessage: string;
	public successMessage: string;
	public editAccess: boolean;
	public schedules: Schedule[] = [];
	public schedulesInitial: Schedule[] = [];
	public users: User[] = [];
	public usersInitial: User[] = [];
	public teams = [];
	public teamsInitial = [];
	public disableButtons = false;
	public teamScrollSubject = new Subject<any>();
	public userScrollSubject = new Subject<any>();
	public userCtrl = new FormControl();
	public teamCtrl = new FormControl();
	public scheduleCtrl = new FormControl();

	private escalationId: string;

	@ViewChild('teamInput') public teamInput: ElementRef<HTMLInputElement>;
	@ViewChild('userInput') public userInput: ElementRef<HTMLInputElement>;
	@ViewChild('scheduleInput')
	public scheduleInput: ElementRef<HTMLInputElement>;
	@ViewChild('auto') public matAutocomplete: MatAutocomplete;

	public companyInfoSubscription: Subscription;
	public userModuleId: any;
	public teamModuleId: any;

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private formBuilder: FormBuilder,
		private escalationApiService: EscalationApiService,
		private escalationService: EscalationsService,
		private loaderService: LoaderService,
		private cacheService: CacheService,
		private http: HttpClient,
		private globals: AppGlobals,
		private modulesService: ModulesService
	) {}

	public ngOnInit() {
		let page = 0;
		this.modulesService
			.getModuleByName('Users')
			.pipe(
				map((response: any) => {
					this.userModuleId = response.MODULE_ID;
				}),
				mergeMap((users) =>
					this.escalationService.getUsersData(this.userModuleId, page, '')
				)
			)
			.subscribe((userResponse: any) => {
				this.users = userResponse['DATA'];

				this.usersDataScroll();
			});

		this.modulesService
			.getModuleByName('Teams')
			.pipe(
				map((response: any) => {
					this.teamModuleId = response.MODULE_ID;
				}),
				mergeMap((teams) =>
					this.escalationService.getTeamsData(this.teamModuleId, page, '')
				)
			)
			.subscribe((teamResponse: any) => {
				this.teams = teamResponse['DATA'];

				this.teamsDataScroll();
			});

		this.escalationForm = this.formBuilder.group({});

		this.escalationId = this.route.snapshot.params['escalationId'];
		this.companyInfoSubscription =
			this.cacheService.companyInfoSubject.subscribe((dataStored) => {
				if (dataStored) {
					this.escalationService
						.getDataForEscalations()
						.subscribe((responseList) => {
							this.editAccess = responseList[0];
							this.schedules = responseList[1].SCHEDULES;
							this.schedules = this.schedules.sort((a, b) =>
								a.name.localeCompare(b.name)
							);
							this.schedulesInitial = JSON.parse(
								JSON.stringify(responseList[1].SCHEDULES)
							);
							this.schedulesInitial = this.schedulesInitial.sort((a, b) =>
								a.name.localeCompare(b.name)
							);
							this.usersInitial = JSON.parse(JSON.stringify(responseList[2]));

							this.teamsInitial = JSON.parse(
								JSON.stringify(responseList[3].DATA)
							);

							if (this.escalationId !== 'new') {
								this.escalationApiService
									.getEscalationById(this.escalationId)
									.subscribe(
										(escalationResponse: Escalation) => {
											this.escalation = escalationResponse;
											this.escalationForm.controls.NAME.setValue(
												this.escalation.NAME
											);
											this.escalationForm.controls.DESCRIPTION.setValue(
												this.escalation.DESCRIPTION
											);
										},
										(error: any) => {
											this.errorMessage = error.error.ERROR;
										}
									);
							} else {
								this.addRule();
							}
						});
				}
			});
	}

	public addRule() {
		// adds new rule to escalation
		const escalateTo = {
			SCHEDULE_IDS: [],
			USER_IDS: [],
			TEAM_IDS: [],
		} as EscalateTo;
		const escalationRule = {
			MINS_AFTER: 0,
			ORDER: this.escalation.RULES.length + 1,
			ESCALATE_TO: escalateTo,
		} as EscalationRule;
		this.escalation.RULES.push(escalationRule);
	}

	public removeRule(index) {
		this.escalation.RULES.splice(index, 1);
		for (let i = 0; i < this.escalation.RULES.length; i++) {
			this.escalation.RULES[i].ORDER = i + 1;
		}
	}

	// If input doesn't match in dropdown, reseting the input field
	public resetInput(event: MatChipInputEvent): void {
		if (!this.matAutocomplete.isOpen) {
			const input = event.input;
			// Reset the input value
			if (input) {
				input.value = '';
			}
		}
	}

	// removing uers/teams/schedules from array
	public removeItem(object: string, ruleIndex, arrayType): void {
		const index =
			this.escalation.RULES[ruleIndex].ESCALATE_TO[arrayType].indexOf(object);

		if (index >= 0 && arrayType) {
			this.escalation.RULES[ruleIndex].ESCALATE_TO[arrayType].splice(index, 1);
		}
	}

	public selected(
		event: MatAutocompleteSelectedEvent,
		index,
		inputType,
		arrayType
	): void {
		if (inputType === 'scheduleInput') {
			this.escalation.RULES[index].ESCALATE_TO[arrayType].push(
				event.option.value.scheduleId
			);
		} else if (event.option.value.DATA_ID) {
			this.escalation.RULES[index].ESCALATE_TO[arrayType].push(
				event.option.value.DATA_ID
			);
		} else if (event.option.value['name']) {
			this.escalation.RULES[index].ESCALATE_TO[arrayType].push(
				event.option.value.id
			);
		}

		this[inputType].nativeElement.value = '';
	}

	// filtering array based on input event
	public filterInputValues(event, filteredArray, initialArray) {
		let input = event;
		if (event.hasOwnProperty('DATA_ID')) {
			if (filteredArray === 'teams') {
				input = event.NAME;
			} else if (filteredArray === 'users') {
				input = event.CONTACT.PRIMARY_DISPLAY_FIELD;
			}
		} else if (event.hasOwnProperty('scheduleId')) {
			input = event.name;
		}

		this[filteredArray] = new FilterRuleOptionPipe().transform(
			initialArray,
			input.toLowerCase(),
			filteredArray
		);
	}

	public getDisplayNameFromId(id, type, idKey) {
		const objFound = this[type].find((obj) => obj[idKey] === id);

		if (type === 'usersInitial') {
			return objFound['EMAIL_ADDRESS'];
		} else if (type === 'teamsInitial') {
			return objFound['NAME'];
		} else if (type === 'schedulesInitial') {
			return objFound['name'];
		} else if (objFound !== undefined && objFound) {
			return objFound['NAME'];
		}
	}

	public save() {
		this.escalationForm.get('NAME').markAsTouched();
		if (this.escalationForm.valid) {
			this.escalation.NAME = this.escalationForm.value['NAME'];
			this.escalation.DESCRIPTION = this.escalationForm.value['DESCRIPTION'];
			const escalationObj: Escalation = JSON.parse(
				JSON.stringify(this.escalation)
			);
			if (this.escalationId === 'new') {
				this.escalationApiService.postEscalation(escalationObj).subscribe(
					(escalationResponse: any) => {
						this.router.navigate([`escalations`]);
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
						this.loaderService.isLoading = false;
					}
				);
			} else {
				this.escalationApiService.putEscalation(escalationObj).subscribe(
					(escalationResponse: any) => {
						this.router.navigate([`escalations`]);
					},
					(error: any) => {
						this.errorMessage = error.error.ERROR;
						this.loaderService.isLoading = false;
					}
				);
			}
		} else {
			this.loaderService.isLoading = false;
		}
	}
	public teamsDataScroll() {
		this.teamScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'NAME' + '=' + value;
					}
					let page = 0;
					if (this.teams && !search) {
						page = Math.ceil(this.teams.length / 10);
					}
					return this.escalationService
						.getTeamsData(this.teamModuleId, page, searchValue)
						.pipe(
							map((results: any) => {
								if (search) {
									this.teams = results['DATA'];
								} else if (results['DATA'].length > 0) {
									this.teams = this.teams.concat(results['DATA']);
								}
								return results['DATA'];
							})
						);
				})
			)
			.subscribe();
	}
	// When scrolling the dropdown.
	public onScrollTeams() {
		this.teamScrollSubject.next(['', false]);
	}
	// While entering any text to the input start searching.
	public onSearch(event) {
		const teams = this.teamCtrl.value;

		if (typeof teams !== 'object') {
			const searchText = teams;
			this.teamScrollSubject.next([searchText, true]);
		}
	}

	public autocompleteClosed() {
		this.teamScrollSubject.next(['', true]);
	}

	public usersDataScroll() {
		this.userScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'EMAIL_ADDRESS' + '=' + value;
					}
					let page = 0;
					if (this.users && !search) {
						page = Math.ceil(this.users.length / 10);
					}
					return this.escalationService
						.getUsersData(this.userModuleId, page, searchValue)
						.pipe(
							map((results: any) => {
								if (search) {
									this.users = results['DATA'];
								} else if (results['DATA'].length > 0) {
									this.users = this.users.concat(results['DATA']);
								}
								return results['DATA'];
							})
						);
				})
			)
			.subscribe();
	}
	// When scrolling the dropdown.
	public onScrollUsers() {
		this.userScrollSubject.next(['', false]);
	}
	// While entering any text to the input start searching.
	public onSearchUser(event) {
		const users = this.userCtrl.value;

		if (typeof users !== 'object') {
			const searchText = users;
			this.userScrollSubject.next([searchText, true]);
		}
	}

	public disableSelectedValues(item, ind, arrayType) {
		if (this.escalation.RULES[ind].ESCALATE_TO[arrayType].length > 0) {
			if (this.escalation.RULES[ind].ESCALATE_TO[arrayType].includes(item)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
