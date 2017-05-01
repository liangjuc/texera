import { TextdbAngularGuiPage } from './app.po';

describe('textdb-angular-gui App', () => {
  let page: TextdbAngularGuiPage;

  beforeEach(() => {
    page = new TextdbAngularGuiPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
