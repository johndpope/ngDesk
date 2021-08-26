import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';

import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { LayerRestriction } from '@src/app/models/layer-restriction';
import { SchedulesDetailService } from '@src/app/schedules/schedules-detail/schedules-detail.service';

@Component({
  selector: 'app-chat-business-rule-dialog',
  templateUrl: './chat-business-rule-dialog.component.html',
  styleUrls: ['./chat-business-rule-dialog.component.scss']
})
export class ChatBusinessRuleDialogComponent implements OnInit {
  public times: any = [];
  public weekDays: any = [];
  public restrictionsDaily: any = [];
  public restrictionsSpecific: any = [];
  public buisnessRule: any;
  public errorMessage: String = '';
  constructor(
    public dialogRef: MatDialogRef<ChatBusinessRuleDialogComponent>,
    public scheduleDetailService: SchedulesDetailService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public bannerMessageService: BannerMessageService,
    public translateService: TranslateService
  ) { }

  public ngOnInit() {
    if (this.data.buisnessRule) {
      this.buisnessRule = JSON.parse(JSON.stringify(this.data.buisnessRule));
    } else {
      this.buisnessRule = {
        TIMEZONE: '',
        ACTIVE: true,
        RESTRICTION_TYPE: '',
        RESTRICTIONS: []
      };
    }
    this.times = this.scheduleDetailService.startTimes;
    this.weekDays = this.scheduleDetailService.weekDays;
    if (
      this.buisnessRule.RESTRICTION_TYPE === 'Day' ||
      this.buisnessRule.RESTRICTION_TYPE === null ||
      this.buisnessRule.RESTRICTION_TYPE === ''
    ) {
      this.buisnessRule.RESTRICTION_TYPE = 'Day';
      if (this.buisnessRule.RESTRICTIONS.length > 0) {
        this.restrictionsDaily = this.buisnessRule.RESTRICTIONS;
        this.restrictionsSpecific.push({
          START_TIME: '00:00',
          END_TIME: '01:00',
          START_DAY: 'Sun',
          END_DAY: 'Sun'
        });
      } else {
        // EMPTY RESTRICTIONS
        this.restrictionsDaily.push({
          START_TIME: '00:00',
          END_TIME: '01:00',
          START_DAY: null,
          END_DAY: null
        });
        this.restrictionsSpecific.push({
          START_TIME: '00:00',
          END_TIME: '01:00',
          START_DAY: 'Sun',
          END_DAY: 'Sun'
        });
      }
    } else {
      if (this.buisnessRule.RESTRICTIONS.length > 0) {
        this.restrictionsSpecific = this.buisnessRule.RESTRICTIONS;
        this.restrictionsDaily.push({
          START_TIME: '00:00',
          END_TIME: '01:00',
          START_DAY: null,
          END_DAY: null
        });
      } else {
        // EMPTY RESTRICTIONS
        this.restrictionsDaily.push({
          START_TIME: '00:00',
          END_TIME: '01:00',
          START_DAY: null,
          END_DAY: null
        });
        this.restrictionsSpecific.push({
          START_TIME: '00:00',
          END_TIME: '01:00',
          START_DAY: 'Sun',
          END_DAY: 'Sun'
        });
      }
    }
  }

  public addRestriction(restrictionType): void {
    if (restrictionType === 'Day') {
      this.restrictionsDaily.push({
        START_TIME: '00:00',
        END_TIME: '01:00',
        START_DAY: null,
        END_DAY: null
      });
    } else if (restrictionType === 'Week') {
      this.restrictionsSpecific.push({
        START_TIME: '00:00',
        END_TIME: '01:00',
        START_DAY: 'Sun',
        END_DAY: 'Sun'
      });
    }
  }

  public removeRestriction(restrictionType, index): void {
    if (restrictionType === 'Day') {
      this.restrictionsDaily.splice(index, 1);
    } else if (restrictionType === 'Week') {
      this.restrictionsSpecific.splice(index, 1);
    }
  }

  public onNoClick(): void {
    this.dialogRef.close();
  }

  private getDate(): Date {
    const tempDate = new Date();
    tempDate.setHours(0);
    tempDate.setMinutes(0);
    tempDate.setSeconds(0);
    tempDate.setMilliseconds(0);

    return tempDate;
  }

  private getDayValue(day): number {
    switch (day) {
      case 'Sun':
        return 0;
      case 'Mon':
        return 1;
      case 'Tue':
        return 2;
      case 'Wed':
        return 3;
      case 'Thu':
        return 4;
      case 'Fri':
        return 5;
      case 'Sat':
        return 6;
      default:
        return null;
    }
  }

  // This function returns an array of restrictions in ascending order, helps in determining crazy overlapping conditions
  // ex: for daily if 09:00 - 03:00 is given then it returns 09:00 - 00:00 and 00:00 - 03:00
  // incase of weekly if Tue 09:00 - Mon 03:00 is given then it returns Tue 09:00 - Sun 00:00 and Sun 00:00 - Mon 03:00
  private sortRestrictions(): any {
    if (this.buisnessRule.RESTRICTION_TYPE === 'Day') {
      const temp: any = [];
      this.restrictionsDaily.forEach((restriction, restrictionIndex) => {
        let st = parseInt(restriction.START_TIME.split(':')[0], 10);
        if (parseInt(restriction.START_TIME.split(':')[1], 10) === 30) {
          st += 0.5;
        }

        let end = parseInt(restriction.END_TIME.split(':')[0], 10);
        if (parseInt(restriction.END_TIME.split(':')[1], 10) === 30) {
          end += 0.5;
        }

        if (st >= end && end !== 0) {
          temp.push(
            new LayerRestriction(restriction.START_TIME, '00:00', null, null)
          );
          temp.push(
            new LayerRestriction('00:00', restriction.END_TIME, null, null)
          );
        } else if (st >= end && end === 0) {
          temp.push(
            new LayerRestriction(restriction.START_TIME, '00:00', null, null)
          );
        } else {
          temp.push(restriction);
        }
      });
      return temp;
    } else if (this.buisnessRule.RESTRICTION_TYPE === 'Week') {
      const temp: any = [];
      this.restrictionsSpecific.forEach((restriction, restrictionIndex) => {
        const START_DAY = this.getDayValue(restriction.START_DAY);
        let END_DAY = this.getDayValue(restriction.END_DAY);

        let START_TIME = parseInt(restriction.START_TIME.split(':')[0], 10);
        if (parseInt(restriction.START_TIME.split(':')[1], 10) === 30) {
          START_TIME += 0.5;
        }

        let END_TIME = parseInt(restriction.END_TIME.split(':')[0], 10);
        if (parseInt(restriction.END_TIME.split(':')[1], 10) === 30) {
          END_TIME += 0.5;
        }

        if (START_DAY !== 0 && END_DAY === 0) {
          END_DAY = 7;
        }

        if (START_DAY === 0 && END_DAY === 0) {
          if (START_TIME < END_TIME) {
            temp.push(restriction);
          } else {
            temp.push(
              new LayerRestriction(
                restriction.START_TIME,
                '00:00',
                restriction.START_DAY,
                'Sun'
              )
            );
            temp.push(
              new LayerRestriction(
                '00:00',
                restriction.END_TIME,
                'Sun',
                restriction.END_DAY
              )
            );
          }
        } else {
          if (START_DAY < END_DAY) {
            temp.push(restriction);
          } else if (START_DAY > END_DAY) {
            temp.push(
              new LayerRestriction(
                restriction.START_TIME,
                '00:00',
                restriction.START_DAY,
                'Sun'
              )
            );
            temp.push(
              new LayerRestriction(
                '00:00',
                restriction.END_TIME,
                'Sun',
                restriction.END_DAY
              )
            );
          } else {
            // start and end days are same
            if (START_TIME < END_TIME) {
              temp.push(restriction);
            } else {
              temp.push(
                new LayerRestriction(
                  restriction.START_TIME,
                  '00:00',
                  restriction.START_DAY,
                  'Sun'
                )
              );
              temp.push(
                new LayerRestriction(
                  '00:00',
                  restriction.END_TIME,
                  'Sun',
                  restriction.END_DAY
                )
              );
            }
          }
        }
      });
      return temp;
    }
  }

  public checkForSameRestrictions(type): boolean {
    if (type === 'Day') {
      let keepGoing = true;
      let hasSameRestrictions = false;
      this.restrictionsDaily.forEach((restriction, restrictionIndex) => {
        if (keepGoing) {
          if (restriction.START_TIME === restriction.END_TIME) {
            keepGoing = false;
            hasSameRestrictions = true;
          }
        }
      });
      return hasSameRestrictions;
    } else if (type === 'Week') {
      let keepGoing = true;
      let hasSameRestrictions = false;
      this.restrictionsSpecific.forEach((restriction, restrictionIndex) => {
        if (keepGoing) {
          if (
            restriction.START_TIME === restriction.END_TIME &&
            restriction.START_DAY === restriction.END_DAY
          ) {
            keepGoing = false;
            hasSameRestrictions = true;
          }
        }
      });
      return hasSameRestrictions;
    }
  }

  public saveRestrictions(): void {
    if (this.buisnessRule.RESTRICTION_TYPE === 'Day') {
      if (!this.checkForSameRestrictions(this.buisnessRule.RESTRICTION_TYPE)) {
        // NO SAME RESTRICTIONS
        const temp: any = this.sortRestrictions();
        let validRestrictions = true;

        outerloop: for (let i = 0; i < temp.length - 1; i++) {
          let st = parseInt(temp[i].START_TIME.split(':')[0], 10);
          if (parseInt(temp[i].START_TIME.split(':')[1], 10) === 30) {
            st += 0.5;
          }

          let end = parseInt(temp[i].END_TIME.split(':')[0], 10);
          if (parseInt(temp[i].END_TIME.split(':')[1], 10) === 30) {
            end += 0.5;
          }

          if (
            temp[i].END_TIME.split(':')[0] === '00' &&
            temp[i].END_TIME.split(':')[1] === '00'
          ) {
            end = 24;
          }

          for (let j = i + 1; j < temp.length; j++) {
            let daySt = parseInt(temp[j].START_TIME.split(':')[0], 10);
            if (parseInt(temp[j].START_TIME.split(':')[1], 10) === 30) {
              daySt += 0.5;
            }

            let dayEnd = parseInt(temp[j].END_TIME.split(':')[0], 10);
            if (parseInt(temp[j].END_TIME.split(':')[1], 10) === 30) {
              dayEnd += 0.5;
            }

            if (st >= daySt && st < dayEnd) {
              validRestrictions = false;
              break outerloop;
            } else if (daySt >= st && daySt < end) {
              validRestrictions = false;
              break outerloop;
            }
          }
        }

        if (validRestrictions) {
          this.buisnessRule.ACTIVE = true;
          this.buisnessRule.RESTRICTIONS = this.restrictionsDaily;
          this.dialogRef.close(this.buisnessRule);
        } else {
          this.bannerMessageService.errorNotifications.push({
            message: this.translateService.instant(
              'RESTRICTION_CANNOT_BE_EXCLUSIVE'
            )
          });
        }
      } else {
        this.bannerMessageService.errorNotifications.push({
          message: this.translateService.instant('RESTRICTION_CANNOT_BE_SAME')
        });
      }
    } else if (this.buisnessRule.RESTRICTION_TYPE === 'Week') {
      if (!this.checkForSameRestrictions(this.buisnessRule.RESTRICTION_TYPE)) {
        // NO SAME RESTRICTIONS
        const temp: any = this.sortRestrictions();
        let validRestrictions = true;
        outerloop: for (let i = 0; i < temp.length; i++) {
          const START_DAY = this.getDayValue(temp[i].startDay);
          const END_DAY = this.getDayValue(temp[i].endDay);

          let START_TIME = parseInt(temp[i].START_TIME.split(':')[0], 10);
          if (parseInt(temp[i].START_TIME.split(':')[1], 10) === 30) {
            START_TIME += 0.5;
          }

          let END_TIME = parseInt(temp[i].END_TIME.split(':')[0], 10);
          if (parseInt(temp[i].END_TIME.split(':')[1], 10) === 30) {
            END_TIME += 0.5;
          }

          const startDate = this.getDate();
          const endDate = this.getDate();

          startDate.setHours(parseInt(temp[i].START_TIME.split(':')[0], 10));
          startDate.setMinutes(parseInt(temp[i].START_TIME.split(':')[1], 10));

          endDate.setHours(parseInt(temp[i].END_TIME.split(':')[0], 10));
          endDate.setMinutes(parseInt(temp[i].END_TIME.split(':')[1], 10));

          if (START_DAY === 0 && END_DAY === 0 && START_TIME >= END_TIME) {
            startDate.setDate(
              startDate.getDate() + START_DAY - startDate.getDay()
            );
            endDate.setDate(endDate.getDate() + END_DAY + 7 - endDate.getDay());
          } else if (START_DAY !== startDate.getDay() && START_DAY > END_DAY) {
            startDate.setDate(
              startDate.getDate() + START_DAY - 7 - startDate.getDay()
            );
            endDate.setDate(endDate.getDate() + END_DAY - endDate.getDay());
          } else if (START_DAY === startDate.getDay() && START_DAY > END_DAY) {
            startDate.setDate(
              startDate.getDate() + START_DAY - startDate.getDay()
            );
            endDate.setDate(endDate.getDate() + END_DAY + 7 - endDate.getDay());
          } else if (START_DAY === END_DAY && START_TIME >= END_TIME) {
            startDate.setDate(
              startDate.getDate() + START_DAY - 7 - startDate.getDay()
            );
            endDate.setDate(endDate.getDate() + END_DAY - endDate.getDay());
          } else {
            startDate.setDate(
              startDate.getDate() + START_DAY - startDate.getDay()
            );
            endDate.setDate(endDate.getDate() + END_DAY - endDate.getDay());
          }

          for (let j = i + 1; j < temp.length; j++) {
            const J_START_DAY = this.getDayValue(temp[j].START_DAY);
            const J_END_DAY = this.getDayValue(temp[j].END_DAY);

            let J_START_TIME = parseInt(temp[j].START_TIME.split(':')[0], 10);
            if (parseInt(temp[j].START_TIME.split(':')[j], 10) === 30) {
              J_START_TIME += 0.5;
            }

            let J_END_TIME = parseInt(temp[j].END_TIME.split(':')[0], 10);
            if (parseInt(temp[j].END_TIME.split(':')[j], 10) === 30) {
              J_END_TIME += 0.5;
            }

            const stDate = this.getDate();
            const etDate = this.getDate();

            stDate.setHours(parseInt(temp[j].START_TIME.split(':')[0], 10));
            stDate.setMinutes(parseInt(temp[j].START_TIME.split(':')[1], 10));

            etDate.setHours(parseInt(temp[j].END_TIME.split(':')[0], 10));
            etDate.setMinutes(parseInt(temp[j].END_TIME.split(':')[1], 10));

            if (
              J_START_DAY === 0 &&
              J_END_DAY === 0 &&
              J_START_TIME >= J_END_TIME
            ) {
              stDate.setDate(stDate.getDate() + J_START_DAY - stDate.getDay());
              etDate.setDate(
                etDate.getDate() + J_END_DAY + 7 - etDate.getDay()
              );
            } else if (
              J_START_DAY > J_END_DAY ||
              (J_START_DAY === J_END_DAY && J_START_TIME >= J_END_TIME)
            ) {
              stDate.setDate(
                stDate.getDate() + J_START_DAY - 7 - stDate.getDay()
              );
              etDate.setDate(etDate.getDate() + J_END_DAY - etDate.getDay());
            } else {
              stDate.setDate(stDate.getDate() + J_START_DAY - stDate.getDay());
              etDate.setDate(etDate.getDate() + J_END_DAY - etDate.getDay());
            }

            if (startDate > stDate && startDate < etDate) {
              validRestrictions = false;
              break outerloop;
            } else if (stDate > startDate && stDate < endDate) {
              validRestrictions = false;
              break outerloop;
            }
          }
        }
        if (validRestrictions) {
          this.buisnessRule.ACTIVE = true;
          this.buisnessRule.RESTRICTIONS = this.restrictionsSpecific;
          this.dialogRef.close(this.buisnessRule);
        } else {
          this.bannerMessageService.errorNotifications.push({
            message: this.translateService.instant(
              'RESTRICTION_CANNOT_BE_EXCLUSIVE'
            )
          });
        }
      } else {
        this.bannerMessageService.errorNotifications.push({
          message: this.translateService.instant('RESTRICTION_CANNOT_BE_SAME')
        });
      }
    }
  }
}
