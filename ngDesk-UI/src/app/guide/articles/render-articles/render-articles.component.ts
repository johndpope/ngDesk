import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { AppGlobals } from '../../../app.globals';
import { CompaniesService } from '../../../companies/companies.service';
import { BannerMessageService } from '../../../custom-components/banner-message/banner-message.service';
import { ModulesService } from '../../../modules/modules.service';
import { UsersService } from '../../../users/users.service';
import { GuideService } from '../../guide.service';
import { RolesService } from 'src/app/roles/roles.service';
import { ArticleApiService, CommentMessage } from '@ngdesk/knowledgebase-api';

@Component({
	selector: 'app-render-articles',
	templateUrl: './render-articles.component.html',
	styleUrls: ['./render-articles.component.scss'],
})
export class RenderArticlesComponent implements OnInit {
	public article = {};
	public section = {};
	public sectionArticles = [];
	public sectionId: string;
	public comment = {};
	public users = [];
	public hasEditAccess = false;
	public hasCommentAccess = false;
	public loading: boolean;
	public navArray = [{ NAME: 'GUIDE_HOME', PATH: ['guide'] }];
	public isPublicArticle = true;
	public authToken = this.usersService.getAuthenticationToken();
	public roleName: string;
	public commentMessage = [];
	constructor(
		private route: ActivatedRoute,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		private usersService: UsersService,
		private guideService: GuideService,
		private router: Router,
		private globals: AppGlobals,
		private companiesService: CompaniesService,
		private translateService: TranslateService,
		private roleService: RolesService,
		private articleApiService: ArticleApiService
	) {
		if (!this.authToken) {
			this.companiesService
				.getKnowledgeBaseGeneralSettings()
				.subscribe((response: any) => {
					if (!response.ENABLE_DOCS) {
						this.router.navigate(['login']);
					}
				});
		}
	}

	public ngOnInit() {
		this.route.params.subscribe((params) => {
			this.loading = true;
			this.article['TITLE'] = this.route.snapshot.params['articleName'];
			this.sectionId = this.route.snapshot.params['sectionId'];
			this.getArticle();
			this.hasCommentAccess = this.usersService.getAuthenticationToken()
				? true
				: false;
		});
		this.getSection();
	}

	private getArticle() {
		// get article sections
		this.guideService.getArticlesBySectionId(this.sectionId).subscribe(
			(articlesResponse: any) => {
				this.sectionArticles = articlesResponse.getArticlesBySectionId
					.filter(
						(article) => article.PUBLISH && article.SECTION === this.sectionId
					)
					.sort((a: { ORDER: number }, b: { ORDER: number }) => {
						return a.ORDER - b.ORDER;
					});

				// find article in section articles
				const articleMatchedByTitle = this.sectionArticles.find(
					(art) => art.TITLE === this.article['TITLE']
				);

				// need to make get individual article call for returning attachments with uuid
				this.guideService
					.getKbArticleById(articleMatchedByTitle.ARTICLE_ID)
					.subscribe(
						(articleResponse: any) => {
							this.article = articleResponse.DATA;
							const userRole = this.usersService.user['ROLE'];
							//if user is logged in
							if (userRole !== undefined) {
								this.roleService
									.getRole(userRole)
									.subscribe((response: any) => {
										this.roleName = response.NAME;
										// if admin or agent is logged in, get names from users module call
										// else, comments cannot be added just viewed
										if (
											this.usersService.getAuthenticationToken() &&
											(this.roleName === 'SystemAdmin' ||
												this.roleName === 'Agent')
										) {
											this.getAuthorName();
										} else if (this.article === undefined) {
											this.isPublicArticle = false;
											this.loading = false;
											return;
										} else {
											this.loading = false;
										}
									});
							} else {
								//if user is logged out
								this.loading = false;
							}
						},
						(articleError: any) => {
							this.bannerMessageService.errorNotifications.push(
								articleError.error.ERROR
							);
						}
					);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push(error.error.ERROR);
			}
		);
	}

	public checkEditAccess() {
		if (this.usersService.getAuthenticationToken()) {
			this.usersService.user['TEAMS'].forEach((team, index) => {
				this.hasEditAccess = true;
				this.section['managedBy'].forEach((managedBy) => {
					if (managedBy['_id'] === team) {
						this.hasEditAccess = false;
					}
				});
			});
			this.roleService
				.getRole(this.usersService.user.ROLE)
				.subscribe((response: any) => {
					if (response.NAME === 'SystemAdmin') {
						this.hasEditAccess = true;
					}
				});
		}
	}

	public downloadAttachment(uuid) {
		return `${this.globals.baseRestUrl}/attachments?attachment_uuid=${uuid}&entry_id=${this.article['ARTICLE_ID']}`;
	}

	private getAuthorName(): void {
		let user = this.article;
		this.article[
			'AUTHOR'
		] = `${user['AUTHOR'].CONTACT.FIRST_NAME} ${user['AUTHOR'].CONTACT.LAST_NAME}`;

		if (this.article['COMMENTS'] !== null) {
			this.article['COMMENTS'].forEach((comment) => {
				// comment.sender = JSON.parse(comment.sender);

				user = comment.sender;
				// if user exists set the name else set anonymous
				comment.sender = user
					? `${user['CONTACT'].FIRST_NAME} ${user['CONTACT'].LAST_NAME}`
					: this.translateService.instant('ANONYMOUS_USER');
			});
		}
		this.loading = false;
	}

	private getSection() {
		this.guideService.getkbSections(this.sectionId).subscribe(
			(sectionResponse: any) => {
				this.section = sectionResponse.DATA;
				// check if user has access to edit this article and display edit button
				this.checkEditAccess();

				this.guideService
					.getKbCategoryById(sectionResponse.DATA.category.categoryId)
					.subscribe(
						(categoryResponse: any) => {
							this.navArray.push({
								NAME: categoryResponse.DATA.name,
								PATH: [
									'guide',
									'categories',
									sectionResponse.CATEGORY,
									'detail',
								],
							});
							this.navArray.push({
								NAME: sectionResponse.DATA.name,
								PATH: ['guide', 'sections', this.sectionId, 'detail'],
							});
						},
						(error: any) => {
							this.bannerMessageService.errorNotifications.push(
								error.error.ERROR
							);
						}
					);
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push(error.error.ERROR);
			}
		);
	}

	public addComment(): void {
		if (this.comment['MESSAGE'] !== undefined) {
			this.loading = true;
			this.articleApiService
				.postComments(this.article['ARTICLE_ID'], this.commentMessage)
				.subscribe(
					(response: any) => {
						this.comment['MESSAGE'] = '';
						this.getArticle();
					},
					(error: any) => {
						this.bannerMessageService.errorNotifications.push(
							error.error.ERROR
						);
					}
				);
		} else {
			this.bannerMessageService.errorNotifications.push({
				message: this.translateService.instant(
					'MESSAGE_VALUE_REQUIRED_IN_COMMENT'
				),
			});
		}
	}

	public goToEditArticle(): void {
		// go to edit article page
		this.router.navigate([
			'guide',
			'articles',
			'detail',
			this.article['ARTICLE_ID'],
		]);
	}

	public navigate(path) {
		// if (this.authToken) {
		this.router.navigate(path);
		// }
	}
}
