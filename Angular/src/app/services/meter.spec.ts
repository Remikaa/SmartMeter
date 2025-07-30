import { TestBed } from '@angular/core/testing';

import { Meter } from './meter';

describe('Meter', () => {
  let service: Meter;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Meter);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
