import { LoginPage } from './login.po';
import { browser } from 'protractor';

describe('workspace-project App', () => {
  let page: LoginPage;

  beforeEach(() => {
    page = new LoginPage();
  });

  it('login success', () => {
    page.navigateTo();
    page.setEmailInput();
    page.setPasswordInput();
    const loginButton = page.getLoginButton();
    loginButton.click();
    expect(browser.getCurrentUrl()).toContain('/dashboard');

  });
});
