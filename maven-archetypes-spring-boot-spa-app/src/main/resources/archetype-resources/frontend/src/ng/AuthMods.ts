import { Project, ArrayLiteralExpression, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

createAuthConfig();
updateAuthService();
importAuthConfigIntoAppConfig();
setLoginLogoutButtonComponent();
setLoginLogoutButtonComponentHTMLFile();

//=========================================================
//
// Content for the Auth Config
//
function createAuthConfig() {
	const configDir = "$artifactId/src/app/core/auth";
	const configFile = "$artifactId/src/app/core/auth/auth.config.ts";
	const configContent = `
import {
  LogLevel,
  Configuration,
  BrowserCacheLocation,
} from '@azure/msal-browser';

import {
  IPublicClientApplication,
  PublicClientApplication,
  InteractionType
} from '@azure/msal-browser';

import {
  MsalInterceptorConfiguration,
  MsalGuardConfiguration,
} from '@azure/msal-angular';

import { getApplicationConfigValue } from '../config/config';

function msalConfig() {
  const msalConfig: Configuration = {
    auth: {
      //clientId: 'app-client',
      //authority: 'https://127.0.0.1:8443',
      //knownAuthorities: ['https://127.0.0.1:8443'],
      //protocolMode: 'OIDC',
      //redirectUri: 'https://localhost:8443',
      //postLogoutRedirectUri: 'https://127.0.0.1:8443/oauth2/logged-out'
      clientId: getApplicationConfigValue("clientId"),
      authority: getApplicationConfigValue("authority"),
      knownAuthorities: getApplicationConfigValue("knownAuthorities"),
      protocolMode: getApplicationConfigValue("protocolMode"),
      redirectUri: getApplicationConfigValue("redirectUri"),
      postLogoutRedirectUri: getApplicationConfigValue("postLogoutRedirectUri")
    },
    cache: {
      cacheLocation: BrowserCacheLocation.LocalStorage,
    },
    system: {
      loggerOptions: {
        loggerCallback(logLevel: LogLevel, message: string) {
          console.log(message);
        },
        logLevel: LogLevel.Verbose,
        piiLoggingEnabled: false,
      },
    },
  };
  return  msalConfig;
}


export function MSALInstanceFactory(): IPublicClientApplication {
  return new PublicClientApplication(msalConfig());
}

export function MsalGuardConfigurationFactory(): MsalGuardConfiguration {
  return {
    interactionType: InteractionType.Redirect,
    authRequest: {
      //scopes: [ "openid" ]
      scopes: getApplicationConfigValue("scopes")
	}
  };
}

export function MSALInterceptorConfigFactory(): MsalInterceptorConfiguration {
  const protectedResourceMap = new Map<string, Array<string>>();
  // FIXME
  protectedResourceMap.set("/", ["openid"]);

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };
}
`;

	console.log("Updating " + configFile);
	fs.mkdirSync(configDir, { recursive: true });
	fs.writeFileSync(configFile, configContent);

}

//=========================================================
//
// Content for the Auth Service
//
function updateAuthService() {
	const serviceDir = "$artifactId/src/app/core/auth";
	const serviceFile = "$artifactId/src/app/core/auth/auth.service.ts";
	const serviceContent = `
import { Inject, Injectable } from '@angular/core';

import {
  MsalService,
  MsalBroadcastService,
  MSAL_GUARD_CONFIG,
  MsalGuardConfiguration,
} from '@azure/msal-angular';
import {
  AuthenticationResult,
  EventMessage,
  EventType,
  InteractionStatus,
  PopupRequest,
  RedirectRequest
} from '@azure/msal-browser';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  isLoggedIn = false;
  isIframe = false;

  private readonly _destroying$ = new Subject<void>();

  constructor(
    @Inject(MSAL_GUARD_CONFIG) private msalGuardConfig: MsalGuardConfiguration,
    private msalService: MsalService,
    private msalBroadcastService: MsalBroadcastService,
  ) {
    this.start();
  }

  start() {
	  this.msalService.handleRedirectObservable().subscribe();

    this.setIFrame();
    this.setLoggedIn();
    this.msalService.instance.enableAccountStorageEvents();

    this.msalBroadcastService.msalSubject$
      .pipe(
        filter(
          (msg: EventMessage) =>
            msg.eventType === EventType.ACCOUNT_ADDED ||
            msg.eventType === EventType.ACCOUNT_REMOVED
        )
      )
      .subscribe((result: EventMessage) => {
        if (this.msalService.instance.getAllAccounts().length === 0) {
          window.location.pathname = '/';
        } else {
          this.setLoggedIn();
        }
      });

    this.msalBroadcastService.inProgress$
      .pipe(
        filter(
          (status: InteractionStatus) => status === InteractionStatus.None
        ),
        takeUntil(this._destroying$)
      )
      .subscribe(() => {
        this.setLoggedIn();
        this.checkAndSetActiveAccount();
      });

    /*
    this.msalBroadcastService.inProgress$
      .pipe(
        filter(
          (status: InteractionStatus) => status === InteractionStatus.None
        ),
        takeUntil(this._destroying$)
      )
      .subscribe(() => {
        this.setLoginDisplay();
        this.checkAndSetActiveAccount();
      });

    this.msalBroadcastService.msalSubject$
      .pipe(
        filter(
          (msg: EventMessage) => msg.eventType === EventType.LOGOUT_SUCCESS
        ),
        takeUntil(this._destroying$)
      )
      .subscribe((result: EventMessage) => {
        this.setLoginDisplay();
        this.checkAndSetActiveAccount();
      });

    this.msalBroadcastService.msalSubject$
      .pipe(
        filter(
          (msg: EventMessage) => msg.eventType === EventType.LOGIN_SUCCESS
        ),
        takeUntil(this._destroying$)
      )
      .subscribe((result: EventMessage) => {
        const payload = result.payload as AuthenticationResult;
        this.authService.instance.setActiveAccount(payload.account);
      });
    */
  }

  setIFrame() {
    this.isIframe = window !== window.parent && !window.opener;
  }

  setLoggedIn() {
    this.isLoggedIn = this.msalService.instance.getAllAccounts().length > 0;
  }

  checkAndSetActiveAccount() {
    let activeAccount = this.msalService.instance.getActiveAccount();

    if (!activeAccount && this.msalService.instance.getAllAccounts().length > 0) {
      let accounts = this.msalService.instance.getAllAccounts();
      this.msalService.instance.setActiveAccount(accounts[0]);
    }
  }

  login() {
    this.loginRedirect();
  }

  loginRedirect() {
    if (this.msalGuardConfig.authRequest) {
      this.msalService.loginRedirect({
        ...this.msalGuardConfig.authRequest,
      } as RedirectRequest);
    } else {
      this.msalService.loginRedirect();
    }
  }

  loginPopup() {
    if (this.msalGuardConfig.authRequest) {
      this.msalService
        .loginPopup({ ...this.msalGuardConfig.authRequest } as PopupRequest)
        .subscribe((response: AuthenticationResult) => {
          this.msalService.instance.setActiveAccount(response.account);
        });
    } else {
      this.msalService
        .loginPopup()
        .subscribe((response: AuthenticationResult) => {
          this.msalService.instance.setActiveAccount(response.account);
        });
    }
  }

  logout(popup?: boolean) {
    let account = this.msalService.instance.getActiveAccount();
    if (popup) {
      this.msalService.logoutPopup({
        mainWindowRedirectUri: '/',
        idTokenHint: account?.idToken
      });
    } else {
      this.msalService.logoutRedirect({
        idTokenHint: account?.idToken
      });
    }
  }

  stop(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }

}
`;

	console.log("Updating " + serviceFile);
	fs.mkdirSync(serviceDir, { recursive: true });
	fs.writeFileSync(serviceFile, serviceContent);
}

//=========================================================
//
// Add MSAL providers to app config
//
function importAuthConfigIntoAppConfig() {
	const appConfigFile = "$artifactId/src/app/app.config.ts";
	console.log("Updating " + appConfigFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});
	project.addSourceFilesAtPaths(appConfigFile);
	project.getSourceFiles().forEach((sourceFile) => {
		//
		// Add import statements
		//
		sourceFile.addImportDeclaration({
			namedImports: [
				'HTTP_INTERCEPTORS',
				'provideHttpClient',
				'withInterceptorsFromDi'],
			moduleSpecifier: '@angular/common/http'
		});

		sourceFile.addImportDeclaration({
			namedImports: [
				'MSAL_GUARD_CONFIG',
				'MSAL_INSTANCE',
				'MSAL_INTERCEPTOR_CONFIG',
				'MsalBroadcastService',
				'MsalGuard',
				'MsalInterceptor',
				'MsalService'],
			moduleSpecifier: '@azure/msal-angular'
		});

		sourceFile.addImportDeclaration({
			namedImports: [
				'MSALInstanceFactory',
				'MsalGuardConfigurationFactory',
				'MSALInterceptorConfigFactory'],
			moduleSpecifier: './core/auth/auth.config'
		});

		// Append new providers
		const appConfig = sourceFile.getVariableDeclarationOrThrow("appConfig");
		const json = appConfig.getInitializerIfKindOrThrow(SyntaxKind.ObjectLiteralExpression);
		const providers = json.getPropertyOrThrow("providers");
		const array = providers.getDescendantsOfKind(SyntaxKind.ArrayLiteralExpression)[0];

		array.addElement(`provideHttpClient(withInterceptorsFromDi())`);
		array.addElement(`{
        	provide: HTTP_INTERCEPTORS,
        	useClass: MsalInterceptor,
        	multi: true
    	}`);
		array.addElement(`{
	        provide: MSAL_INSTANCE,
	        useFactory: MSALInstanceFactory
	    }`);
		array.addElement(`{
	        provide: MSAL_GUARD_CONFIG,
	        useFactory: MsalGuardConfigurationFactory
	    }`);
		array.addElement(`{
	        provide: MSAL_INTERCEPTOR_CONFIG,
	        useFactory: MSALInterceptorConfigFactory
	    }`);
		array.addElement(`MsalService`);
		array.addElement(`MsalGuard`);
		array.addElement(`MsalBroadcastService`);

		appConfig.formatText();
		sourceFile.organizeImports();
		sourceFile.save();
	});

}

//=========================================================
//
// Content for the Login Logout Button Component
//
function setLoginLogoutButtonComponent() {

	const loginLogoutButtonComponentFile = "$artifactId/src/app/core/auth/login-logout-button.component.ts";
	const loginLogoutButtonComponentContent = `
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthService } from './auth.service';

@Component({
  selector: 'app-login-logout-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login-logout-button.component.html',
  styleUrl: './login-logout-button.component.css'
})
export class LoginLogoutButtonComponent {

  constructor(private authService: AuthService) {
  }

  isLoggedIn() {
    return this.authService.isLoggedIn;
  }

  login() {
    this.authService.login();
    return false;
  }

  logout() {
    this.authService.logout();
    return false;
  }

}
`;

	console.log("Updating " + loginLogoutButtonComponentFile);
	fs.writeFileSync(loginLogoutButtonComponentFile, loginLogoutButtonComponentContent);

}

//=========================================================
//
// Content for the Login Logout Button HTML File
//
function setLoginLogoutButtonComponentHTMLFile() {

	const loginLogoutButtonComponentFile = "$artifactId/src/app/core/auth/login-logout-button.component.html";
	const loginLogoutButtonComponentContent = `
<button *ngIf="!isLoggedIn()" (click)="login()">Login</button>
<button *ngIf="isLoggedIn()" (click)="logout()">Logout</button>
`;

	console.log("Updating " + loginLogoutButtonComponentFile);
	fs.writeFileSync(loginLogoutButtonComponentFile, loginLogoutButtonComponentContent);
}
