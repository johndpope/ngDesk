export class ChatFaq {
  constructor(
    public NAME: string,
    public DESCRIPTION: string,
    public QUESTIONS: String[],
    public ANSWERS: String[],
    public MODULES: String[]
  ) {}
  public get name() {
    return this.NAME;
  }
  public set name(NAME: string) {
    this.NAME = NAME;
  }
  public get description() {
    return this.DESCRIPTION;
  }
  public set description(DESCRIPTION: string) {
    this.DESCRIPTION = DESCRIPTION;
  }
  public get answers() {
    return this.ANSWERS;
  }
  public set answers(ANSWERS: String[]) {
    this.ANSWERS = ANSWERS;
  }
  public get question() {
    return this.QUESTIONS;
  }
  public set question(QUESTIONS: String[]) {
    this.QUESTIONS = QUESTIONS;
  }

  public get moduleId() {
    return this.MODULES;
  }
  public set moduleId(MODULE_ID: String[]) {
    this.MODULES = MODULE_ID;
  }
}
