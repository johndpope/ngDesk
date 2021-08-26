import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from '../../../custom-components/banner-message/banner-message.service';
import { CustomTableService } from '../../../custom-table/custom-table.service';
import { ConfirmDialogComponent } from '../../../dialogs/confirm-dialog/confirm-dialog.component';
import { GuideService } from '../../guide.service';

@Component({
	selector: 'app-manage-articles',
	templateUrl: './manage-articles.component.html',
	styleUrls: ['./manage-articles.component.scss']
})
export class ManageArticlesComponent implements OnInit {
	public dialogRef: MatDialogRef<ConfirmDialogComponent>;
	public articleActions = {
		actions: [
			{ NAME: '', ICON: 'delete', PERMISSION_NAME: 'DELETE' },
			{ NAME: '', ICON: 'layers', PERMISSION_NAME: 'PUBLISH' },
			{ NAME: '', ICON: 'layers_clear', PERMISSION_NAME: 'UNPUBLISH' }
		]
	};
	constructor(
		private router: Router,
		private bannerMessageService: BannerMessageService,
		public customTableService: CustomTableService,
		private guideService: GuideService,
		private translateService: TranslateService,
		private dialog: MatDialog
	) {
		this.translateService.get('DELETE').subscribe((value: string) => {
			this.articleActions[value] = article => {
				this.deleteArticle(article);
			};
			this.articleActions.actions[0].NAME = value;
		});
		this.translateService.get('PUBLISH').subscribe((enableValue: string) => {
			this.articleActions[enableValue] = article => {
				this.updateArticle(article, true);
			};
			this.articleActions.actions[1].NAME = enableValue;
		});

		this.translateService.get('UNPUBLISH').subscribe((disableValue: string) => {
			this.articleActions[disableValue] = article => {
				this.updateArticle(article, false);
			};
			this.articleActions.actions[2].NAME = disableValue;
		});
	}

	public ngOnInit() {
		// TODO: get all articles
		const columnsHeaders: string[] = [];
		const columnsHeadersObj: { DISPLAY: string; NAME: string }[] = [];
		columnsHeadersObj.push(
			{ DISPLAY: this.translateService.instant('TITLE'), NAME: 'TITLE' },
			{
				DISPLAY: this.translateService.instant('DATE_CREATED'),
				NAME: 'DATE_CREATED'
			},
			{ DISPLAY: this.translateService.instant('PUBLISH'), NAME: 'PUBLISH' }
		);
		columnsHeaders.push(this.translateService.instant('TITLE'));
		columnsHeaders.push(this.translateService.instant('DATE_CREATED'));
		columnsHeaders.push(this.translateService.instant('PUBLISH'));
		columnsHeadersObj.push({
			DISPLAY: this.translateService.instant('ACTION'),
			NAME: 'ACTION'
		});
		columnsHeaders.push(this.translateService.instant('ACTION'));
		this.customTableService.columnsHeaders = columnsHeaders;
		this.customTableService.columnsHeadersObj = columnsHeadersObj;
		this.customTableService.sortBy = 'TITLE';
		this.customTableService.sortOrder = 'asc';
		this.customTableService.pageIndex = 0;
		this.customTableService.pageSize = 10;
		this.customTableService.activeSort = {
			ORDER_BY: 'asc',
			SORT_BY: this.translateService.instant('TITLE'),
			NAME: 'TITLE'
		};
		this.getArticles();
	}

	private getArticles() {
		const sortBy = this.customTableService.sortBy;
		const orderBy = this.customTableService.sortOrder;
		const page = this.customTableService.pageIndex;
		const pageSize = this.customTableService.pageSize;
		this.guideService
			.getSortedArticles(sortBy, orderBy, page + 1, pageSize)
			.subscribe(
				(response: any) => {
					this.customTableService.setTableDataSource(
						response.DATA,
						response.TOTAL_RECORDS
					);
				},
				(error: any) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR
					});
				}
			);
	}

	private deleteArticle(article) {
		let dialogMessage = '';
		this.translateService
			.get('ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS', {
				value: this.translateService.instant('ARTICLE').toLowerCase()
			})
			.subscribe(res => {
				dialogMessage = res;
			});
		const dialogRef = this.dialog.open(ConfirmDialogComponent, {
			data: {
				message: dialogMessage,
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn'
			}
		});

		// EVENT AFTER MODAL DIALOG IS CLOSED
		dialogRef.afterClosed().subscribe(result => {
			if (result === this.translateService.instant('DELETE')) {
				this.guideService.deleteArticle(article.ARTICLE_ID).subscribe(
					(response: any) => {
						this.getArticles();
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR
						});
					}
				);
			}
		});
	}

	private updateArticle(article, publish) {
		article.PUBLISH = publish;
		this.guideService.putArticle(article).subscribe(
			(response: any) => {
				this.getArticles();
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR
				});
			}
		);
	}

	public sortData() {
		this.getArticles();
	}

	public pageChangeEmit(event) {
		this.getArticles();
	}

	public newArticle() {
		this.router.navigate([`guide/articles/detail/new`]);
	}

	public rowClicked(rowData): void {
		if (rowData.PUBLISH) {
			this.router.navigate([
				'guide',
				'articles',
				rowData.SECTION,
				rowData.TITLE
			]);
		} else {
			this.router.navigate([`guide/articles/detail/${rowData.ARTICLE_ID}`]);
		}
	}
}
