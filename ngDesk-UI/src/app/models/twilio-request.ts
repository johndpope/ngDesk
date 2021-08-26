export class TwilioRequest {
  constructor(
    private COMPANY_SUBDOMAIN: string,
    private EMAIL_ADDRESS: string,
    private FIRST_NAME: string,
    private LAST_NAME: string,
    private PHONE_NUMBER: string,
    private COUNTRY: any
  ) {}

  public get companySubdomain() {
    return this.COMPANY_SUBDOMAIN;
  }

  public set companySubdomain(companySubdomain: string) {
    this.COMPANY_SUBDOMAIN = companySubdomain;
  }

  public get emailAddress() {
    return this.EMAIL_ADDRESS;
  }

  public set emailAddress(emailAddress: string) {
    this.EMAIL_ADDRESS = emailAddress;
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

  public get country() {
    return this.COUNTRY;
  }

  public set country(country: string) {
    this.COUNTRY = country;
  }
}
