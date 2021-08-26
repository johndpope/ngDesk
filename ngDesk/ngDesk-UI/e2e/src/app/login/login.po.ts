import { browser, by, element } from 'protractor';

export class LoginPage {
  navigateTo() {
    return browser.get('/login');
  }

  setEmailInput() {
    return element(by.name('email')).sendKeys('spencer@allbluesolutions.com');
  }

  setPasswordInput() {
    return element(by.name('password')).sendKeys('Pn35x2dUW@');
  }

  getLoginButton() {
    return element(by.id('login-button'));
  }
}
