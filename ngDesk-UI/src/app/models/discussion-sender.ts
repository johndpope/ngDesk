export class DiscussionSender {

  public FIRST_NAME: string;
  public LAST_NAME: string;
  public UUID: string;
  public ROLE: string;

  constructor(firstName, lastName, uuid, role) {
    this.FIRST_NAME = firstName;
    this.LAST_NAME = lastName;
    this.UUID = uuid;
    this.ROLE = role;
  }
}
