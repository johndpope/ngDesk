export interface Role {
  PERMISSIONS: Permission[];
  DESCRIPTION: string;
  ROLE_ID: string;
  USERS: User[];
  NAME: string;
}

export interface ModulePermissions {
  DELETE: string;
  ACCESS_TYPE: string;
  EDIT: string;
  VIEW: string;
  ACCESS: string;
  MODULE_NAME: string;
}

export interface FieldPermissions {
  PERMISSION: string;
  FIELD: string;
}

export interface Permission {
  MODULE_PERMISSIONS: ModulePermissions;
  MODULE: string;
  FIELD_PERMISSIONS: FieldPermissions[];
}

export interface User {
  LANGUAGE: string;
  DATE_CREATED: any;
  DATA_ID: string;
  LAST_NAME: string;
  DELETED: boolean;
  LAST_IP: string;
  FIRST_NAME: string;
  DISABLED: boolean;
  ROLE: string;
  ACCOUNT: string;
  STATUS: string;
  LAST_BROWSER: string;
  EMAIL_ADDRESS: string;
  DEFAULT_CONTACT_METHOD: string;
  TEAMS: string[];
  USER_UUID: string;
  TIMEZONE: string;
  LAST_SEEN: string;
  LAST_SCREEN_DIMENSIONS: string;
  LAST_UPDATED?: Date;
  NOTIFICATION_SOUND: string;
  EMAIL_VERIFIED?: boolean;
  LOGIN_ATTEMPTS?: number;
  PHONE_NUMBER: string;
}
