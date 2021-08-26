import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { UsersService } from '@src/app/users/users.service';

@Component({
  selector: 'app-email-verify',
  templateUrl: './email-verify.component.html',
  styleUrls: ['./email-verify.component.scss']
})
export class EmailVerifyComponent implements OnInit {
  public email: string;
  public isVerified;

  constructor(
	private route: ActivatedRoute,
	private userService: UsersService
  ) {}

  public ngOnInit() {
	this.email = this.route.snapshot.queryParams['email'];
	const uuid = this.route.snapshot.queryParams['uuid'];
	this.userService.verifyEmail(this.email, uuid).subscribe(
		(response: any) => {
		this.isVerified = true;
		},
		(error: any) => {
		console.log(error);
		this.isVerified = false;
		}
	);
  }
}
