export class SignUpModel {
  constructor(
    public username: string,
    public email: string,
    private password: string,
  ) {}
}
