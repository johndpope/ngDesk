export class Customer {

  constructor(private EMAIL_ADDRESS: string, private PASSWORD: string, private FIRST_NAME: string,
    private LAST_NAME: string, private PHONE_NUMBER: string) {

  }

  public get emailAddress() {
    return this.EMAIL_ADDRESS;
  }

  public set emailAddress(emailAddress: string) {
    this.EMAIL_ADDRESS = emailAddress;
  }

  public get password() {
    return this.PASSWORD;
  }

  public set password(password: string) {
    this.PASSWORD = password;
  }

  public get firstName() {
    return this.FIRST_NAME;
  }

  public set firstName(firstName: string) {
    this.FIRST_NAME = firstName;
  }

  public get lastName() {
    return this.LAST_NAME;
  }

  public set lastName(lastName: string) {
    this.LAST_NAME = lastName;
  }

  public get phoneNumber() {
    return this.PHONE_NUMBER;
  }

  public set phoneNumber(phoneNumber: string) {
    this.PHONE_NUMBER = phoneNumber;
  }
}
