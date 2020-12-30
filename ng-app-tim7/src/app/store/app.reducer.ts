import {ActionReducer, ActionReducerMap, MetaReducer} from '@ngrx/store';
import { localStorageSync } from 'ngrx-store-localstorage';

import * as fromAuth from '../components/sign-in/store/sign-in.reducer';
import * as fromSignUp from '../components/sign-up/store/sign-up.reducer';
import * as fromActivate from '../components/activate-account/store/activate-account.reducer';
import * as fromAdmin from '../components/administrator/store/administrator.reducer';

export interface AppState {
  auth: fromAuth.State;
  signUp: fromSignUp.State;
  activate: fromActivate.State;
  administrator: fromAdmin.State;
}

export const appReducer: ActionReducerMap<AppState> = {
  auth: fromAuth.signInReducer,
  signUp: fromSignUp.signUpReducer,
  activate: fromActivate.activateAccountReducer,
  administrator: fromAdmin.administratorReducer
};

const reducerKeys = ['user'];
export function localStorageSyncReducer(reducer: ActionReducer<any>): ActionReducer<any> {
  return localStorageSync({keys: reducerKeys})(reducer);
}
export const metaReducers: MetaReducer<AppState>[] = [localStorageSyncReducer];
