export class ModuleMapping {
	constructor(
		public SUBJECT: String,
		public BODY: String,
		public REQUESTOR: String,
		public CC_EMAILS: String,
		public FROM_EMAIL: String,
		public TEAMS: String
	) {}

	public get subject() {
		return this.SUBJECT;
	}

	public set subject(subject: String) {
		this.SUBJECT = subject;
	}

	public get body() {
		return this.BODY;
	}

	public set body(body: String) {
		this.BODY = body;
	}

	public get ccEmails() {
		return this.CC_EMAILS;
	}

	public set ccEmails(cEmails: String) {
		this.CC_EMAILS = cEmails;
	}

	public get requestor() {
		return this.REQUESTOR;
	}

	public set requestor(requestor: String) {
		this.REQUESTOR = requestor;
	}

	public get fromEmail() {
		return this.FROM_EMAIL;
	}

	public set fromEmail(fromEmail: String) {
		this.FROM_EMAIL = fromEmail;
	}

	public get teams() {
		return this.TEAMS;
	}

	public set teams(teams: String) {
		this.TEAMS = teams;
	}
}
