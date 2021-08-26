export class EscalateTo {
  constructor(public SCHEDULE_IDS: string[], public USER_IDS: string[], public TEAM_IDS: string[]) {

  }

  public get scheduleIds() {
    return this.SCHEDULE_IDS;
  }

  public set scheduleIds(scheduleIds: string[]) {
    this.SCHEDULE_IDS = scheduleIds;
  }

  public get userIds() {
    return this.USER_IDS;
  }

  public set userIds(userIds: string[]) {
    this.USER_IDS = userIds;
  }

  public get teamIds() {
    return this.TEAM_IDS;
  }

  public set teamIds(teamIds: string[]) {
    this.TEAM_IDS = teamIds;
  }
}
