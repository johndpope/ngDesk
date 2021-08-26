export class InviteEmailMessage {
  constructor(
    private MESSAGE_1: string,
    private MESSAGE_2: string,
    private SUBJECT: string
  ) {}

  public get message1() {
    return this.MESSAGE_1;
  }

  public set message1(message1) {
    this.MESSAGE_1 = message1;
  }

  public get message2() {
    return this.MESSAGE_2;
  }

  public set message2(message2) {
    this.MESSAGE_2 = message2;
  }

  public get subject() {
    return this.SUBJECT;
  }

  public set subject(subject) {
    this.SUBJECT = subject;
  }
}
