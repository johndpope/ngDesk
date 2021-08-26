import { Overlay, OverlayConfig, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';
import { ComponentRef, ElementRef, Injectable, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { WalkthroughDialogComponent } from '../../dialogs/walkthrough-dialog/walkthrough-dialog.component';
import { UsersService } from '../../users/users.service';
import { FilePreviewOverlayRef } from './file-preview-overlay-ref';
import { FILE_PREVIEW_DIALOG_DATA } from './file-preview-overlay.tokens';
import { CompaniesService } from 'src/app/companies/companies.service';

export interface PopupData {
	element: string;
	title: string;
	description: string;
	progress: string;
	buttonText: string;
	arrowDirection: string;
}

export interface FilePreviewDialogConfig {
	panelClass?: string;
	hasBackdrop?: boolean;
	backdropClass?: string;
	data?: PopupData;
}

export interface ShowMoreData {
	MODULE: string;
	DIALOG_TITLE: string;
	DIALOG_DESC: string;
	FIRST_STEP_ELEMENT: string;
	API_KEY: string;
}

const DEFAULT_CONFIG: FilePreviewDialogConfig = {
	hasBackdrop: true,
	backdropClass: 'dark-backdrop',
	panelClass: 'tm-file-preview-dialog-panel',
	data: null,
};

@Injectable({
	providedIn: 'root',
})
export class FilePreviewOverlayService {
	constructor(
		private overlay: Overlay,
		private injector: Injector,
		private usersService: UsersService,
		private translateService: TranslateService
	) {}
	public hostElement = '';
	private ticketsWalkthroughData: PopupData[] = [
		{
			element: 'layouts-button',
			title: this.translateService.instant('LAYOUTS_BUTTON_TITLE'),
			description: this.translateService.instant('LAYOUTS_BUTTON_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_1_OF_4'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'top-left',
		},
		{
			element: 'layouts-sidebar',
			title: this.translateService.instant('LAYOUTS_SIDEBAR_TITLE'),
			description: this.translateService.instant('LAYOUTS_SIDEBAR_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_2_OF_4'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'left',
		},
		{
			element: 'custom-button',
			title: this.translateService.instant('NEW_BUTTON_TITLE'),
			description: this.translateService.instant('NEW_BUTTON_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_3_OF_4'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'top-center',
		},
		{
			element: 'layout-table',
			title: this.translateService.instant('EMAIL_TICKET_TITLE'),
			description: this.translateService.instant('EMAIL_TICKET_DESC', {
				supportEmail: `support@${this.usersService.getSubdomain()}.ngdesk.com`,
			}),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_4_OF_4'),
			buttonText: this.translateService.instant('CLOSE'),
			arrowDirection: 'top-center',
		},
		{
			element: 'status-dropdown',
			title: this.translateService.instant('STATUS_TITLE'),
			description: this.translateService.instant('STATUS_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_1_OF_8'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'left',
		},
		{
			element: 'assignee-dropdown',
			title: this.translateService.instant('ASSIGNEE_TITLE'),
			description: this.translateService.instant('ASSIGNEE_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_2_OF_8'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'left',
		},
		{
			element: 'subject-heading',
			title: this.translateService.instant('SUBJECT_TITLE'),
			description: this.translateService.instant('SUBJECT_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_3_OF_8'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'top-center',
		},
		{
			element: 'ticket-conversation-list',
			title: this.translateService.instant('MESSAGES_TITLE'),
			description: this.translateService.instant('MESSAGES_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_4_OF_8'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'bottom',
		},
		{
			element: 'ticket-submit-message-area',
			title: this.translateService.instant('SEND_MESSAGE_TITLE'),
			description: this.translateService.instant('SEND_MESSAGE_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_5_OF_8'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'right',
		},
		{
			element: 'private-message-switch',
			title: this.translateService.instant('PRIVATE_MESSAGE_TITLE'),
			description: this.translateService.instant('PRIVATE_MESSAGE_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_6_OF_8'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'right',
		},
		{
			element: 'time-spent-input',
			title: this.translateService.instant('TIME_SPENT_INPUT_TITLE'),
			description: this.translateService.instant('TIME_SPENT_INPUT_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_7_OF_8'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'left',
		},
		{
			element: 'save-ticket-action',
			title: this.translateService.instant('SAVE_TITLE'),
			description: this.translateService.instant('SAVE_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_8_OF_8'),
			buttonText: this.translateService.instant('CLOSE'),
			arrowDirection: 'right',
		},
	];

	private chatsWalkthroughData: PopupData[] = [
		{
			element: 'chat-messages',
			title: this.translateService.instant('CHAT_MESSAGES_TITLE'),
			description: this.translateService.instant('CHAT_MESSAGES_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_1_OF_5'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'top-center',
		},
		{
			element: 'chat-text-area',
			title: this.translateService.instant('CHAT_TEXT_AREA_TITLE'),
			description: this.translateService.instant('CHAT_TEXT_AREA_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_2_OF_5'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'bottom',
		},
		{
			element: 'chat-details-area',
			title: this.translateService.instant('CHAT_DETAILS_AREA_TITLE'),
			description: this.translateService.instant('CHAT_DETAILS_AREA_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_3_OF_5'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'right',
		},
		{
			element: 'chat-transfer-button',
			title: this.translateService.instant('CHAT_TRANSFER_BUTTON_TITLE'),
			description: this.translateService.instant('CHAT_TRANSFER_BUTTON_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_4_OF_5'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'top-center',
		},
		{
			element: 'pre-chat-survey-detail',
			title: this.translateService.instant('CHAT_PRECHAT_SURVEY_TITLE'),
			description: this.translateService.instant('CHAT_PRECHAT_SURVEY_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_5_OF_5'),
			buttonText: this.translateService.instant('DONE'),
			arrowDirection: 'left',
		},
		{
			element: 'chat-widgets-table',
			title: this.translateService.instant('CHAT_WIDGETS_TABLE_TITLE'),
			description: this.translateService.instant('CHAT_WIDGETS_TABLE_DESC'),
			progress: null,
			buttonText: null,
			arrowDirection: 'top-center',
		},
		{
			element: 'chat-script',
			title: this.translateService.instant('CHAT_SCRIPT_TITLE'),
			description: this.translateService.instant('CHAT_SCRIPT_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_1_OF_7'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'top-center',
		},
		{
			element: 'chat-plugin',
			title: this.translateService.instant('CHAT_PLUGIN_TITLE'),
			description: this.translateService.instant('CHAT_PLUGIN_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_2_OF_7'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'top-center',
		},
		{
			element: 'chat-email-developers',
			title: this.translateService.instant('CHAT_EMAIL_DEVELOPERS_TITLE'),
			description: this.translateService.instant('CHAT_EMAIL_DEVELOPERS_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_3_OF_7'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'left',
		},
		{
			element: 'chat-customize-details',
			title: this.translateService.instant('CHAT_CUSTOMIZE_DETAILS_TITLE'),
			description: this.translateService.instant('CHAT_CUSTOMIZE_DETAILS_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_4_OF_7'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'left',
		},
		{
			element: 'pre-chat-survey',
			title: this.translateService.instant('CHAT_PRECHAT_SURVEY_TITLE'),
			description: this.translateService.instant('CHAT_PRECHAT_SURVEY_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_5_OF_7'),
			buttonText: this.translateService.instant('CLOSE'),
			arrowDirection: 'left',
		},
		{
			element: 'chat-business-rules',
			title: this.translateService.instant('CHAT_BUSINESS_RULES_TITLE'),
			description: this.translateService.instant('CHAT_BUSINESS_RULES_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_6_OF_7'),
			buttonText: this.translateService.instant('NEXT'),
			arrowDirection: 'left',
		},
		{
			element: 'chat-prompts-details',
			title: this.translateService.instant('CHAT_PROMPTS_DETAILS_TITLE'),
			description: this.translateService.instant('CHAT_PROMPTS_DETAILS_DESC'),
			progress: this.translateService.instant('WALKTHROUGH_PROGRESS_7_OF_7'),
			buttonText: this.translateService.instant('DONE'),
			arrowDirection: 'right',
		},
		{
			element: 'chat-status',
			title: this.translateService.instant('CHAT_STATUS_TITLE'),
			description: this.translateService.instant('CHAT_STATUS_DESC'),
			progress: this.translateService.instant('CHAT_GETTING_STARTED_STATUS'),
			buttonText: this.translateService.instant('DONE'),
			arrowDirection: 'top-right',
		},
		{
			element: 'chat-widget',
			title: this.translateService.instant('CHAT_WIDGET'),
			description: this.translateService.instant('CHAT_WIDGET_DESC'),
			progress: this.translateService.instant('CHAT_GETTING_STARTED_WIDGET'),
			buttonText: this.translateService.instant('DONE'),
			arrowDirection: 'right',
		},
		// {
		// 	element: 'chat-operating-hours-switch',
		// 	title: this.translateService.instant('CHAT_OPERATING_HOURS_SWITCH_TITLE'),
		// 	description: this.translateService.instant(
		// 		'CHAT_OPERATING_HOURS_SWITCH_DESC'
		// 	),
		// 	progress: this.translateService.instant('WALKTHROUGH_PROGRESS_7_OF_8'),
		// 	buttonText: this.translateService.instant('NEXT'),
		// 	arrowDirection: 'left'
		// },
		// {
		// 	element: 'chatbot-settings',
		// 	title: this.translateService.instant('CHATBOT_SETTINGS_TITLE'),
		// 	description: this.translateService.instant('CHATBOT_SETTINGS_DESC'),
		// 	progress: this.translateService.instant('WALKTHROUGH_PROGRESS_8_OF_8'),
		// 	buttonText: this.translateService.instant('DONE'),
		// 	arrowDirection: 'left'
		// }
	];

	private showMoreData: ShowMoreData[] = [
		{
			MODULE: 'Chat',
			DIALOG_TITLE: 'LEARN_MORE_CHAT_DETAIL_TITLE',
			DIALOG_DESC: 'LEARN_MORE_CHAT_DETAIL_DESCRIPTION',
			FIRST_STEP_ELEMENT: 'chat-messages',
			API_KEY: 'CHAT_DETAIL',
		},
		{
			MODULE: 'Tickets',
			DIALOG_TITLE: 'LEARN_MORE_TICKETS_DETAIL_TITLE',
			DIALOG_DESC: 'LEARN_MORE_TICKETS_DETAIL_DESCRIPTION',
			FIRST_STEP_ELEMENT: 'status-dropdown',
			API_KEY: 'TICKETS_DETAIL',
		},
	];

	public getWalkthroughData(module: string, element: string): PopupData {
		if (module === 'Chat') {
			return this.chatsWalkthroughData.find((item) => item.element === element);
		} else if (module === 'Ticket' || module === 'Tickets') {
			return this.ticketsWalkthroughData.find(
				(item) => item.element === element
			);
		}
	}

	public getAllWalkthroughData(module: string): PopupData[] {
		if (module === 'Chat') {
			return this.chatsWalkthroughData;
		} else if (module === 'Ticket' || module === 'Tickets') {
			return this.ticketsWalkthroughData;
		}
	}

	public getShowMoreData(moduleName: string): ShowMoreData {
		return this.showMoreData.find((data) => data.MODULE === moduleName);
	}

	private getOverlayConfig(
		config: FilePreviewDialogConfig,
		host: ElementRef
	): OverlayConfig {
		const positionStrategy = this.createPositionStrategy(host);

		const overlayConfig = new OverlayConfig({
			hasBackdrop: config.hasBackdrop,
			backdropClass: config.backdropClass,
			panelClass: config.panelClass,
			scrollStrategy: this.overlay.scrollStrategies.block(),
			positionStrategy,
		});

		return overlayConfig;
	}

	private createOverlay(config: FilePreviewDialogConfig, host: ElementRef) {
		// Returns an OverlayConfig
		const overlayConfig = this.getOverlayConfig(config, host);

		// Returns an OverlayRef
		return this.overlay.create(overlayConfig);
	}

	private createInjector(
		config: FilePreviewDialogConfig,
		dialogRef: FilePreviewOverlayRef
	): PortalInjector {
		// Instantiate new WeakMap for our custom injection tokens
		const injectionTokens = new WeakMap();

		// Set custom injection tokens
		injectionTokens.set(FilePreviewOverlayRef, dialogRef);
		injectionTokens.set(FILE_PREVIEW_DIALOG_DATA, config.data);

		// Instantiate new PortalInjector
		return new PortalInjector(this.injector, injectionTokens);
	}

	private attachModalContainer(
		overlayRef: OverlayRef,
		config: FilePreviewDialogConfig,
		dialogRef: FilePreviewOverlayRef
	) {
		const injector = this.createInjector(config, dialogRef);
		const containerPortal = new ComponentPortal(
			WalkthroughDialogComponent,
			null,
			injector
		);
		const containerRef: ComponentRef<WalkthroughDialogComponent> = overlayRef.attach(
			containerPortal
		);
		return containerRef.instance;
	}

	private createPositionStrategy(host) {
		const positions = [];
		host = host.id ? host : host.nativeElement;
		switch (host.id) {
			case 'layouts-button': {
				positions.push({
					originX: 'start',
					originY: 'bottom',
					overlayX: 'start',
					overlayY: 'bottom',
					offsetY: host.offsetWidth + 20,
					offsetX: -5,
				});
				break;
			}
			case 'layouts-sidebar': {
				positions.push({
					originX: 'start',
					originY: 'top',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 10,
					offsetX: host.offsetWidth + 36,
				});
				break;
			}
			case 'custom-button': {
				positions.push({
					originX: 'center',
					originY: 'bottom',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: host.offsetHeight,
					offsetX: 0,
				});
				break;
			}
			case 'layout-table': {
				positions.push({
					originX: 'center',
					originY: 'bottom',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: -50,
					offsetX: 0,
				});
				break;
			}
			case 'pre-chat-survey': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 30,
				});
				break;
			}
			case 'pre-chat-survey-detail': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 30,
				});
				break;
			}
			case 'chat-messages': {
				positions.push({
					originX: 'center',
					originY: 'bottom',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: 30,
					offsetX: 0,
				});
				break;
			}
			case 'chat-text-area': {
				positions.push({
					originX: 'center',
					originY: 'top',
					overlayX: 'center',
					overlayY: 'bottom',
					offsetY: -30,
					offsetX: 0,
				});
				break;
			}
			case 'chat-details-area': {
				positions.push({
					originX: 'start',
					originY: 'top',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 45,
					offsetX: -30,
				});
				break;
			}
			case 'chat-transfer-button': {
				positions.push({
					originX: 'center',
					originY: 'bottom',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: 30,
					offsetX: 0,
				});
				break;
			}
			case 'chat-widgets-table': {
				positions.push({
					originX: 'center',
					originY: 'bottom',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: 40,
					offsetX: 0,
				});
				break;
			}
			case 'status-dropdown': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 36,
				});
				break;
			}
			case 'assignee-dropdown': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 36,
				});
				break;
			}
			case 'subject-heading': {
				positions.push({
					originX: 'center',
					originY: 'bottom',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: 36,
					offsetX: 0,
				});
				break;
			}
			case 'ticket-conversation-list': {
				positions.push({
					originX: 'center',
					originY: 'top',
					overlayX: 'center',
					overlayY: 'bottom',
					offsetY: -36,
					offsetX: 0,
				});
				break;
			}
			case 'ticket-submit-message-area': {
				positions.push({
					originX: 'start',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: -36,
				});
				break;
			}
			case 'private-message-switch': {
				positions.push({
					originX: 'start',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: -36,
				});
				break;
			}
			case 'time-spent-input': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 36,
				});
				break;
			}
			case 'save-ticket-action': {
				positions.push({
					originX: 'start',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: -36,
				});
				break;
			}
			case 'chat-script': {
				positions.push({
					originX: 'center',
					originY: 'center',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: 30,
					offsetX: 0,
				});
				break;
			}
			case 'chat-plugin': {
				positions.push({
					originX: 'center',
					originY: 'center',
					overlayX: 'center',
					overlayY: 'top',
					offsetY: 30,
					offsetX: 0,
				});
				break;
			}
			case 'chat-email-developers': {
				positions.push({
					originX: 'center',
					originY: 'bottom',
					overlayX: 'start',
					overlayY: 'bottom',
					offsetY: 30,
					offsetX: 0,
				});
				break;
			}
			case 'chat-customize-details': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 0,
				});
				break;
			}
			case 'chat-prechat-survey': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 0,
				});
				break;
			}
			case 'chat-business-rules': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 0,
				});
				break;
			}
			case 'chat-preview-area': {
				positions.push({
					originX: 'start',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: -36,
				});
				break;
			}
			case 'chat-prompts-details': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'end',
					overlayY: 'center',
					offsetY: 0,
					offsetX: -100,
				});
				break;
			}
			case 'chat-operating-hours-switch': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 45,
				});
				break;
			}
			case 'chatbot-settings': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 0,
					offsetX: 45,
				});
				break;
			}
			case 'chat-status': {
				positions.push({
					originX: 'start',
					originY: 'bottom',
					overlayX: 'start',
					overlayY: 'bottom',
					offsetY: 0,
					offsetX: 300,
				});
				break;
			}
			case 'chat-widget': {
				positions.push({
					originX: 'end',
					originY: 'center',
					overlayX: 'start',
					overlayY: 'center',
					offsetY: 600,
					offsetX: 650,
				});
				break;
			}
		}

		return this.overlay
			.position()
			.flexibleConnectedTo(host)
			.withPositions(positions);
	}

	public open(data: any, host: ElementRef) {
		this.hostElement = host['id'] ? host['id'] : host.nativeElement.id;

		const config: FilePreviewDialogConfig = {
			data: data,
		};
		// Override default configuration
		const dialogConfig = {
			...DEFAULT_CONFIG,
			...config,
		};

		// Returns an OverlayRef which is a PortalHost
		const overlayRef = this.createOverlay(dialogConfig, host);

		// Instantiate remote control
		const dialogRef = new FilePreviewOverlayRef(overlayRef);

		// return overlay component
		const overlayComponent = this.attachModalContainer(
			overlayRef,
			dialogConfig,
			dialogRef
		);

		return {
			dialogRef,
			overlayComponent,
		};
	}
}
