import { APP_BASE_HREF } from '@angular/common';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ActivatedRoute, RouterModule, Routes } from '@angular/router';

import { EscalationsDetailComponent } from './escalations-detail.component';

import {
	MissingTranslationHandler,
	TranslateLoader,
	TranslateModule
} from '@ngx-translate/core';
import { TranslateService } from '@ngx-translate/core';

import { HttpClient, HttpClientModule } from '@angular/common/http';

import { NewNameDescriptionComponent } from 'src/app/custom-components/new-name-description/new-name-description.component';
import { ToolbarComponent } from 'src/app/toolbar/toolbar.component';

import { CustomMissingTranslationHandler } from 'src/app/custom-missing-translation.handler';

import { HttpLoaderFactory } from 'src/app/app.module';

import { MockComponent } from 'ng-mocks';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';

import { MatInputModule } from '@angular/material/input';
import { CookieService } from 'ngx-cookie-service';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppGlobals } from '../../app.globals';

import { BASE_PATH as ESCALATION_BASE_PATH } from '@ngdesk/escalation-api';

import { LoggerModule, NgxLoggerLevel } from 'ngx-logger';

import { EscalationApiService } from '@ngdesk/escalation-api';
import { EscalationsService } from '../escalations.service';

import { forkJoin, of } from 'rxjs';

describe('EscalationDetailComponent', () => {
	let component: EscalationsDetailComponent;
	let fixture: ComponentFixture<EscalationsDetailComponent>;
	let translate: TranslateService;
	let fakeEscalationService;
	let fakeEscalationApiService;

	const routes: Routes = [
		{
			path: 'escalations/:escalationId',
			component: EscalationsDetailComponent
		}
	];

	beforeEach(waitForAsync(() => {
		fakeEscalationService = jasmine.createSpyObj('EscalationsService', [
			'getDataForEscalations'
		]);
		fakeEscalationService.getDataForEscalations.and.returnValue(
			forkJoin([
				of(true),
				of({ SCHEDULES: [] }),
				of({ DATA: [] }),
				of({ DATA: [] })
			])
		);

		fakeEscalationApiService = jasmine.createSpyObj('EscalationApiService', [
			'getEscalationById'
		]);

		TestBed.configureTestingModule({
			declarations: [
				EscalationsDetailComponent,
				NewNameDescriptionComponent,
				MockComponent(ToolbarComponent)
			],
			imports: [
				TranslateModule.forRoot({
					loader: {
						provide: TranslateLoader,
						useFactory: HttpLoaderFactory,
						deps: [HttpClient]
					},
					missingTranslationHandler: {
						provide: MissingTranslationHandler,
						useClass: CustomMissingTranslationHandler
					}
				}),
				HttpClientModule,
				BrowserAnimationsModule,
				MatIconModule,
				MatCardModule,
				MatInputModule,
				MatChipsModule,
				MatFormFieldModule,
				MatAutocompleteModule,
				FormsModule,
				ReactiveFormsModule,
				RouterModule.forRoot(routes, { enableTracing: true, relativeLinkResolution: 'legacy' }),
				LoggerModule.forRoot({
					level: NgxLoggerLevel.DEBUG
				})
			],
			providers: [
				AppGlobals,
				CookieService,
				{
					provide: ESCALATION_BASE_PATH,
					useValue: '/api/ngdesk-escalation-service-v1'
				},
				TranslateService,
				{ provide: EscalationsService, useValue: fakeEscalationService },
				{ provide: APP_BASE_HREF, useValue: '/' },
				{
					provide: ActivatedRoute,
					useValue: {
						snapshot: {
							params: {
								escalationId: 'new'
							}
						}
					}
				},
				{ provide: EscalationApiService, useValue: fakeEscalationApiService }
			]
		}).compileComponents();
		translate = TestBed.inject(TranslateService);
		translate.use('en');
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(EscalationsDetailComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
		setTimeout(function() {
			// Setting Custom Timeout to Avoid Jasmine Default Timeout as the other one causes tests to fail randomly
		}, 500);
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it('Escalation form invalid when empty', () => {
		expect(component.escalationForm.valid).toBeFalsy();
	});

	it('Escalation Name field validity', () => {
		let errors = {};
		const name = component.escalationForm.controls['NAME'];
		expect(name.valid).toBeFalsy();

		// Email field is required
		errors = name.errors || {};
		expect(errors['required']).toBeTruthy();

		// Set name to random value
		name.setValue('Test Escalation');
		errors = name.errors || {};
		expect(errors['required']).toBeFalsy();

		// Set name to empty string
		name.setValue('');
		errors = name.errors || {};
		expect(name.valid).toBeFalsy();
		expect(errors['required']).toBeTruthy();
	});

	it('Rule should be added', () => {
		const rulesLength = component.escalation.RULES.length;
		component.addRule();
		expect(component.escalation.RULES.length).toEqual(rulesLength + 1);
	});
});
