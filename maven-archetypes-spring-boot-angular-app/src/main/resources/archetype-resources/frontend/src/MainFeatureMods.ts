import { Project, ObjectLiteralExpression, Scope, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

//=========================================================

importRouterLinkIntoSideMenu();
setSideMenuComponentHTMLFile();

importSideMenuIntoMainFeatures();
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

		sourceFile.save();
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

		sourceFile.save();
	});
}

//=========================================================
//
// Content for the Me Component
//
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
