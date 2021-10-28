import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
	name: 'filterRuleOption',
})
export class FilterRuleOptionPipe implements PipeTransform {
	public transform(items: any, input: string, filterType: string): any {
		const filteredItems = [];
		switch (filterType) {
			case 'conditions': {
				items.forEach((item) => {
					if (item.DISPLAY_LABEL.toLowerCase().includes(input)) {
						filteredItems.push(item);
					}
				});
				break;
			}

			case 'users': {
				items.forEach((item) => {
					if (
						(item.FIRST_NAME &&
							item.FIRST_NAME.toLowerCase().includes(input)) ||
						(item.LAST_NAME && item.LAST_NAME.toLowerCase().includes(input)) ||
						item.CONTACT.PRIMARY_DISPLAY_FIELD.toLowerCase().includes(input)
					) {
						filteredItems.push(item);
					} else {
						if (item.FULL_NAME.toLowerCase().includes(input)) {
							filteredItems.push(item);
						}
					}
				});
				break;
			}

			case 'teams': {
				items.forEach((item) => {
					if (item.NAME.toLowerCase().includes(input)) {
						filteredItems.push(item);
					}
				});
				break;
			}

			case 'schedules': {
				items.forEach((item) => {
					if (
						(item.NAME && item.NAME.toLowerCase().includes(input)) ||
						(item.name && item.name.toLowerCase().includes(input))
					) {
						filteredItems.push(item);
					}
				});
				break;
			}

			case 'cc_emails': {
				items.forEach((item) => {
					if (item.EMAIL_ADDRESS.toLowerCase().includes(input)) {
						filteredItems.push(item);
					}
				});
				break;
			}
			case 'languages': {
				items.forEach((item) => {
					if (item.toLowerCase().includes(input)) {
						filteredItems.push(item);
					}
				});
				break;
			}

			case 'picklistValues': {
				items.forEach((item) => {
					if (item.toLowerCase().includes(input)) {
						filteredItems.push(item);
					}
				});
				break;
			}

			case 'values': {
				items.forEach((item) => {
					if (
						(typeof item[item['PRIMARY_DISPLAY_FIELD']] === 'string' &&
							item[item['PRIMARY_DISPLAY_FIELD']]
								.toLowerCase()
								.includes(input)) ||
						(typeof item[item['PRIMARY_DISPLAY_FIELD']] !== 'string' &&
							item[item['PRIMARY_DISPLAY_FIELD']] === input)
					) {
						filteredItems.push(item);
					}
				});
				break;
			}

			case 'roles': {
				items.forEach((item) => {
					if (item.NAME.toLowerCase().includes(input.toLowerCase())) {
						filteredItems.push(item);
					}
				});
				break;
			}

			default: {
				if (items) {
					items.forEach((item) => {
						if (item.DISPLAY.toLowerCase().includes(input)) {
							filteredItems.push(item);
						}
					});
				}
			}
		}

		return filteredItems;
	}
}
