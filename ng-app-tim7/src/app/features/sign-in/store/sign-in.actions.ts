import { Action } from '@ngrx/store';

export const SIGN_IN = '[Auth] Sign in';
export const SIGN_OUT = '[Auth] Sign out';
export const AUTHENTICATE_SUCCESS = '[Auth] Sign-in success';
export const AUTHENTICATE_FAIL = '[Auth] Sign-in fail';
export const CLEAR_ERROR = '[Auth] Clear Error';

export class AuthenticateSuccess implements Action {
  readonly type = AUTHENTICATE_SUCCESS;

  constructor(
    public payload: {
      username: string;
      id: number;
      accessToken: string,
      role: string
    }
  ) {}
}

export class SignInStart implements Action {
  readonly type = SIGN_IN;

  constructor(public payload: { username: string; password: string }) {}
}

export class SignOut implements Action {
  readonly type = SIGN_OUT;
}

export class AuthenticateFail implements Action {
  readonly type = AUTHENTICATE_FAIL;

  constructor(public payload: string) {}
}

export class ClearError implements Action {
  readonly type = CLEAR_ERROR;
}

export type SignInActions =
  | SignInStart
  | AuthenticateSuccess
  | AuthenticateFail
  | ClearError
  | SignOut;
