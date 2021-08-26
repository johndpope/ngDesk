export class SignupEmail {
  constructor(private SUBJECT: string, private MESSAGE: string) {}

  public get subject() {
    return this.SUBJECT;
  }

  public set subject(subject) {
    this.SUBJECT = subject;
  }

  public get message() {
    return this.MESSAGE;
  }

  public set message(message) {
    this.MESSAGE = message;
  }
}
