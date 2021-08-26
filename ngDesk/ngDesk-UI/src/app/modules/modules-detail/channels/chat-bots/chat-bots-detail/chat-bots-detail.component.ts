import { animate, style, transition, trigger } from '@angular/animations';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import {
	Component,
	OnInit,
	Pipe,
	PipeTransform,
	ViewChild,
	ElementRef,
} from '@angular/core';
import {
	FormArray,
	FormBuilder,
	FormControl,
	FormGroup,
	Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { AppGlobals } from 'src/app/app.globals';
import { CompaniesService } from '../../../../../companies/companies.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { ModulesService } from '../../../../modules.service';
import { from, BehaviorSubject } from 'rxjs';
import { UsersService } from 'src/app/users/users.service';
import { map } from 'rxjs/operators';
import { stringToKeyValue } from '@angular/flex-layout/extended/typings/style/style-transforms';

@Component({
	selector: 'app-chat-bots-detail',
	templateUrl: './chat-bots-detail.component.html',
	styleUrls: ['./chat-bots-detail.component.scss'],
	animations: [
		trigger('inOutAnimation', [
			transition(':leave', [
				style({ transform: 'translateX(100%)', opacity: 0 }),
				animate('500ms', style({ transform: 'translateX(0)', opacity: 1 })),
			]),
			transition(':enter', [
				style({ transform: 'translateX(0)', opacity: 1 }),
				animate('500ms', style({ transform: 'translateX(100%)', opacity: 0 })),
			]),
		]),
	],
})
export class ChatBotsDetailComponent implements OnInit {
	public transform = 100;
	public chatBotForm: FormGroup;
	public showSidebar = true;
	public showNodeDetail = false;
	public nodeSelected: any = {};
	public additionalOptionsOpen = false;
	public currentNodeQuestion = '';
	public disableTextInput = false;
	public currentNodeName = '';
	public showEditableLabel = false;
	public currentNodeMapping = '';
	public dropPosition = '';
	public nodeCount = 0;
	public buttonOption = '';
	public buttonOptions = [];
	private fromNode: any = {};
	private fromOption: number;
	private nodeSettings: any = {};
	public nodeInFocus = '';
	public sidebarState = 'add';
	public isValidNodeName = true;
	public module: any = {};
	public chatMessages = [];
	public selectedOption;
	public connectedId;
	public chatBot: any = {
		NAME: '',
		DESCRIPTION: '',
		WORKFLOW: {
			NODES: [],
		},
	};

	public botIcons = {
		Email: 'email_outline',
		Phone: 'smartphone_outline',
		Name: 'account_circle_outline',
		Buttons: 'done_all_outline',
		FreeText: 'textsms_outline',
		SendMessage: 'message_outline',
		TransferToAgent: 'people_outline',
	};

	// This is an array of all available nodes
	public availableBotNodes = [
		{
			NAME: 'Email Address',
			TYPE: 'QuestionNode',
			SUB_TYPE: 'Email',
		},
		{
			NAME: 'Phone Number',
			TYPE: 'QuestionNode',
			SUB_TYPE: 'Phone',
		},
		{
			NAME: "User's Name",
			TYPE: 'QuestionNode',
			SUB_TYPE: 'Name',
		},
		{
			NAME: 'Free Text Reply',
			TYPE: 'QuestionNode',
			SUB_TYPE: 'FreeText',
		},
		{
			NAME: 'Multiple Choice',
			TYPE: 'QuestionNode',
			SUB_TYPE: 'Buttons',
		},
		{
			NAME: 'Send Message',
			TYPE: 'SendMessage',
			SUB_TYPE: 'SendMessage',
		},
		{
			NAME: 'Transfer To Agent',
			TYPE: 'TransferToAgent',
			SUB_TYPE: 'TransferToAgent',
		},
	];

	@ViewChild('scrollMe', {
		read: ElementRef,
	})
	private chatScrollContainer: ElementRef;

	public boxes: any = [];
	public showType: string;
	public chatMessages$: BehaviorSubject<any[]> = new BehaviorSubject<any[]>([]);
	public subdomain: string;
	public messageMap = new Map<string, {}>();
	public parentMap = new Map<String, Map<String, String>>();
	public messageTextBox: ElementRef;
	public globalNodeId: any;
	public submitMessage: string;
	public buttonMessageArr: any[] = [];
	public emailPattern = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$/;
	public agentAssignedTime;
	errorExists: boolean;
	sendMessageDisabled: boolean;
	public currentIndex = 0;
	constructor(
		private formBuilder: FormBuilder,
		private globals: AppGlobals,
		private route: ActivatedRoute,
		private router: Router,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		private companiesService: CompaniesService,
		private userService: UsersService,
		private translateService: TranslateService
	) {}

	public ngOnInit() {
		this.showType = 'Builder';
		this.subdomain = this.userService.getSubdomain();
		this.chatBotForm = this.formBuilder.group({});
		const chatBotId = this.route.snapshot.params['chatBotId'];
		const moduleId = this.route.snapshot.params['moduleId'];
		const name = this.modulesService.name;
		this.modulesService.getModuleById(moduleId).subscribe(
			(response: any) => {
				this.module = response;

				if (chatBotId !== 'new') {
					this.modulesService.getChatBot(moduleId, chatBotId).subscribe(
						(chatBotResponse: any) => {
							this.modulesService.name = undefined;
							console.log(this.modulesService.name);
							this.chatBot = chatBotResponse;
							this.chatBotForm.controls['NAME'].setValue(chatBotResponse.NAME);
							this.chatBotForm.controls['DESCRIPTION'].setValue(
								chatBotResponse.DESCRIPTION
							);
							this.chatBot.WORKFLOW.NODES.forEach((node: any) => {
								this.nodeSettings[node.ID] = {
									SHOW_REMOVE: false,
									SHOW_ADD_NODE: false,
									SHOW_CHILDREN: true,
									SHOW_NODE: true,
								};
							});
							this.resetContainer();
							this.showChildren(this.chatBot.WORKFLOW.NODES[0]);
							this.modulesService.name = undefined;
						},
						(chatBotError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: chatBotError.error.ERROR,
							});
						}
					);
				} else if (name != undefined && name != 'new') {
					this.modulesService.getChatBotTemplate(moduleId).subscribe(
						(chatBotResponse: any) => {
							this.modulesService.name = undefined;
							this.chatBot = chatBotResponse;
							this.chatBotForm.controls['NAME'].setValue(chatBotResponse.NAME);
							this.chatBotForm.controls['DESCRIPTION'].setValue(
								chatBotResponse.DESCRIPTION
							);
							this.chatBot.WORKFLOW.NODES.forEach((node: any) => {
								this.nodeSettings[node.ID] = {
									SHOW_REMOVE: false,
									SHOW_ADD_NODE: false,
									SHOW_CHILDREN: true,
									SHOW_NODE: true,
								};
							});
							this.resetContainer();
							this.showChildren(this.chatBot.WORKFLOW.NODES[0]);
							this.modulesService.name = undefined;
						},
						(chatBotError: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: chatBotError.error.ERROR,
							});
						}
					);
				} else {
					this.chatBot.WORKFLOW.NODES.push({
						TYPE: 'Droppable',
						POSITION_X: 30,
						POSITION_Y: 100,
					});
				}

				if (this.module.FIELDS && this.module.FIELDS.length > 0) {
					this.module.FIELDS.sort((field1, field2) =>
						field1.DISPLAY_LABEL.localeCompare(field2.DISPLAY_LABEL)
					);
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	public zoomOut() {
		if (this.transform > 30) {
			this.transform -= 10;
		}
	}

	public zoomIn() {
		if (this.transform < 150) {
			this.transform += 10;
		}
	}

	public toggleSidebar(
		nodeFrom,
		index,
		position,
		optionIndex?: number,
		event?
	) {
		this.dropPosition = index + '-' + position;
		this.sidebarState = 'add';
		this.currentNodeQuestion = '';
		this.disableTextInput = false;
		this.nodeSettings[nodeFrom.ID] = {
			SHOW_REMOVE: false,
			SHOW_ADD_NODE: false,
			SHOW_CHILDREN: true,
			SHOW_NODE: true,
		};
		if (this.showNodeDetail) {
			this.showNodeDetail = !this.showNodeDetail;
		}
		if (!this.showSidebar) {
			this.showSidebar = !this.showSidebar;
		} else {
			let keepGoing = true;
			let removedDroppable = false;
			this.chatBot.WORKFLOW.NODES.forEach((node, nodeIndex) => {
				if (keepGoing) {
					if (node.TYPE === 'Droppable') {
						this.chatBot.WORKFLOW.NODES.splice(nodeIndex, 1);
						keepGoing = false;
						removedDroppable = true;
					}
				}
			});
			if (removedDroppable) {
				if (this.fromNode.SUB_TYPE === 'Buttons') {
					if (
						this.fromNode.VALUES.OPTIONS[this.fromOption].CONNECTS_TO.length > 0
					) {
						const childNode = this.chatBot.WORKFLOW.NODES.find(
							(node) =>
								node.ID ===
								this.fromNode.VALUES.OPTIONS[this.fromOption].CONNECTS_TO
						);
						// 100: height of gap to next div, 90: height of div, 35 height of options, 15 height of gap to option
						const heightToDecrease = 100 + 90 + 35 + 15;
						this.decreaseHeightOfAllChildNodes(childNode, heightToDecrease);
					}
				} else {
					if (this.fromNode.CONNECTS_TO.trim().length > 0) {
						const childNode = this.chatBot.WORKFLOW.NODES.find(
							(node) => node.ID === this.fromNode.CONNECTS_TO
						);
						// 100: height of gap to next div, 90: height of div
						const heightToDecrease = 100 + 90;
						this.decreaseHeightOfAllChildNodes(childNode, heightToDecrease);
					}
				}
			}
		}

		if (position === 'before') {
		} else {
			this.fromNode = nodeFrom;
			if (nodeFrom.SUB_TYPE !== 'Buttons') {
				const droppableArea: any = {
					POSITION_X: nodeFrom.POSITION_X,
					TYPE: 'Droppable',
				};
				droppableArea.POSITION_Y = nodeFrom.POSITION_Y + 90 + 100;
				if (this.fromNode.CONNECTS_TO.trim().length !== 0) {
					// IN BETWEEN, INCREASE HEIGHT OF ALL CONNECTED BOTTOM NODES
					this.increaseHeightOfAllChildNodes(this.fromNode);
				}
				this.chatBot.WORKFLOW.NODES.splice(index + 1, 0, droppableArea);
			} else {
				// Increase Height of all child nodes
				this.fromOption = optionIndex;
				const connectsTo =
					this.fromNode.VALUES.OPTIONS[optionIndex].CONNECTS_TO;
				if (connectsTo.trim().length > 0) {
					const nextNode = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === connectsTo
					);
					this.increaseHeightOfAllChildNodes(nextNode);
				}
				const posX = nodeFrom.POSITION_X + (125 + 100) * optionIndex;
				const posY = nodeFrom.POSITION_Y + 90 + 15 + 35 + 100;

				const droppableArea: any = {
					TYPE: 'Droppable',
					POSITION_X: posX,
					POSITION_Y: posY,
				};

				this.dropPosition = index + '.' + optionIndex + '-after';
				this.chatBot.WORKFLOW.NODES.splice(index + 1, 0, droppableArea);
			}
		}
	}

	public addToButtons() {
		this.buttonOptions.push({
			OPTION: this.buttonOption,
			CONNECTS_TO: '',
			OPTION_ID: this.globals.guid(),
		});
		this.buttonOption = '';
	}

	public removeOption(optionIndex) {
		this.buttonOptions.splice(optionIndex, 1);
	}

	public toggleNodesDetail(node) {
		this.nodeSelected = JSON.parse(JSON.stringify(node));
		this.showEditableLabel = false;
		this.additionalOptionsOpen = false;
		this.buttonOptions = [];
		this.buttonOption = '';

		// Check to find whether the node was clicked on sidebar or layout
		if (!node.POSITION_X) {
			this.currentNodeName = this.nodeCount + 1 + '. ' + node.NAME;
			this.currentNodeMapping = '';
		}
		this.showNodeDetail = true;
	}

	public sortOptions(event: CdkDragDrop<string[]>) {
		moveItemInArray(
			this.buttonOptions,
			event.previousIndex,
			event.currentIndex
		);
	}

	public addNode() {
		const fromIndex = this.chatBot.WORKFLOW.NODES.indexOf(this.fromNode);
		if (this.chatBot.WORKFLOW.NODES.length > 1) {
			if (
				this.nodeSelected.TYPE === 'TransferToAgent' &&
				this.fromNode.SUB_TYPE === 'Buttons' &&
				this.chatBot.WORKFLOW.NODES[fromIndex].VALUES.OPTIONS[this.fromOption]
					.CONNECTS_TO !== ''
			) {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant(
						'TRANSFER_TO_AGENT_IN_BETWEEN'
					),
				});
				return;
			} else if (
				this.nodeSelected.TYPE === 'TransferToAgent' &&
				this.fromNode.SUB_TYPE !== 'Buttons' &&
				this.chatBot.WORKFLOW.NODES[fromIndex].CONNECTS_TO !== ''
			) {
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant(
						'TRANSFER_TO_AGENT_IN_BETWEEN'
					),
				});
				return;
			}
		}
		const newNode = JSON.parse(JSON.stringify(this.nodeSelected));
		let droppableArea: any = {};
		let indexToInsert = 0;
		let keepGoing = true;
		this.chatBot.WORKFLOW.NODES.forEach((node, index) => {
			if (keepGoing) {
				if (node.TYPE === 'Droppable') {
					droppableArea = this.chatBot.WORKFLOW.NODES[index];
					this.chatBot.WORKFLOW.NODES.splice(index, 1);
					indexToInsert = index;
					keepGoing = false;
				}
			}
		});
		newNode.POSITION_X = droppableArea.POSITION_X;
		newNode.POSITION_Y = droppableArea.POSITION_Y;
		newNode.ID = this.globals.guid();
		newNode.CONNECTS_TO = '';
		newNode.VALUES = {
			MESSAGE: this.currentNodeQuestion,
			MAPPING: this.currentNodeMapping,
			DISABLE_TEXT_INPUT: this.disableTextInput,
		};

		if (newNode.SUB_TYPE === 'Buttons') {
			newNode.VALUES.OPTIONS = JSON.parse(JSON.stringify(this.buttonOptions));
		}
		newNode.NAME = this.currentNodeName;
		this.nodeSettings[newNode.ID] = {
			SHOW_REMOVE: false,
			SHOW_ADD_NODE: false,
			SHOW_CHILDREN: true,
			SHOW_NODE: true,
		};

		this.chatBot.WORKFLOW.NODES.splice(indexToInsert, 0, newNode);
		this.nodeCount++;
		const length = this.chatBot.WORKFLOW.NODES.length;
		if (length > 1) {
			if (this.fromNode.SUB_TYPE === 'Buttons') {
				if (newNode.SUB_TYPE === 'Buttons') {
					this.chatBot.WORKFLOW.NODES[
						indexToInsert
					].VALUES.OPTIONS[0].CONNECTS_TO =
						this.chatBot.WORKFLOW.NODES[fromIndex].VALUES.OPTIONS[
							this.fromOption
						].CONNECTS_TO;
				} else {
					this.chatBot.WORKFLOW.NODES[indexToInsert].CONNECTS_TO =
						this.chatBot.WORKFLOW.NODES[fromIndex].VALUES.OPTIONS[
							this.fromOption
						].CONNECTS_TO;
				}

				this.chatBot.WORKFLOW.NODES[fromIndex].VALUES.OPTIONS[
					this.fromOption
				].CONNECTS_TO = newNode.ID;
			} else {
				if (newNode.SUB_TYPE === 'Buttons') {
					this.chatBot.WORKFLOW.NODES[
						indexToInsert
					].VALUES.OPTIONS[0].CONNECTS_TO =
						this.chatBot.WORKFLOW.NODES[fromIndex].CONNECTS_TO;
				} else {
					this.chatBot.WORKFLOW.NODES[indexToInsert].CONNECTS_TO =
						this.chatBot.WORKFLOW.NODES[fromIndex].CONNECTS_TO;
				}
				this.chatBot.WORKFLOW.NODES[fromIndex].CONNECTS_TO = newNode.ID;
			}
			if (
				newNode.SUB_TYPE === 'Buttons' &&
				this.chatBot.WORKFLOW.NODES[indexToInsert].VALUES.OPTIONS[0].CONNECTS_TO
					.length > 0
			) {
				this.increaseHeightOfAllChildNodesAddedInBetween(newNode);
			}
		}

		this.nodeSettings[newNode.ID].SHOW_CHILDREN = true;
		this.showAllParents(newNode);
		this.showChildren(newNode);

		this.showSidebar = false;
		this.showNodeDetail = false;
		this.nodeInFocus = newNode.ID;
		this.currentNodeQuestion = '';
		this.disableTextInput = false;
		this.currentNodeName = '';
		this.currentNodeMapping = '';
		this.dropPosition = '';
		this.sidebarState = '';
		this.buttonOptions = [];
		this.buttonOption = '';
		this.fromNode = {};
		this.fromOption = -1;
	}

	public resetContainer() {
		let resetContainer = true;
		if (this.chatBot.WORKFLOW.NODES.length === 1) {
			if (this.chatBot.WORKFLOW.NODES[0].TYPE === 'Droppable') {
				resetContainer = false;
			}
		}

		if (
			this.showSidebar &&
			this.chatBot.WORKFLOW.NODES.length > 0 &&
			resetContainer
		) {
			let keepGoing = true;
			let removedDroppable = false;
			this.chatBot.WORKFLOW.NODES.forEach((node, index) => {
				if (keepGoing) {
					if (node.TYPE === 'Droppable') {
						this.chatBot.WORKFLOW.NODES.splice(index, 1);
						removedDroppable = true;
						keepGoing = false;
					}
				}
			});
			if (removedDroppable) {
				if (this.fromNode.SUB_TYPE === 'Buttons') {
					if (
						this.fromNode.VALUES.OPTIONS[this.fromOption].CONNECTS_TO.length > 0
					) {
						const childNode = this.chatBot.WORKFLOW.NODES.find(
							(node) =>
								node.ID ===
								this.fromNode.VALUES.OPTIONS[this.fromOption].CONNECTS_TO
						);
						// 100: height of gap to next div, 90: height of div, 35 height of options, 15 height of gap to option
						const heightToDecrease = 100 + 90 + 35 + 15;
						if (childNode) {
							this.decreaseHeightOfAllChildNodes(childNode, heightToDecrease);
						}
					}
				} else {
					if (this.fromNode.CONNECTS_TO.trim().length > 0) {
						const childNode = this.chatBot.WORKFLOW.NODES.find(
							(node) => node.ID === this.fromNode.CONNECTS_TO
						);
						// 100: height of gap to next div, 90: height of div, 35 height of options, 15 height of gap to option
						const heightToDecrease = 100 + 90;
						if (childNode) {
							this.decreaseHeightOfAllChildNodes(childNode, heightToDecrease);
						}
					}
				}
			}
			this.currentNodeQuestion = '';
			this.disableTextInput = false;
			this.currentNodeName = '';
			this.showNodeDetail = false;
			this.showSidebar = false;
			this.dropPosition = '';
			this.currentNodeMapping = '';
			this.sidebarState = '';
			this.buttonOptions = [];
			this.buttonOption = '';
			this.fromNode = {};
			this.fromOption = -1;
			this.nodeSelected = {};
		}
	}

	public toggleNodeFocus(node, state) {
		if (state === 'enter') {
			this.nodeSettings[node.ID].SHOW_ADD_NODE = true;
			this.nodeSettings[node.ID].SHOW_REMOVE = true;
		} else {
			this.nodeSettings[node.ID].SHOW_ADD_NODE = false;
			this.nodeSettings[node.ID].SHOW_REMOVE = false;
		}
	}

	public removeNode(index) {
		const removedNode = this.chatBot.WORKFLOW.NODES[index];
		this.chatBot.WORKFLOW.NODES.splice(index, 1);
		this.resetContainer();
		if (this.chatBot.WORKFLOW.NODES.length === 0) {
			this.chatBot.WORKFLOW.NODES.push({
				TYPE: 'Droppable',
				POSITION_X: 30,
				POSITION_Y: 100,
			});
			this.showSidebar = true;
			this.sidebarState = 'add';
		} else {
			// FIND PARENT NODE
			let parentNode: any = {};
			let parentOptionIndex = -1;
			let keepGoing = true;
			this.chatBot.WORKFLOW.NODES.forEach((node) => {
				if (keepGoing) {
					if (node.SUB_TYPE === 'Buttons') {
						for (let i = 0; i < node.VALUES.OPTIONS.length; i++) {
							const option = node.VALUES.OPTIONS[i];
							if (option.CONNECTS_TO === removedNode.ID) {
								parentNode = node;
								parentOptionIndex = i;
								keepGoing = false;
								break;
							}
						}
					} else {
						if (node.CONNECTS_TO === removedNode.ID) {
							parentNode = node;
							keepGoing = false;
						}
					}
				}
			});

			if (removedNode.SUB_TYPE === 'Buttons') {
				removedNode.VALUES.OPTIONS.forEach((option, optionIndex) => {
					if (optionIndex === 0) {
						if (option.CONNECTS_TO.trim().length > 0) {
							const childNode = this.chatBot.WORKFLOW.NODES.find(
								(node) => node.ID === option.CONNECTS_TO
							);
							// 100: height of gap to next div, 90: height of div, 35 height of options, 15 height of gap to option
							const heightToDecrease = 100 + 90 + 35 + 15;
							this.decreaseHeightOfAllChildNodes(childNode, heightToDecrease);

							if (parentNode.POSITION_X) {
								if (parentNode.SUB_TYPE === 'Buttons') {
									parentNode.VALUES.OPTIONS[parentOptionIndex].CONNECTS_TO =
										removedNode.VALUES.OPTIONS[optionIndex].CONNECTS_TO;
								} else {
									parentNode.CONNECTS_TO =
										removedNode.VALUES.OPTIONS[optionIndex].CONNECTS_TO;
								}
							}
						} else {
							if (parentNode.POSITION_X) {
								if (parentNode.SUB_TYPE === 'Buttons') {
									parentNode.VALUES.OPTIONS[parentOptionIndex].CONNECTS_TO = '';
								} else {
									parentNode.CONNECTS_TO = '';
								}
							}
						}
					} else {
						if (option.CONNECTS_TO.trim().length > 0) {
							const nodeToBeRemoved = this.chatBot.WORKFLOW.NODES.find(
								(node) => node.ID === option.CONNECTS_TO
							);
							this.deleteAllChildNodes(nodeToBeRemoved);
						}
					}
				});
			} else {
				if (removedNode.CONNECTS_TO.trim().length > 0) {
					const childNode = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === removedNode.CONNECTS_TO
					);
					const heightToDecrease = 100 + 90;
					// 100: height of gap to next div, 90: height of div
					this.decreaseHeightOfAllChildNodes(childNode, 190);
					if (parentNode.POSITION_X) {
						if (parentNode.SUB_TYPE === 'Buttons') {
							parentNode.VALUES.OPTIONS[parentOptionIndex].CONNECTS_TO =
								removedNode.CONNECTS_TO;
						} else {
							parentNode.CONNECTS_TO = removedNode.CONNECTS_TO;
						}
					}
				} else {
					if (parentNode.POSITION_X) {
						if (parentNode.SUB_TYPE === 'Buttons') {
							parentNode.VALUES.OPTIONS[parentOptionIndex].CONNECTS_TO = '';
						} else {
							parentNode.CONNECTS_TO = '';
						}
					}
				}
			}

			const nodeSize = this.chatBot.WORKFLOW.NODES.length;
			if (nodeSize === 1) {
				if (this.chatBot.WORKFLOW.NODES[0].TYPE === 'Droppable') {
					this.showSidebar = true;
					this.showNodeDetail = false;
					this.sidebarState = 'add';
				} else {
					this.chatBot.WORKFLOW.NODES[0].CONNECTS_TO = '';
				}
			}
		}
	}

	public nodeClicked(node) {
		this.resetContainer();
		this.nodeSelected = JSON.parse(JSON.stringify(node));
		this.currentNodeName = node.NAME;
		this.currentNodeMapping = node.VALUES.MAPPING;
		this.currentNodeQuestion = node.VALUES.MESSAGE;
		this.disableTextInput = node.VALUES.DISABLE_TEXT_INPUT;
		this.nodeInFocus = node.ID;
		this.sidebarState = 'update';
		this.additionalOptionsOpen = false;

		if (node.SUB_TYPE === 'Buttons') {
			this.buttonOptions = JSON.parse(JSON.stringify(node.VALUES.OPTIONS));
		}

		this.nodeSettings[node.ID].SHOW_CHILDREN = true;
		this.showAllParents(node);
		this.showChildren(node);
		if (!this.showSidebar) {
			this.showSidebar = !this.showSidebar;
		}
		this.showNodeDetail = true;
	}

	public updateNode() {
		const nodeToUpdate = this.chatBot.WORKFLOW.NODES.find(
			(node) => node.ID === this.nodeInFocus
		);
		nodeToUpdate.VALUES.MESSAGE = this.currentNodeQuestion;
		nodeToUpdate.VALUES.MAPPING = this.currentNodeMapping;
		nodeToUpdate.VALUES.DISABLE_TEXT_INPUT = this.disableTextInput;
		nodeToUpdate.NAME = this.currentNodeName;

		if (nodeToUpdate.SUB_TYPE === 'Buttons') {
			nodeToUpdate.VALUES.OPTIONS.forEach((buttonOption, buttonOptionIndex) => {
				const existingOption = this.buttonOptions.find(
					(option) => option.OPTION_ID === buttonOption.OPTION_ID
				);

				// Option doesn't exist
				if (!existingOption) {
					const nodeToRemove = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === buttonOption.CONNECTS_TO
					);

					if (nodeToRemove) {
						this.deleteAllChildNodes(nodeToRemove);
					}

					// If option is not the last one
					if (buttonOptionIndex !== nodeToUpdate.VALUES.OPTIONS.length - 1) {
						for (
							let i = buttonOptionIndex + 1;
							i < nodeToUpdate.VALUES.OPTIONS.length;
							i++
						) {
							const nextOption = nodeToUpdate.VALUES.OPTIONS[i];
							if (nextOption.CONNECTS_TO.trim().length > 0) {
								const childNode = this.chatBot.WORKFLOW.NODES.find(
									(node) => node.ID === nextOption.CONNECTS_TO
								);
								this.reduceXPositionOfNodeAndChildren(childNode);
							}
						}
					}
				}
			});
			nodeToUpdate.VALUES.OPTIONS = this.buttonOptions;
		}
		this.resetContainer();
	}

	// Once the options are updated this function
	// is used to update the x positions of all nodes
	private reduceXPositionOfNodeAndChildren(node) {
		// Reduce 125 for opton width, 100 for width gap
		node.POSITION_X = node.POSITION_X - 125 - 100;
		if (node.SUB_TYPE === 'Buttons') {
			node.VALUES.OPTIONS.forEach((option) => {
				if (option.CONNECTS_TO.trim().length > 0) {
					const childNode = this.chatBot.WORKFLOW.NODES.find(
						(botNode) => botNode.ID === option.CONNECTS_TO
					);
					return this.reduceXPositionOfNodeAndChildren(childNode);
				}
			});
		} else {
			if (node.CONNECTS_TO.trim().length > 0) {
				const childNode = this.chatBot.WORKFLOW.NODES.find(
					(botNode) => botNode.ID === node.CONNECTS_TO
				);
				return this.reduceXPositionOfNodeAndChildren(childNode);
			}
		}
	}

	public validateNodeName() {
		const existingNode = this.chatBot.WORKFLOW.NODES.find(
			(node) => node.NAME === this.currentNodeName
		);
		if (existingNode && this.nodeSelected.ID !== existingNode.ID) {
			this.isValidNodeName = false;
		} else {
			this.isValidNodeName = true;
		}
	}

	private increaseHeightOfAllChildNodes(fromNode) {
		if (fromNode.SUB_TYPE === 'Buttons') {
			fromNode.VALUES.OPTIONS.forEach((option) => {
				if (option.CONNECTS_TO.length > 0) {
					const nextNode = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === option.CONNECTS_TO
					);
					nextNode.POSITION_Y = nextNode.POSITION_Y + 190;
					return this.increaseHeightOfAllChildNodes(nextNode);
				}
			});
		} else {
			if (fromNode.CONNECTS_TO.trim().length > 0) {
				const nextNode = this.chatBot.WORKFLOW.NODES.find(
					(node) => node.ID === fromNode.CONNECTS_TO
				);
				nextNode.POSITION_Y = nextNode.POSITION_Y + 190;
				return this.increaseHeightOfAllChildNodes(nextNode);
			}
		}
	}

	private increaseHeightOfAllChildNodesAddedInBetween(fromNode) {
		if (fromNode.SUB_TYPE === 'Buttons') {
			fromNode.VALUES.OPTIONS.forEach((option) => {
				if (option.CONNECTS_TO.length > 0) {
					const nextNode = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === option.CONNECTS_TO
					);
					nextNode.POSITION_Y = nextNode.POSITION_Y + 50;
					return this.increaseHeightOfAllChildNodesAddedInBetween(nextNode);
				}
			});
		} else {
			if (fromNode.CONNECTS_TO.trim().length > 0) {
				const nextNode = this.chatBot.WORKFLOW.NODES.find(
					(node) => node.ID === fromNode.CONNECTS_TO
				);
				nextNode.POSITION_Y = nextNode.POSITION_Y + 50;
				return this.increaseHeightOfAllChildNodesAddedInBetween(nextNode);
			}
		}
	}

	// DECREASES HEIGHT OF NODE PASSED IN AND ALL ITS CHILD NODES
	private decreaseHeightOfAllChildNodes(fromNode, value) {
		fromNode.POSITION_Y = fromNode.POSITION_Y - value;
		if (fromNode.SUB_TYPE === 'Buttons') {
			fromNode.VALUES.OPTIONS.forEach((option) => {
				if (option.CONNECTS_TO.length > 0) {
					const nextNode = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === option.CONNECTS_TO
					);
					return this.decreaseHeightOfAllChildNodes(nextNode, value);
				}
			});
		} else {
			if (fromNode.CONNECTS_TO.trim().length > 0) {
				const nextNode = this.chatBot.WORKFLOW.NODES.find(
					(node) => node.ID === fromNode.CONNECTS_TO
				);
				return this.decreaseHeightOfAllChildNodes(nextNode, value);
			}
		}
	}

	// DELETE_SECTION PASSED IN NODE AND ALL OF ITS CHILD NODES
	private deleteAllChildNodes(nodeToBeRemoved) {
		const nodeIndex = this.chatBot.WORKFLOW.NODES.indexOf(nodeToBeRemoved);

		if (nodeIndex !== -1) {
			this.chatBot.WORKFLOW.NODES.splice(nodeIndex, 1);

			if (nodeToBeRemoved.SUB_TYPE === 'Buttons') {
				nodeToBeRemoved.VALUES.OPTIONS.forEach((option) => {
					if (option.CONNECTS_TO.trim().length > 0) {
						const nextNode = this.chatBot.WORKFLOW.NODES.find(
							(node) => node.ID === option.CONNECTS_TO
						);
						return this.deleteAllChildNodes(nextNode);
					}
				});
			} else {
				if (nodeToBeRemoved.CONNECTS_TO.trim().length > 0) {
					const nextNode = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === nodeToBeRemoved.CONNECTS_TO
					);
					return this.deleteAllChildNodes(nextNode);
				}
			}
		}
	}

	private showAllParents(childNode) {
		let parentNode: any = {};

		for (let i = 0; i < this.chatBot.WORKFLOW.NODES.length; i++) {
			const node = this.chatBot.WORKFLOW.NODES[i];
			if (node.SUB_TYPE === 'Buttons') {
				node.VALUES.OPTIONS.forEach((option) => {
					if (option.CONNECTS_TO === childNode.ID) {
						parentNode = node;
					}
				});
			} else {
				if (node.CONNECTS_TO === childNode.ID) {
					parentNode = node;
					break;
				}
			}
		}

		if (parentNode.POSITION_X) {
			this.nodeSettings[parentNode.ID].SHOW_NODE = true;
			this.nodeSettings[parentNode.ID].SHOW_CHILDREN = true;
			if (parentNode.SUB_TYPE === 'Buttons') {
				parentNode.VALUES.OPTIONS.forEach((option) => {
					if (
						option.CONNECTS_TO.trim().length > 0 &&
						option.CONNECTS_TO !== childNode.ID
					) {
						const nextNode = this.chatBot.WORKFLOW.NODES.find(
							(node) => node.ID === option.CONNECTS_TO
						);
						this.nodeSettings[nextNode.ID].SHOW_NODE = true;
						this.nodeSettings[nextNode.ID].SHOW_CHILDREN = false;
						this.hideChildren(nextNode);
					}
				});
			}
			return this.showAllParents(parentNode);
		} else {
			this.nodeSettings[childNode.ID].SHOW_NODE = true;
			this.nodeSettings[childNode.ID].SHOW_CHILDREN = true;
		}
	}

	private showChildren(fromNode) {
		if (fromNode.SUB_TYPE === 'Buttons') {
			fromNode.VALUES.OPTIONS.forEach((option, optionIndex) => {
				if (optionIndex === 0) {
					if (option.CONNECTS_TO.trim().length > 0) {
						const nextNode = this.chatBot.WORKFLOW.NODES.find(
							(node) => node.ID === option.CONNECTS_TO
						);
						this.nodeSettings[nextNode.ID].SHOW_NODE = true;
						this.nodeSettings[nextNode.ID].SHOW_CHILDREN = true;
						return this.showChildren(nextNode);
					}
				} else {
					if (option.CONNECTS_TO.trim().length > 0) {
						const nextNode = this.chatBot.WORKFLOW.NODES.find(
							(node) => node.ID === option.CONNECTS_TO
						);
						return this.hideChildren(nextNode);
					}
				}
			});
		} else {
			if (fromNode.CONNECTS_TO.trim().length > 0) {
				const nextNode = this.chatBot.WORKFLOW.NODES.find(
					(node) => node.ID === fromNode.CONNECTS_TO
				);
				this.nodeSettings[nextNode.ID].SHOW_NODE = true;
				this.nodeSettings[nextNode.ID].SHOW_CHILDREN = true;
				return this.showChildren(nextNode);
			}
		}
	}

	private hideChildren(fromNode) {
		if (fromNode.SUB_TYPE === 'Buttons') {
			fromNode.VALUES.OPTIONS.forEach((option) => {
				if (option.CONNECTS_TO.trim().length > 0) {
					const nextNode = this.chatBot.WORKFLOW.NODES.find(
						(node) => node.ID === option.CONNECTS_TO
					);
					this.nodeSettings[nextNode.ID].SHOW_NODE = false;
					this.nodeSettings[nextNode.ID].SHOW_CHILDREN = false;
					return this.hideChildren(nextNode);
				}
			});
		} else {
			if (fromNode.CONNECTS_TO.trim().length > 0) {
				const nextNode = this.chatBot.WORKFLOW.NODES.find(
					(node) => node.ID === fromNode.CONNECTS_TO
				);
				this.nodeSettings[fromNode.ID].SHOW_CHILDREN = false;
				this.nodeSettings[nextNode.ID].SHOW_NODE = false;
				this.nodeSettings[nextNode.ID].SHOW_CHILDREN = false;
				return this.hideChildren(nextNode);
			}
		}
	}

	public save() {
		const chatBotId = this.route.snapshot.params['chatBotId'];
		const moduleId = this.route.snapshot.params['moduleId'];
		if (
			this.chatBot.WORKFLOW.NODES.length === 1 &&
			this.chatBot.WORKFLOW.NODES[0].SUB_TYPE === undefined
		) {
			return this.bannerMessageService.errorNotifications.push({
				message: 'Add at least one node',
			});
		} else if (this.chatBotForm.valid) {
			this.chatBot.NAME = this.chatBotForm.value['NAME'];
			this.chatBot.DESCRIPTION = this.chatBotForm.value['DESCRIPTION'];
			if (chatBotId !== 'new') {
				this.modulesService
					.putChatBot(moduleId, chatBotId, this.chatBot)
					.subscribe(
						(response: any) => {
							this.companiesService.trackEvent(`Updated ChatBot`, {
								CHAT_BOT_ID: response.CHAT_BOT_ID,
								MODULE_ID: moduleId,
							});
							this.router.navigate([`modules/${moduleId}/chatbots`]);
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
			} else {
				this.modulesService.postChatBot(moduleId, this.chatBot).subscribe(
					(response: any) => {
						this.companiesService.trackEvent(`Created ChatBot`, {
							CHAT_BOT_ID: response.CHAT_BOT_ID,
							MODULE_ID: moduleId,
						});
						this.router.navigate([`modules/${moduleId}/chatbots`]);
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
	public onSelectTabs($event) {
		const tabValue = $event.tab.textLabel;
		if (tabValue === 'Builder') {
			this.showType = 'Builder';
		} else {
			this.sendMessageDisabled = false;
			this.messageMap = new Map<string, {}>();
			this.showType = 'Preview';
			let i = 0;
			this.buttonMessageArr = [];
			this.chatBot.WORKFLOW.NODES.forEach((node: any) => {
				// if(node.SUB_TYPE === 'QUESTION_ASKED'){
				//   return;
				// }
				if (node && i === 0) {
					let nl2brBody = node.VALUES.MESSAGE;
					const linkifyStr = require('linkifyjs/string');
					nl2brBody = linkifyStr(nl2brBody, {});
					// casts the message in an html format
					const htmlMsg = `<html> <head></head> <body>${nl2brBody}</body> </html>`;
					if (node.SUB_TYPE === 'Buttons') {
						if (
							node.VALUES.DISABLE_TEXT_INPUT !== '' &&
							node.VALUES.DISABLE_TEXT_INPUT
						) {
							this.sendMessageDisabled = true;
						}
						node.VALUES.OPTIONS.forEach((option) => {
							this.buttonMessageArr.push(option);
						});
					}

					let metaData = '';
					if (node.SUB_TYPE === 'TransferToAgent') {
						const mdata = 'Chat will now be transferred to an agent';
						const meta_data = linkifyStr(mdata, {});
						metaData = `<html> <head></head> <body>${meta_data}</body> </html>`;
						this.agentAssignedTime = new Date();
					}
					if (node.SUB_TYPE !== 'SendMessage' && node.MAPPING !== '') {
						i++;
					}
					let chatMessage = {
						MESSAGE: htmlMsg,
						NAME: node.SUB_TYPE,
						DATE_CREATED: new Date(),
						DATE_RESPONDED: '',
						REPLY: '',
						CONNECTS_TO: node.CONNECTS_TO,
						META_DATA: metaData,
					};
					this.messageMap.set(node.ID, chatMessage);
				}
			});
		}
	}

	public triggerFunction(event, submitMessage) {
		// enterToSend is checkbox in chat for if "press enter to submit" is preferred
		if (event.shiftKey && event.key === 'Enter') {
			this.messageTextBox.nativeElement.value += '\n';
		} else if (event.key === 'Enter') {
			event.preventDefault();
			this.sendMessage(submitMessage);
		}
	}
	public sendMessage(submitMessage) {
		if (submitMessage && submitMessage !== '') {
			let nl2brBody = submitMessage;
			const linkifyStr = require('linkifyjs/string');
			nl2brBody = linkifyStr(nl2brBody, {});
			const msg = `<html> <head></head> <body>${nl2brBody}</body> </html>`;
			let valueObject = this.messageMap.get(this.globalNodeId);
			if (valueObject['REPLY'] === null || valueObject['REPLY'] === '') {
				valueObject['REPLY'] = msg;
				(valueObject['DATE_RESPONDED'] = new Date()),
					this.messageMap.set(this.globalNodeId, valueObject);
			}
			let isValidOption = true;
			if (valueObject['NAME'] === 'Buttons') {
				isValidOption = this.validateChatOptions(submitMessage);
			}
			const isValid = this.validateChatMessages(valueObject, submitMessage);
			let index = 0;
			let errorIdx = 0;
			this.buttonMessageArr = [];
			this.sendMessageDisabled = false;
			let nodeExists = false;
			let currentNode = '';
			this.chatBot.WORKFLOW.NODES.forEach((node: any) => {
				if (node && index == 0) {
					if (!this.messageMap.has(node.ID)) {
						if (isValid && isValidOption) {
							this.errorExists = false;
							let nl2brBody = node.VALUES.MESSAGE;
							const linkifyStr = require('linkifyjs/string');
							nl2brBody = linkifyStr(nl2brBody, {});
							// casts the message in an html format
							let htmlMsg = `<html> <head></head> <body>${nl2brBody}</body> </html>`;
							this.buttonMessageArr = [];
							if (node.SUB_TYPE === 'Buttons') {
								if (
									node.VALUES.DISABLE_TEXT_INPUT !== '' &&
									node.VALUES.DISABLE_TEXT_INPUT
								) {
									this.sendMessageDisabled = true;
								}
								this.currentIndex--;
								node.VALUES.OPTIONS.forEach((option) => {
									this.buttonMessageArr.push(option);
									this.currentIndex++;
								});
							}
							if (
								(this.selectedOption &&
									this.selectedOption.CONNECTS_TO !== '' &&
									this.selectedOption.CONNECTS_TO === node.ID) ||
								(this.connectedId && this.connectedId === node.ID)
							) {
								nodeExists = true;
								let metaData = '';
								currentNode = node.SUB_TYPE;
								if (node.SUB_TYPE === 'TransferToAgent') {
									const mdata = 'Chat will now be transferred to an agent';
									const meta_data = linkifyStr(mdata, {});
									metaData = `<html> <head></head> <body>${meta_data}</body> </html>`;
									this.agentAssignedTime = new Date();
									this.buttonMessageArr = [];
								}
								if (node.VALUES.MESSAGE === '' || node.VALUES.MESSAGE == null) {
									this.sendMessageDisabled = true;
									htmlMsg = '';
								}
								let chatMessage = {
									MESSAGE: htmlMsg,
									NAME: node.SUB_TYPE,
									DATE_CREATED: new Date(),
									DATE_RESPONDED: '',
									REPLY: '',
									CONNECTS_TO: node.CONNECTS_TO,
									META_DATA: metaData,
								};
								this.messageMap.set(node.ID, chatMessage);
								this.currentIndex++;
								if (node.SUB_TYPE !== 'SendMessage' && node.MAPPING !== '') {
									index++;
								} else {
									this.buttonMessageArr = [];
									this.connectedId = node.CONNECTS_TO;
									valueObject = this.messageMap.get(node.ID);
								}
							}
						} else {
							if (errorIdx == 0) {
								nodeExists = true;
								const nodeId = this.globalNodeId.split('_')[0];
								const value = this.messageMap.get(nodeId);
								let errorMessage = '';
								let message = value['MESSAGE'];
								if (value['NAME'] === 'Email') {
									errorMessage = 'Not a valid email address.<br>';
									message = errorMessage + message;
								} else if (value['NAME'] === 'Phone') {
									errorMessage =
										'Not a valid phone number.<br>For ex: +1 1234567890 is a valid phone number.<br>';
									message = errorMessage + message;
								}
								let node = this.getNodeById(nodeId);
								this.buttonMessageArr = [];
								if (node.SUB_TYPE === 'Buttons') {
									if (
										node.VALUES.DISABLE_TEXT_INPUT !== '' &&
										node.VALUES.DISABLE_TEXT_INPUT
									) {
										this.sendMessageDisabled = true;
									}
									this.currentIndex--;
									node.VALUES.OPTIONS.forEach((option) => {
										this.buttonMessageArr.push(option);
										this.currentIndex++;
									});
								}
								let chatMessage = {
									MESSAGE: message,
									NAME: value['NAME'],
									DATE_CREATED: new Date(),
									DATE_RESPONDED: '',
									REPLY: '',
									CONNECTS_TO: value['CONNECTS_TO'],
									META_DATA: value['META_DATA'],
								};
								const date = new Date();
								this.messageMap.set(
									this.globalNodeId + '_Error' + date.getSeconds(),
									chatMessage
								);
								errorIdx++;
							}
						}
					}
				}
			});
			if (
				!nodeExists ||
				(currentNode === 'SendMessage' &&
					this.currentIndex === this.chatBot.WORKFLOW.NODES.length - 1)
			) {
				this.agentAssignedTime = new Date();
				valueObject['META_DATA'] = 'This chat has ended.';
				this.sendMessageDisabled = true;
				this.buttonMessageArr = [];
			}
		}
		this.submitMessage = '';
	}

	public validateChatMessages(valueObject, message) {
		if (valueObject['NAME'] === 'Email') {
			return this.emailPattern.test(message);
		}
		if (valueObject['NAME'] === 'Phone') {
			let phoneNumberPattern = /^[2-9]\d{2}[2-9]\d{2}\d{4}$/;
			let digits = message.replace(/\D/g, '');
			return phoneNumberPattern.test(message);
		}
		return true;
	}

	public getKeys() {
		return Array.from(this.messageMap.keys());
	}

	public getValue(key) {
		if (this.messageMap.has(key)) {
			this.globalNodeId = key;
		}
		const chatmessage = this.messageMap.get(key);
		this.connectedId = chatmessage['CONNECTS_TO'];
		return chatmessage;
	}

	public appendMessage(option) {
		this.sendMessageDisabled = true;
		this.selectedOption = option;
		this.sendMessage(option.OPTION);
	}

	public validateChatOptions(submitMessage) {
		for (let i = 0; i < this.buttonMessageArr.length; i++) {
			if (
				submitMessage.toLowerCase() ===
				this.buttonMessageArr[i].OPTION.toLowerCase()
			) {
				this.connectedId = this.buttonMessageArr[i].CONNECTS_TO;
				return true;
			}
		}
		return false;
	}

	public getNodeById(nodeId) {
		return this.chatBot.WORKFLOW.NODES.find((node) => node.ID === nodeId);
	}
}

@Pipe({ name: 'filterDefaultFields' })
export class FilterDefaultFieldsPipe implements PipeTransform {
	transform(fields: any, nodeSelected: any): any {
		if (
			nodeSelected.SUB_TYPE === 'FreeText' ||
			nodeSelected.SUB_TYPE === 'SendMessage' ||
			nodeSelected.SUB_TYPE === 'Buttons'
		) {
			const filteredFields = fields.filter((field) => {
				if (
					field.NAME !== 'CREATED_BY' &&
					field.NAME !== 'SOURCE_TYPE' &&
					field.NAME !== 'CHANNEL' &&
					field.NAME !== 'CHAT_ID' &&
					field.NAME !== 'DATE_CREATED' &&
					field.NAME !== 'LAST_UPDATED_BY' &&
					field.NAME !== 'DATE_UPDATED' &&
					field.DATA_TYPE.DISPLAY !== 'Discussion' &&
					field.DATA_TYPE.DISPLAY !== 'Relationship' &&
					field.DATA_TYPE.DISPLAY !== 'Chronometer'
				) {
					return field;
				}
			});
			return filteredFields;
		}
		return fields;
	}
}
