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
		private roleService: RolesService
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
		this.guideService.getArticlesBySection(this.sectionId, true).subscribe(
			(articlesResponse: any) => {
				this.sectionArticles = articlesResponse.DATA.filter(
					(article) => article.PUBLISH && article.SECTION === this.sectionId
				).sort((a: { ORDER: number }, b: { ORDER: number }) => {
					return a.ORDER - b.ORDER;
				});
				// find article in section articles
				const articleMatchedByTitle = this.sectionArticles.find(
					(art) => art.TITLE === this.article['TITLE']
				);
				// need to make get individual article call for returning attachments with uuid
				this.guideService
					.getArticle(articleMatchedByTitle.ARTICLE_ID)
					.subscribe(
						(articleResponse: any) => {
							this.article = articleResponse;
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
				if (this.section['MANAGED_BY'].indexOf(team) !== -1) {
					this.hasEditAccess = true;
				}
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
		this.article['AUTHOR'] = JSON.parse(this.article['AUTHOR']);
		let user = this.article['AUTHOR'];
		this.article['AUTHOR'] = `${user.FIRST_NAME} ${user.LAST_NAME}`;
		this.article['COMMENTS'].forEach((comment) => {
			comment.SENDER = JSON.parse(comment.SENDER);
			user = comment.SENDER;
			// if user exists set the name else set anonymous
			comment.SENDER = user
				? `${user.FIRST_NAME} ${user.LAST_NAME}`
				: this.translateService.instant('ANONYMOUS_USER');
		});
		this.loading = false;
	}

	private getSection() {
		this.guideService.getSectionById(this.sectionId).subscribe(
			(sectionResponse: any) => {
				this.section = sectionResponse;
				// check if user has access to edit this article and display edit button
				this.checkEditAccess();
				this.guideService.getCategoryById(sectionResponse.CATEGORY).subscribe(
					(categoryResponse: any) => {
						this.navArray.push({
							NAME: categoryResponse.NAME,
							PATH: ['guide', 'categories', sectionResponse.CATEGORY, 'detail'],
						});
						this.navArray.push({
							NAME: sectionResponse.NAME,
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
			this.guideService
				.postComment(this.article['ARTICLE_ID'], this.comment)
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
