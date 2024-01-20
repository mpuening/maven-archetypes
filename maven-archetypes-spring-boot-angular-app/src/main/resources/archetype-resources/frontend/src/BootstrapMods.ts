import { Project, Node, SyntaxKind } from 'ts-morph';
import * as fs from 'fs';

//=========================================================

const stylesCSSFile = "$artifactId/src/styles.css";
const bootstrapCSS = `
@import "bootstrap/dist/css/bootstrap.min.css";
@import "bootstrap-icons/font/bootstrap-icons.min.css";
`

//=========================================================
//
// Modify Angular styles.css File
//
console.log("Updating " + stylesCSSFile);
fs.appendFileSync(stylesCSSFile, bootstrapCSS);

//=========================================================
//
// Test Home Page
//
const appPageFile = "$artifactId/src/app/app.component.html";
const appPageContent = `
<h3>Spring Boot Angular App</h3>
<div class="input-group w-50">
	<span class="input-group-text" id="basic-addon1">
		<i class="bi bi-search"></i>
	</span>
	<input type="text" class="form-control" placeholder="Input example" aria-label="Input example"
		aria-describedby="basic-addon1">
	<button type="button" class="btn btn-primary">
		<i class="bi bi-search"></i> Search
	</button>
</div>
`;

console.log("Updating " + appPageFile);
fs.writeFileSync(appPageFile, appPageContent);
