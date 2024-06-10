import { Project, ObjectLiteralExpression, Scope, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

//=========================================================

importHeaderAndFooterInAppComponentFile();

// Not using this approach. 
//setBodyClassesInAppComponentFile();

importRouterLinkIntoHeader();

setHeaderComponentHTMLFile();
setFooterComponentHTMLFile();
setFooterComponentCSSFile();

setMainAppWithLayoutElements();

//=========================================================
//
// Import RouterLink to Header Component
//
function importRouterLinkIntoHeader() {
	const headerComponentFile = "$artifactId/src/app/core/layout/header/header.component.ts";
	console.log("Updating " + headerComponentFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths(headerComponentFile);
	project.getSourceFiles().forEach((sourceFile) => {
		const HeaderComponent = sourceFile.getClass('HeaderComponent');
		if (HeaderComponent != null) {
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
			const decorators = HeaderComponent.getDecorators();
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
// Import Header and Footer in App Component File
//
function importHeaderAndFooterInAppComponentFile() {
	const appComponentFile = "$artifactId/src/app/app.component.ts";
	console.log("Updating " + appComponentFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths(appComponentFile);
	project.getSourceFiles().forEach((sourceFile) => {

		const AppComponent = sourceFile.getClass('AppComponent');
		if (AppComponent != null) {
			//
			// Add import statements
			//
			sourceFile.addImportDeclaration({
				namedImports: ['HeaderComponent'],
				moduleSpecifier: './core/layout/header/header.component'
			});
			sourceFile.addImportDeclaration({
				namedImports: ['FooterComponent'],
				moduleSpecifier: './core/layout/footer/footer.component'
			});

			//
			// Reference Header and Footer in @Component.imports
			//
			const decorators = AppComponent.getDecorators();
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
										?.addElement('HeaderComponent');
									entry
										.getFirstChildByKind(SyntaxKind.ArrayLiteralExpression)
										?.addElement('FooterComponent');
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
// Set Body classes via App Component File. The classes set in this
// method match these found on this page:
//
// https://getbootstrap.com/docs/5.3/examples/sticky-footer-navbar/
//
// However, this approach didn't work since the footer element is
// too deeply nested fot the CSS to match. Instead use other simple CSS
function setBodyClassesInAppComponentFile() {
	const appComponentFile = "$artifactId/src/app/app.component.ts";
	console.log("Updating " + appComponentFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths(appComponentFile);
	project.getSourceFiles().forEach((sourceFile) => {

		const AppComponent = sourceFile.getClass('AppComponent');
		if (AppComponent != null) {
			//
			// Add C'tor to set document.body classes
			//
			sourceFile.addImportDeclaration({
				namedImports: ['Inject'],
				moduleSpecifier: '@angular/core'
			});
			sourceFile.addImportDeclaration({
				namedImports: ['DOCUMENT'],
				moduleSpecifier: '@angular/common'
			});
			const ctor = AppComponent.addConstructor();
			ctor.addParameter({
				decorators: [{
					name: "Inject",
					arguments: ["DOCUMENT"]
				}],
				name: "document",
				type: "any",
				scope: Scope.Private
			});
			ctor.addStatements([
				// d-flex flex-column h-100"
				"// Classes to support Bootstrap footers",
				"this.document.body.classList.add('d-flex');",
				"this.document.body.classList.add('flex-column');",
				"this.document.body.classList.add('h-100');",
				"this.document.html.classList.add('h-100');"
			]);
		}

		sourceFile.save();
		sourceFile.organizeImports();
		sourceFile.save();
	});
}

//=========================================================
//
// Content for the Header Component
//
function setHeaderComponentHTMLFile() {

	const headerComponentFile = "$artifactId/src/app/core/layout/header/header.component.html";
	const headerComponentContent = `
<header>
  <nav class="navbar navbar-expand-md navbar-dark bg-dark">
    <div class="container-fluid">
      <a class="navbar-brand" href="#">App Name</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse" aria-controls="navbarCollapse" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarCollapse">
        <ul class="navbar-nav me-auto mb-2 mb-md-0">
          <li class="nav-item" routerLinkActive="active">
            <a class="nav-link" routerLink="/main">Main</a>
          </li>
          <li class="nav-item" routerLinkActive="active">
            <a class="nav-link" routerLink="/admin">Admin</a>
          </li>
        </ul>
        <form class="d-flex" role="logout">
          <button class="btn btn-outline-success" type="submit">Logout</button>
        </form>
      </div>
    </div>
  </nav>
</header>
`;

	console.log("Updating " + headerComponentFile);
	fs.writeFileSync(headerComponentFile, headerComponentContent);
}

//=========================================================
//
// Content for the Footer Component
//
function setFooterComponentHTMLFile() {
	const footerComponentFile = "$artifactId/src/app/core/layout/footer/footer.component.html";
	const footerComponentContent = `
<p>&nbsp;</p>
<footer class="footer mt-auto bg-body-tertiary">
  <div class="container">
    <span class="text-body-secondary">Thank you.</span>
  </div>
</footer>
`;

	console.log("Updating " + footerComponentFile);
	fs.writeFileSync(footerComponentFile, footerComponentContent);
}

//=========================================================
//
// Content for the Footer Component
//
function setFooterComponentCSSFile() {
	const footerCSSFile = "$artifactId/src/app/core/layout/footer/footer.component.css";
	const footerCSSContent = `
footer {
  position:fixed;
  bottom:0px;
  width:100%;
}`;

	console.log("Updating " + footerCSSFile);
	fs.writeFileSync(footerCSSFile, footerCSSContent);
}

//=========================================================
//
// Main Page
//
function setMainAppWithLayoutElements() {
	const appPageFile = "$artifactId/src/app/app.component.html";
	const appPageContent = `
<app-layout-header></app-layout-header>
<main>
    <router-outlet></router-outlet>
</main>
<app-layout-footer></app-layout-footer>
`;

	console.log("Updating " + appPageFile);
	fs.writeFileSync(appPageFile, appPageContent);
}