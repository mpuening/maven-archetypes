import { Project, ObjectLiteralExpression, Scope, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

//=========================================================

importRouterLinkIntoSideMenu();
setSideMenuComponentHTMLFile();

importSideMenuIntoAdminFeatures();
setEventsComponentHTMLFile();
setSupportComponentHTMLFile();

//=========================================================
//
// Import RouterLink to SideMenu Component
//
function importRouterLinkIntoSideMenu() {
	const sidemenuComponentFile = "$artifactId/src/app/features/admin/sidemenu/sidemenu.component.ts";
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

	const sidemenuComponentFile = "$artifactId/src/app/features/admin/sidemenu/sidemenu.component.html";
	const sidemenuComponentContent = `
<div class="d-flex flex-column align-items-center align-items-sm-start px-3 pt-2 text-white min-vh-100">
    <ul class="nav nav-pills flex-column mb-sm-auto mb-0 align-items-center align-items-sm-start">
	      <li class="w-100" routerLinkActive="active">
	        <a class="nav-link" routerLink="/admin/events">Events</a>
	      </li>
	      <li class="w-100" routerLinkActive="active">
	        <a class="nav-link" routerLink="/admin/support">Support</a>
	      </li>
    </ul>
</div>
`;

	console.log("Updating " + sidemenuComponentFile);
	fs.writeFileSync(sidemenuComponentFile, sidemenuComponentContent);
}

//=========================================================
//
// Import SideMenu to Admin Components
//
function importSideMenuIntoAdminFeatures() {
	const eventsComponentFile = "$artifactId/src/app/features/admin/events/events.component.ts";
	const supportComponentFile = "$artifactId/src/app/features/admin/support/support.component.ts";
	console.log("Updating Admin Features");

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths([ eventsComponentFile, supportComponentFile ]);
	project.getSourceFiles().forEach((sourceFile) => {
		let AdminComponent = sourceFile.getClass('EventsComponent');
		if (AdminComponent == null) {
			AdminComponent = sourceFile.getClass('SupportComponent');
		}

		if (AdminComponent != null) {
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
			const decorators = AdminComponent.getDecorators();
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
// Content for the Events Component
//
function setEventsComponentHTMLFile() {

	const eventsComponentFile = "$artifactId/src/app/features/admin/events/events.component.html";
	const eventsComponentContent = `
<div class="container-fluid">
    <div class="row flex-nowrap">
    	<div class="col-auto col-md-3 col-xl-2 px-sm-2 px-0 bg-dark">
    		<app-feature-admin-sidemenu></app-feature-admin-sidemenu>
 		</div>

        <div class="col py-3">
            <h3>Events works</h3>
        </div>
    </div>
</div>
`;

	console.log("Updating " + eventsComponentFile);
	fs.writeFileSync(eventsComponentFile, eventsComponentContent);
}

//=========================================================
//
// Content for the Support Component
//
function setSupportComponentHTMLFile() {

	const supportComponentFile = "$artifactId/src/app/features/admin/support/support.component.html";
	const supportComponentContent = `
<div class="container-fluid">
    <div class="row flex-nowrap">
    	<div class="col-auto col-md-3 col-xl-2 px-sm-2 px-0 bg-dark">
    		<app-feature-admin-sidemenu></app-feature-admin-sidemenu>
 		</div>

        <div class="col py-3">
            <h3>Support works</h3>
        </div>
    </div>
</div>
`;

	console.log("Updating " + supportComponentFile);
	fs.writeFileSync(supportComponentFile, supportComponentContent);
}
