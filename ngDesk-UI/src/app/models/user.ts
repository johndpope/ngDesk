
export interface User {
  LANGUAGE: string;
  DATE_CREATED: string;
  DATA_ID: string;
  NOTIFICATION_SOUND: string;
  LAST_NAME: string;
  DELETED: boolean;
  FIRST_NAME: string;
  DISABLED: boolean;
  EMAIL_VERIFIED: boolean;
  ROLE: string;
  ACCOUNT: string;
  STATUS: string;
  EMAIL_ADDRESS: string;
  DEFAULT_CONTACT_METHOD: string;
  TEAMS: string[];
  USER_UUID: string;
  LOGIN_ATTEMPTS: number;
  INVITE_ACCEPTED: boolean;
  LAST_UPDATED_BY: string;
  DATE_UPDATED?: Date;
}
