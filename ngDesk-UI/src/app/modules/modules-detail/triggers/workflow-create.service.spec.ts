import { TestBed } from '@angular/core/testing';

import { WorkflowCreateService } from './workflow-create.service';

describe('WorkflowCreateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: WorkflowCreateService = TestBed.get(WorkflowCreateService);
    expect(service).toBeTruthy();
  });
});
