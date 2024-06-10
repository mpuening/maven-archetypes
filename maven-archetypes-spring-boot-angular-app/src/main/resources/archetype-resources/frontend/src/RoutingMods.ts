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
			namedImports: ['MeComponent'],
			moduleSpecifier: './features/main/me/me.component'
		});
		sourceFile.addImportDeclaration({
			namedImports: ['HelpComponent'],
			moduleSpecifier: './features/main/help/help.component'
		});

		sourceFile.addImportDeclaration({
			namedImports: ['EventsComponent'],
			moduleSpecifier: './features/admin/events/events.component'
		});
		sourceFile.addImportDeclaration({
			namedImports: ['SupportComponent'],
			moduleSpecifier: './features/admin/support/support.component'
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
				component: MeComponent
			}, {
				path: 'help', 
				component: HelpComponent
			}]
			}`);
		array.addElement(`{
			path: 'admin', 
			children:  [{
				path: 'events', 
				component: EventsComponent
			}, {
				path: 'support', 
				component: SupportComponent
			}]
			}`);

		routes.formatText();
		sourceFile.save();
	});
}
