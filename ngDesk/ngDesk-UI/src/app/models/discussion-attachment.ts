export class DiscussionAttachment {
  public FILE_EXTENSION: string;
  public FILE: string;
  public FILE_NAME: string;

  constructor(fileExtension, file, filename) {
    this.FILE_EXTENSION = fileExtension;
    this.FILE = file;
    this.FILE_NAME = filename;
  }
}
