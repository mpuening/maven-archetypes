import { Project } from 'ts-morph';
import * as fs from 'fs';


createAppConfigUtilities();
importAppConfigIntoMainFile();

//=========================================================
//
// App Config utility functions
//
function createAppConfigUtilities() {
	const configDir = "$artifactId/src/app/core/config";
	const configFile = "$artifactId/src/app/core/config/config.ts";
	const configContent = `
export function loadApplicationConfig() {
  const request = new XMLHttpRequest();
  const async = false;
  request.open("GET", "/api/config", async);
  request.send(null);

  if (request.status === 200) {
    window.sessionStorage.setItem('appConfig', request.responseText);
    console.log('App configuration loaded...');
  }
  else {
    console.log('Error loading configuration: Status code: \${request.status}');
  }
}

export function getApplicationConfigValue(key: string) {
  try {
    const json = window.sessionStorage.getItem('appConfig');
    const config = json != null ? JSON.parse(json) : {};
    const value = config[key];
    return value;
  } catch (error) {
    return null;
  }
}
`;

	console.log("Updating " + configFile);
	fs.mkdirSync(configDir, { recursive: true });
	fs.writeFileSync(configFile, configContent);
}

function importAppConfigIntoMainFile() {
	const mainFile = "$artifactId/src/main.ts";
	console.log("Updating " + mainFile);

	const project = new Project({
		tsConfigFilePath: './tsconfig.json',
		skipAddingFilesFromTsConfig: true,
	});
	project.addSourceFilesAtPaths(mainFile);
	project.getSourceFiles().forEach((sourceFile) => {
		//
		// Add import statements
		//
		sourceFile.addImportDeclaration({
			namedImports: ['loadApplicationConfig'],
			moduleSpecifier: './app/core/config/config'
		});

		// Index should be the number of import statemments to be first stmt in file
		sourceFile.insertStatements(4, "\nloadApplicationConfig();\n");

		sourceFile.save();
	});
}
