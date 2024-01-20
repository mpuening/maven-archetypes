import { Project, Node, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

//=========================================================
// See https://tailwindcss.com/docs/guides/angular

const tailwindConfigFile = "$artifactId/tailwind.config.js";
const tailwindConfigContent = '"./src/**/*.{html,ts}"';

const stylesCSSFile = "$artifactId/src/styles.css";
const tailwindCSS = `
@tailwind base;
@tailwind components;
@tailwind utilities;
`;

//=========================================================
//
// Modify Tailwind Config File
//
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
	sourceFile.save();
});

//=========================================================
//
// Modify Angular styles.css File
//
console.log("Updating " + stylesCSSFile);
fs.appendFileSync(stylesCSSFile, tailwindCSS);

//=========================================================
//
// Test Home Page
//
const appPageFile = "$artifactId/src/app/app.component.html";
const appPageContent = `
<h1 class="text-3xl font-bold underline">
  Hello world!
</h1>
`;

console.log("Updating " + appPageFile);
fs.writeFileSync(appPageFile, appPageContent);
