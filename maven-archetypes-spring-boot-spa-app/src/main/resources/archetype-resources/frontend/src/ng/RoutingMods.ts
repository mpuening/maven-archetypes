import { Project, ObjectLiteralExpression, Scope, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

importFeaturesInAppRoutingFile();

//=========================================================
//
// Import Pages in App Routes File
//
function importFeaturesInAppRoutingFile() {
	const appRoutesFile = "$artifactId/src/app/app.routes.ts";
	console.log("Updating " + appRoutesFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});

	project.addSourceFilesAtPaths(appRoutesFile);
	project.getSourceFiles().forEach((sourceFile) => {

		sourceFile.addImportDeclaration({
			namedImports: ['Me'],
			moduleSpecifier: './features/main/me/me'
		});
		sourceFile.addImportDeclaration({
			namedImports: ['Help'],
			moduleSpecifier: './features/main/help/help'
		});

		sourceFile.addImportDeclaration({
			namedImports: ['Events'],
			moduleSpecifier: './features/admin/events/events'
		});
		sourceFile.addImportDeclaration({
			namedImports: ['Support'],
			moduleSpecifier: './features/admin/support/support'
		});
		
		const routes = sourceFile.getVariableDeclarationOrThrow("routes");
		const array = routes.getInitializerIfKindOrThrow(SyntaxKind.ArrayLiteralExpression);

		array.addElement(`{
			path: '', 
			redirectTo: '/main',
			pathMatch: 'full'
			}`);
		array.addElement(`{
			path: 'main', 
			redirectTo: '/main/me',
			pathMatch: 'full'
			}`);
		array.addElement(`{
			path: 'admin', 
			redirectTo: '/admin/events',
			pathMatch: 'full'
			}`);
		array.addElement(`{
			path: 'main', 
			children:  [{
				path: 'me', 
				component: Me
			}, {
				path: 'help', 
				component: Help
			}]
			}`);
		array.addElement(`{
			path: 'admin', 
			children:  [{
				path: 'events', 
				component: Events
			}, {
				path: 'support', 
				component: Support
			}]
			}`);

		routes.formatText();
		sourceFile.save();
	});
}
