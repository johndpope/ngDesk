import {
	AfterViewInit,
	ChangeDetectorRef,
	Component,
	ElementRef,
	OnDestroy,
	OnInit,
	ViewChild,
	Inject,
} from '@angular/core';

import { NgForm } from '@angular/forms';

declare var Stripe: stripe.StripeStatic;

import { TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

import { PaymentApiService } from '@ngdesk/payment-api';
import { ConfigService } from '@src/app/config.service';

@Component({
	selector: 'app-payment',
	templateUrl: './payment.component.html',
	styleUrls: ['./payment.component.scss'],
})
export class PaymentComponent implements OnInit, AfterViewInit {
	public stripe;

	@ViewChild('cardInfo', { static: false }) public cardInfo: ElementRef;
	public error: any;
	public card: any;
	public plan: any;
	public noOfUsers = 1;
	private isOpenPremise = false;
	private isOpenEnterprise = false;
	private estimatedPay = 0;
	public subscritionStatus = 'INACTIVE';
	public isLoading = false;

	constructor(
		private bannerMessageService: BannerMessageService,
		private translateService: TranslateService,
		private paymentApiService: PaymentApiService,
		private configService: ConfigService
	) {}
	public ngOnInit() {
		this.stripe = Stripe(this.configService.getConfig().stripePublicKey);

		this.paymentApiService.getSubscription().subscribe(
			(response: any) => {
				this.subscritionStatus = response.STATUS;
				this.plan = response.PLAN;
				this.noOfUsers = response.NUMBER_OF_AGENTS;
				this.calculateBill();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public ngAfterViewInit() {
		const style = {
			base: {
				lineHeight: '24px',
				fontFamily: 'monospace',
				fontSmoothing: 'antialiased',
				fontSize: '16px',
				'::placeholder': {
					color: 'gray',
				},
			},
		};

		this.card = this.stripe.elements().create('card', { style });
		this.card.mount(this.cardInfo.nativeElement);
	}
	public ngOnDestroy() {
		this.card.destroy();
	}

	async onSubmit(form: NgForm) {
		if (!this.plan) {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant('SELECT_PLAN'),
			});
		} else {
			this.isLoading = true;
			this.stripe
				.createPaymentMethod({
					type: 'card',
					card: this.card,
				})
				.then((result) => {
					if (result.error) {
						this.bannerMessageService.errorNotifications.push({
							message: result.error,
						});
					} else {
						this.paymentApiService
							.postSubscription(
								this.plan,
								result.paymentMethod.id,
								this.noOfUsers
							)
							.subscribe(
								(response: any) => {
									this.subscritionStatus = response.STATUS;
									this.noOfUsers = response.NUMBER_OF_AGENTS;
									this.plan = response.PLAN;
									this.calculateBill();
									this.isLoading = false;
								},
								(error: any) => {
									this.isLoading = false;
									this.bannerMessageService.errorNotifications.push({
										message: error.error.ERROR,
									});
								}
							);
					}
				});
		}
	}

	public toggleSelected(planSelected) {
		this.plan = planSelected;
		this.calculateBill();
	}

	public calculateBill() {
		const teamCharge = 4;
		const enterpriseCharge = 7;
		if (this.plan === 'Team') {
			this.estimatedPay = teamCharge * this.noOfUsers;
		} else if (this.plan === 'Enterprise') {
			this.estimatedPay = enterpriseCharge * this.noOfUsers;
		}
	}
}
