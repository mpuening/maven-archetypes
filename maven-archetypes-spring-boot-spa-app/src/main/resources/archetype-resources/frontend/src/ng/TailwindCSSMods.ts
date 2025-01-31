import { Project, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

//=========================================================
// See https://tailwindcss.com/docs/guides/angular

updateTailwindConfigFileForAngularApps();

updateTailwindCSSToStylesCSSFile();

setMainAppWithTestPage();

//=========================================================
//
// Update Tailwind config file so that it knows what to process
//
function updateTailwindConfigFileForAngularApps() {
	const tailwindConfigFile = "$artifactId/tailwind.config.js";
	const tailwindConfigContent = '"./src/**/*.{html,ts}"';

	console.log("Updating " + tailwindConfigFile);
	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths(tailwindConfigFile);
	project.getSourceFiles().forEach((sourceFile) => {

		const stmts = sourceFile.getStatements();
		stmts.forEach(assignment => {
			assignment.forEachChild(lhs_eq_rhs => {
				const rhs = lhs_eq_rhs.getFirstChildByKind(SyntaxKind.ObjectLiteralExpression);
				if (rhs != null) {
					rhs.getProperties().forEach(entry => {
						if (entry.getText().includes("content")) {
							entry
								.getFirstChildByKind(SyntaxKind.ArrayLiteralExpression)
								?.addElement(tailwindConfigContent);
						}
					});
				}
			});
		});

		// An alternative way to consider parsing file... (not tried yet)
		// // Example Code : const myprops = {property1: 20, property2: 30}
		//const dec = sourceFile.getVariableDeclarationOrThrow('myprops');
		//const objectLiteralExpression = dec.getInitializerIfKindOrThrow(SyntaxKind.ObjectLiteralExpression);
		//console.log(objectLiteralExpression.getProperties()); // output here	

		sourceFile.save();
	});
}
//=========================================================
//
// Update Angular styles.css file to import Tailwind CSS
//
function updateTailwindCSSToStylesCSSFile() {
	const stylesCSSFile = "$artifactId/src/styles.css";
	const tailwindCSS = `
@tailwind base;
@tailwind components;
@tailwind utilities;
`;

	console.log("Updating " + stylesCSSFile);
	fs.appendFileSync(stylesCSSFile, tailwindCSS);
}

//=========================================================
//
// Test page to see if config is working
//
function setMainAppWithTestPage() {
	const appPageFile = "$artifactId/src/app/app.component.html";
	const appPageContent = `
<h1 class="text-3xl font-bold underline">
  Hello world!
</h1>
`;

	console.log("Updating " + appPageFile);
	fs.writeFileSync(appPageFile, appPageContent);
}
