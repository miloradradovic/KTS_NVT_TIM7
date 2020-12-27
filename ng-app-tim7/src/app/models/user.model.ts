export class User {
  constructor(
    public username: string,
    public id: string,
    private token: string,
    private tokenExpirationDate: Date
  ) {}

  get getToken() {
    if (!this.tokenExpirationDate || new Date() > this.tokenExpirationDate) {
      return null;
    }
    return this.token;
  }
}
