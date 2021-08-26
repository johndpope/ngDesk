import { Component, OnInit } from '@angular/core';
import { CompaniesService } from '../../companies/companies.service';
import { Theme } from '../../models/theme';

@Component({
  selector: 'app-page-customization',
  templateUrl: './page-customization.component.html',
  styleUrls: ['./page-customization.component.scss']
})
export class PageCustomizationComponent implements OnInit {
  public themeSelected: Theme = new Theme('', '', '', '', '', '', '', '', '');
  public themes: Theme[] = [];

  constructor(private companiesService: CompaniesService) { }

  public ngOnInit() {

    const yellowTheme = new Theme('yellow-theme', '#FFFF8D', '#FFFF00', '#FFEA00', '#FFEB3B', '#FDD835', '#FBC02D', '#F9A825', '#F57F17');
    const indigoTheme = new Theme('indigo-theme', '#E8EAF6', '#C5CBE9', '#9FA8DA', '#7985CB', '#5C6BC0', '#3F51B5', '#303F9F', '#1A237E');
    const greenTheme = new Theme('green-theme', '#b9f6ca', '#69f0ae', '#00e676', '#4caf50', '#43a047', '#388e3c', '#2e7d32', '#1b5e20');

    this.themes = [yellowTheme, indigoTheme, greenTheme];
    // function to get theme
    this.companiesService.getTheme()
      .subscribe(
        (response: any) => {
          this.themes.forEach((theme) => {
            if (theme.name === response.LOGOS_THEMES.NAME) {
              this.themeSelected = new Theme(theme.name, '#E8EAF6', '#C5CBE9', '#9FA8DA',
                '#7985CB', '#5C6BC0', '#3F51B5', '#3F51B5', '#3F51B5');
            }
          });
        },
        (error: any) => {
          console.log(error);
        }
      );
  }

  public selectTheme(theme) {
    this.themeSelected.name = theme.name;
  }

  public saveTheme() {
    console.log(this.themeSelected);
  }
}
