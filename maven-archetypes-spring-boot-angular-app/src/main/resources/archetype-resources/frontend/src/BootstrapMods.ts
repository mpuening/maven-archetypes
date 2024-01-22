import * as fs from 'fs';

//=========================================================

updateAngularJSONFileToBundleBootstrap();

setMainAppWithTestPage();

//=========================================================
//
// Update angular.json file so that Bootstrap is bundled in the build
//
function updateAngularJSONFileToBundleBootstrap() {
	const angularJSONFile = "$artifactId/angular.json";

	console.log("Updating " + angularJSONFile);

	const angular = JSON.parse(fs.readFileSync(angularJSONFile, "utf-8"));
	const project = angular.projects["$artifactId"];

	const styles = project.architect.build.options.styles;
	styles.push("bootstrap/dist/css/bootstrap.min.css");
	styles.push("bootstrap-icons/font/bootstrap-icons.min.css");

	const scripts = project.architect.build.options.scripts;
	scripts.push("node_modules/bootstrap/dist/js/bootstrap.min.js");

	fs.writeFileSync(angularJSONFile, JSON.stringify(angular, null, 2));
}

//=========================================================
//
// Test page to see if config is working
//
function setMainAppWithTestPage() {
	const appPageFile = "$artifactId/src/app/app.component.html";
	const appPageContent = `
<h3>Spring Boot Angular App</h3>
<div class="input-group w-50">
	<span class="input-group-text" id="basic-addon1">
		<i class="bi bi-search"></i>
	</span>
	<input type="text" class="form-control" placeholder="Input example" aria-label="Input example" aria-describedby="basic-addon1">
	<button type="button" class="btn btn-primary">
		<i class="bi bi-search"></i> Search
	</button>
</div>
`;

	console.log("Updating " + appPageFile);
	fs.writeFileSync(appPageFile, appPageContent);
}
