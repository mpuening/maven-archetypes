import { Project, ObjectLiteralExpression, Scope, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

//=========================================================

importRouterLinkIntoSideMenu();
setSideMenuComponentHTMLFile();

importSideMenuIntoMainFeatures();

setMeComponentFile();
setMeComponentHTMLFile();

setHelpComponentHTMLFile();

//=========================================================
//
// Import RouterLink to SideMenu Component
//
function importRouterLinkIntoSideMenu() {
	const sidemenuComponentFile = "$artifactId/src/app/features/main/sidemenu/sidemenu.component.ts";
	console.log("Updating " + sidemenuComponentFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths(sidemenuComponentFile);
	project.getSourceFiles().forEach((sourceFile) => {
		const SideMenuComponent = sourceFile.getClass('SidemenuComponent');
		if (SideMenuComponent != null) {
			//
			// Add import statements
			//
			sourceFile.addImportDeclaration({
				namedImports: ['RouterLink'],
				moduleSpecifier: '@angular/router'
			});

			//
			// Reference RouterLink in @Component.imports
			//
			const decorators = SideMenuComponent.getDecorators();
			decorators.forEach(decorator => {
				if (decorator.getText().startsWith("@Component")) {
					const args = decorator.getArguments();
					for (let i = 0; i < args.length; i++) {
						const config = args[i] as ObjectLiteralExpression;
						if (config != null) {
							config.getProperties().forEach(entry => {

								if (entry.getText().includes("imports")) {
									entry
										.getFirstChildByKind(SyntaxKind.ArrayLiteralExpression)
										?.addElement('RouterLink');
								}
							});
						}
					}
				}
			});
		}

		sourceFile.saveSync();
	});
}

//=========================================================
//
// Content for the SideMenu Component
//
function setSideMenuComponentHTMLFile() {

	const sidemenuComponentFile = "$artifactId/src/app/features/main/sidemenu/sidemenu.component.html";
	const sidemenuComponentContent = `
<div class="d-flex flex-column align-items-center align-items-sm-start px-3 pt-2 text-white min-vh-100">
    <ul class="nav nav-pills flex-column mb-sm-auto mb-0 align-items-center align-items-sm-start">
	      <li class="w-100" routerLinkActive="active">
	        <a class="nav-link" routerLink="/main/me">Me</a>
	      </li>
	      <li class="w-100" routerLinkActive="active">
	        <a class="nav-link" routerLink="/main/help">Help</a>
	      </li>
    </ul>
</div>
`;

	console.log("Updating " + sidemenuComponentFile);
	fs.writeFileSync(sidemenuComponentFile, sidemenuComponentContent);
}

//=========================================================
//
// Import SideMenu to Main Components
//
function importSideMenuIntoMainFeatures() {
	const meComponentFile = "$artifactId/src/app/features/main/me/me.component.ts";
	const helpComponentFile = "$artifactId/src/app/features/main/help/help.component.ts";
	console.log("Updating Main Features");

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths([ meComponentFile, helpComponentFile ]);
	project.getSourceFiles().forEach((sourceFile) => {
		let MainComponent = sourceFile.getClass('MeComponent');
		if (MainComponent == null) {
			MainComponent = sourceFile.getClass('HelpComponent');
		}

		if (MainComponent != null) {
			//
			// Add import statements
			//
			sourceFile.addImportDeclaration({
				namedImports: ['SidemenuComponent'],
				moduleSpecifier: '../sidemenu/sidemenu.component'
			});

			//
			// Reference RouterLink in @Component.imports
			//
			const decorators = MainComponent.getDecorators();
			decorators.forEach(decorator => {
				if (decorator.getText().startsWith("@Component")) {
					const args = decorator.getArguments();
					for (let i = 0; i < args.length; i++) {
						const config = args[i] as ObjectLiteralExpression;
						if (config != null) {
							config.getProperties().forEach(entry => {

								if (entry.getText().includes("imports")) {
									entry
										.getFirstChildByKind(SyntaxKind.ArrayLiteralExpression)
										?.addElement('SidemenuComponent');
								}
							});
						}
					}
				}
			});
		}

		sourceFile.saveSync();
	});
}

//=========================================================
//
// Content for the Me Component
//

// TODO: Instead of just replacing, should I add functions?
function setMeComponentFile() {

	const meComponentFile = "$artifactId/src/app/features/main/me/me.component.ts";

	console.log("Updating " + meComponentFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths([ meComponentFile ]);
	project.getSourceFiles().forEach((sourceFile) => {
		let MeComponent = sourceFile.getClass('MeComponent');

		if (MeComponent != null) {
			//
			// Add import statements
			//
			sourceFile.addImportDeclaration({
				namedImports: ['Observable'],
				moduleSpecifier: 'rxjs/internal/Observable'
			});
			sourceFile.addImportDeclaration({
				namedImports: ['HttpClient'],
				moduleSpecifier: '@angular/common/http'
			});
			sourceFile.addImportDeclaration({
				namedImports: ['AuthService'],
				moduleSpecifier: '../../../core/auth/auth.service'
			});

			//
			// Add New code
			//			
			const property = MeComponent.addProperty({
              isStatic: false,
              name: "payload",
              type: "string",
              initializer: "''"
            });

            const ctor = MeComponent.addConstructor({
              parameters: [{
                scope: Scope.Private,
                name: "http",
                type: "HttpClient"
              }, {
                scope: Scope.Private,
                name: "authService",
                type: "AuthService"
              }],
              //statements: "console.log('test');"
            });

            const showIdToken = MeComponent.addMethod({
              isStatic: false, name: "showIdToken", returnType: "void"
            });
            showIdToken.setBodyText(`
                let account = this.authService.getActiveAccount();
                if (account != null && account.idToken != null) {
                  this.payload = this.parseJwt(account.idToken);
                } else {
                  this.payload = "Not logged in"
                }
             `);

            const parseJwt = MeComponent.addMethod({
              isStatic: false, name: "parseJwt", returnType: "string", parameters: [{
                name: "token",
                type: "string"
              }]
            });
            parseJwt.setBodyText(`
                const parts = token.split('.');
                if (parts.length !== 3) {
                  return 'Invalid JWT format';
                }
                const decodedPayload = atob(parts[1]);
                const payload = JSON.parse(decodedPayload);
                const json = JSON.stringify(payload, null, 4);
                return json;
            `);

            const showMeEndpoint = MeComponent.addMethod({
              isStatic: false, name: "showMeEndpoint", returnType: "void"
            });
            showMeEndpoint.setBodyText(`
                this.me().subscribe({
                  next: (data) => {
                    this.payload = JSON.stringify(data, null, 4);
                  },
                  error: (error) => {
                    console.log(error);
                    this.payload = error.message;
                  }
                });
           `);

            const me = MeComponent.addMethod({
              isStatic: false, name: "me", returnType: "Observable<any>"
            });
            me.setBodyText(`
              return this.http.get<any>('/api/me');
            `);

		}
		sourceFile.organizeImports();
		sourceFile.formatText();
		sourceFile.saveSync();
	});
}

function setMeComponentHTMLFile() {

	const meComponentFile = "$artifactId/src/app/features/main/me/me.component.html";
	const meComponentContent = `
<div class="container-fluid">
    <div class="row flex-nowrap">
    	<div class="col-auto col-md-3 col-xl-2 px-sm-2 px-0 bg-dark">
    		<app-feature-main-sidemenu></app-feature-main-sidemenu>
 		</div>

        <div class="col py-3">
            <h3>Me works</h3>
            <p>
                <a href="javascript:void(0);" (click)="showIdToken()">Id Token</a> |
                <a href="javascript:void(0);" (click)="showMeEndpoint()">Me Endpoint</a>
            </p>
            <pre>
{{ payload }}
            </pre>
        </div>
    </div>
</div>
`;

	console.log("Updating " + meComponentFile);
	fs.writeFileSync(meComponentFile, meComponentContent);
}

//=========================================================
//
// Content for the Help Component
//
function setHelpComponentHTMLFile() {

	const helpComponentFile = "$artifactId/src/app/features/main/help/help.component.html";
	const helpComponentContent = `
<div class="container-fluid">
    <div class="row flex-nowrap">
    	<div class="col-auto col-md-3 col-xl-2 px-sm-2 px-0 bg-dark">
    		<app-feature-main-sidemenu></app-feature-main-sidemenu>
 		</div>

        <div class="col py-3">
            <h3>Help works</h3>
        </div>
    </div>
</div>
`;

	console.log("Updating " + helpComponentFile);
	fs.writeFileSync(helpComponentFile, helpComponentContent);
}
