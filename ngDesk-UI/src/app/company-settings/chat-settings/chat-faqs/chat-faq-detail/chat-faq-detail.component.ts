import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { ChannelsService } from '../../../../channels/channels.service';
import { ChatFaq } from '../../../../models/chatFaq';
import { ModulesService } from '../../../../modules/modules.service';

@Component({
	selector: 'app-chat-faq-detail',
	templateUrl: './chat-faq-detail.component.html',
	styleUrls: ['./chat-faq-detail.component.scss'],
})
export class ChatFaqDetailComponent implements OnInit {
	public answersForm: FormGroup;
	public questionsForm: FormGroup;
	public chatFaq: ChatFaq = new ChatFaq('', '', [], [], []);
	public faqForm: FormGroup;
	public modules: String[] = [];
	public errorMessage: string;
	public successMessage: string;
	public disableButtons = false;
	public answers: String[];
	public questions: String[];
	public selected = [];
	constructor(
		private bannerMessageService: BannerMessageService,
		private _fb: FormBuilder,
		private translateService: TranslateService,
		private router: Router,
		private route: ActivatedRoute,
		private formBuilder: FormBuilder,
		private channelsService: ChannelsService,
		private modulesService: ModulesService
	) {}

	public ngOnInit() {
		this.answersForm = this._fb.group({
			itemRows: this._fb.array([this.initItemRows()]),
		});
		this.questionsForm = this._fb.group({
			itemRows: this._fb.array([this.initItemRows()]),
		});

		this.modulesService.getAllModules().subscribe((moduleResonse: any) => {
			moduleResonse.MODULES.forEach((element) => {
				this.modules.push(element.MODULE_ID);
			});
			this.modules.splice(0, 3);
		});

		this.faqForm = this.formBuilder.group({
			QUESTIONS: [],
			ANSWERS: [],
		});
		const faqId = this.route.snapshot.params['faqId'];
		if (faqId !== 'new') {
			this.channelsService.getFaq(faqId).subscribe(
				(faqResponse: any) => {
					for (let i = 1; i < faqResponse.QUESTIONS.length; i++) {
						this.addNewQuestion();
					}
					for (let i = 1; i < faqResponse.ANSWERS.length; i++) {
						this.addNewRow();
					}

					this.selected = faqResponse.MODULES;
					this.faqForm.setValue({
						NAME: faqResponse.NAME,
						DESCRIPTION: faqResponse.DESCRIPTION,
						QUESTIONS: faqResponse.QUESTIONS,
						ANSWERS: faqResponse.ANSWERS,
					});

					this.chatFaq = new ChatFaq(
						faqResponse.NAME,
						faqResponse.DESCRIPTION,
						faqResponse.QUESTIONS,
						faqResponse.ANSWERS,
						[]
					);
				},
				(error: any) => {
					this.errorMessage = error.error.ERROR;
				}
			);
		} else {
			for (let i = 0; i < 4; i++) {
				this.addNewQuestion();
			}
		}
	}

	public save() {
		if (this.faqForm.valid) {
			this.chatFaq.NAME = this.faqForm.value['NAME'];
			this.chatFaq.DESCRIPTION = this.faqForm.value['DESCRIPTION'];
			this.questions = this.questionsForm.value['itemRows'].map(
				(a) => a.itemName
			);
			this.answers = this.answersForm.value['itemRows'].map((a) => a.itemName);
			this.chatFaq.QUESTIONS = this.questions;
			this.chatFaq.ANSWERS = this.answers;
			let index = 0;
			this.chatFaq.ANSWERS.forEach((element) => {
				if (element == null) {
					this.chatFaq.ANSWERS[index] = '';
					index++;
				}
			});
			index = 0;
			this.chatFaq.QUESTIONS.forEach((element) => {
				if (element == null) {
					this.chatFaq.QUESTIONS[index] = '';
					index++;
				}
			});
			if (this.chatFaq.QUESTIONS.length < 5) {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Please fill at least 5 questions',
				});
			}
			if (this.chatFaq.ANSWERS.length < 1) {
				return this.bannerMessageService.errorNotifications.push({
					message: 'Please fill at least 1 answer',
				});
			}
			if (this.chatFaq.MODULES.length > 0) {
				for (let i = 1; i <= this.chatFaq.MODULES.length; i++) {
					this.chatFaq.MODULES.pop();
				}
			}
			this.chatFaq.MODULES = this.selected;
			const chatFaqObj = JSON.parse(JSON.stringify(this.chatFaq));
			const faqId = this.route.snapshot.params['faqId'];
			if (faqId !== 'new') {
				this.channelsService.putFaqs(faqId, chatFaqObj).subscribe(
					(data: any) => {
						this.successMessage = this.translateService.instant(
							'UPDATED_SUCCESSFULLY'
						);
						this.router.navigate([`company-settings`, `chat-faqs`]);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			} else {
				this.channelsService.postFaqs(chatFaqObj).subscribe(
					(data: any) => {
						this.successMessage = this.translateService.instant(
							'SAVED_SUCCESSFULLY'
						);
						this.router.navigate([`company-settings`, `chat-faqs`]);
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
			}
		}
	}
	get formArr() {
		return this.answersForm.get('itemRows') as FormArray;
	}

	public initItemRows() {
		return this._fb.group({
			itemName: [''],
		});
	}

	public addNewRow() {
		this.formArr.push(this.initItemRows());
	}

	public deleteRow(index: number) {
		this.formArr.removeAt(index);
		this.chatFaq.ANSWERS.splice(index, 1);
	}

	get questionArray() {
		return this.questionsForm.get('itemRows') as FormArray;
	}
	public addNewQuestion() {
		this.questionArray.push(this.initItemRows());
	}

	public deleteQuestion(index: number) {
		this.questionArray.removeAt(index);
		this.chatFaq.QUESTIONS.splice(index, 1);
	}
}
