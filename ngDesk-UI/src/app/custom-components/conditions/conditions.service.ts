import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppGlobals } from '@src/app/app.globals';
import { ModulesService } from '@src/app/modules/modules.service';

@Injectable({
	providedIn: 'root',
})
export class ConditionsService {
	constructor(
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private http: HttpClient,
		private globals: AppGlobals
	) {}

	public setOperators(condition) {
		if (condition.DATA_TYPE) {
			if (
				condition.DATA_TYPE.BACKEND === 'Custom' &&
				condition.DATA_TYPE.DISPLAY === 'Custom'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
						BACKEND: 'NOT_EQUALS_TO',
					},
				];
			}
			if (
				condition.DATA_TYPE.BACKEND === 'String' &&
				condition.DATA_TYPE.DISPLAY === 'Discussion'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('CONTAINS'),
						BACKEND: 'CONTAINS',
					},
					{
						DISPLAY: this.translateService.instant('DOES_NOT_CONTAIN'),
						BACKEND: 'DOES_NOT_CONTAIN',
					},
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('LENGTH_IS_GREATER_THAN'),
						BACKEND: 'LENGTH_IS_GREATER_THAN',
					},
					{
						DISPLAY: this.translateService.instant('LENGTH_IS_LESS_THAN'),
						BACKEND: 'LENGTH_IS_LESS_THAN',
					},
					{
						DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
						BACKEND: 'NOT_EQUALS_TO',
					},
				];
			}
			if (
				condition.DATA_TYPE.BACKEND === 'String' &&
				condition.DATA_TYPE.DISPLAY === 'Text'
			) {
				let operators: any[] = [];
				if (condition.NAME === 'ROLE') {
					operators = [
						{
							DISPLAY: this.translateService.instant('EQUALS_TO'),
							BACKEND: 'EQUALS_TO',
						},
						{
							DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
							BACKEND: 'NOT_EQUALS_TO',
						},
					];
				} else {
					operators = [
						{
							DISPLAY: this.translateService.instant('CONTAINS'),
							BACKEND: 'CONTAINS',
						},
						{
							DISPLAY: this.translateService.instant('DOES_NOT_CONTAIN'),
							BACKEND: 'DOES_NOT_CONTAIN',
						},
						{
							DISPLAY: this.translateService.instant('EQUALS_TO'),
							BACKEND: 'EQUALS_TO',
						},
						{
							DISPLAY: this.translateService.instant('LENGTH_IS_GREATER_THAN'),
							BACKEND: 'LENGTH_IS_GREATER_THAN',
						},
						{
							DISPLAY: this.translateService.instant('LENGTH_IS_LESS_THAN'),
							BACKEND: 'LENGTH_IS_LESS_THAN',
						},
						{
							DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
							BACKEND: 'NOT_EQUALS_TO',
						},
						{
							DISPLAY: this.translateService.instant('REGEX'),
							BACKEND: 'REGEX',
						},
					];
				}
				return operators;
			}

			if (
				condition.DATA_TYPE.BACKEND === 'String' &&
				condition.DATA_TYPE.DISPLAY !== 'Relationship'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('CONTAINS'),
						BACKEND: 'CONTAINS',
					},
					{
						DISPLAY: this.translateService.instant('DOES_NOT_CONTAIN'),
						BACKEND: 'DOES_NOT_CONTAIN',
					},
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
						BACKEND: 'NOT_EQUALS_TO',
					},
				];
			}

			if (
				condition.DATA_TYPE.BACKEND === 'Integer' ||
				condition.DATA_TYPE.BACKEND === 'Float' ||
				condition.DATA_TYPE.BACKEND === 'Aggregate'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('GREATER_THAN'),
						BACKEND: 'GREATER_THAN',
					},
					{
						DISPLAY: this.translateService.instant('LESS_THAN'),
						BACKEND: 'LESS_THAN',
					},
					{
						DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
						BACKEND: 'NOT_EQUALS_TO',
					},
				];
			}
			if (
				condition.DATA_TYPE.DISPLAY === 'Time Window' ||
				condition.DATA_TYPE.DISPLAY === 'Approval'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
				];
			}
			if (
				(condition.DATA_TYPE.BACKEND === 'Timestamp' ||
					condition.DATA_TYPE.BACKEND === 'Date') &&
				condition.DATA_TYPE.DISPLAY !== 'Time Window'
			) {
				if (condition.DATA_TYPE.DISPLAY !== 'Time') {
					return [
						{
							DISPLAY: this.translateService.instant('DAYS_BEFORE_TODAY'),
							BACKEND: 'DAYS_BEFORE_TODAY',
						},
						{
							DISPLAY: this.translateService.instant('GREATER_THAN'),
							BACKEND: 'GREATER_THAN',
						},
						{
							DISPLAY: this.translateService.instant('LESS_THAN'),
							BACKEND: 'LESS_THAN',
						},
					];
				} else {
					return [
						{
							DISPLAY: this.translateService.instant('GREATER_THAN'),
							BACKEND: 'GREATER_THAN',
						},
						{
							DISPLAY: this.translateService.instant('LESS_THAN'),
							BACKEND: 'LESS_THAN',
						},
					];
				}
			}
			if (
				condition.DATA_TYPE.BACKEND === 'Array' &&
				condition.DATA_TYPE.DISPLAY === 'Relationship'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('CONTAINS'),
						BACKEND: 'CONTAINS',
					},
					{
						DISPLAY: this.translateService.instant('DOES_NOT_CONTAIN'),
						BACKEND: 'DOES_NOT_CONTAIN',
					},
					{
						DISPLAY: this.translateService.instant('REGEX'),
						BACKEND: 'REGEX',
					},
				];
			}
			if (
				condition.DATA_TYPE.BACKEND === 'Array' ||
				condition.DATA_TYPE.DISPLAY === 'List Text'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('CONTAINS'),
						BACKEND: 'CONTAINS',
					},
					{
						DISPLAY: this.translateService.instant('DOES_NOT_CONTAIN'),
						BACKEND: 'DOES_NOT_CONTAIN',
					},
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
						BACKEND: 'NOT_EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('REGEX'),
						BACKEND: 'REGEX',
					},
				];
			}

			if (
				condition.DATA_TYPE.BACKEND === 'Boolean' ||
				condition.DATA_TYPE.DISPLAY === 'Relationship'
			) {
				return [
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
						BACKEND: 'NOT_EQUALS_TO',
					},
				];
			}

			if (condition.DATA_TYPE.BACKEND === 'Formula') {
				return [
					{
						DISPLAY: this.translateService.instant('EQUALS_TO'),
						BACKEND: 'EQUALS_TO',
					},
					{
						DISPLAY: this.translateService.instant('NOT_EQUALS_TO'),
						BACKEND: 'NOT_EQUALS_TO',
					},
				];
			}
		} else {
			return [];
		}
	}

	public buildQueryToGetRelationshipData(
		module,
		pageNumber,
		primaryDisplayField,
		searchValue
	) {
		const query = `
						{
							DATA: get${module.NAME}(moduleId: "${module.MODULE_ID}", pageNumber: ${pageNumber}, pageSize: 10, sortBy: "DATE_CREATED", orderBy: "Asc", search: "${searchValue}") {
								DATA_ID: _id
								PRIMARY_DISPLAY_FIELD:${primaryDisplayField.NAME}
								
						}
					}`;

		return this.makeGraphQLCall(query);
	}

	public makeGraphQLCall(query: string) {
		return this.http.post(this.globals.graphqlUrl, query);
	}
}
