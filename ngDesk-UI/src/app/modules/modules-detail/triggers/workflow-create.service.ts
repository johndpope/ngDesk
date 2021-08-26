import { Injectable } from '@angular/core';
import { Workflow } from '@ngdesk/workflow-api';

@Injectable({
	providedIn: 'root',
})
export class WorkflowCreateService {
	public workflow: Workflow;
	constructor() {}
}
