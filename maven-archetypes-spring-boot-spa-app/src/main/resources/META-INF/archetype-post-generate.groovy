//
// Spring Boot SPA App Post Processor
//
println()
println("Beginning Spring Boot SPA App Post Processor")
println()

// ==============================================

final PARENT_DIR = new File(request.outputDirectory, request.artifactId)
final FRONTEND_DIR = new File(PARENT_DIR, (request.artifactId + "-frontend"))
final BACKEND_DIR = new File(PARENT_DIR, (request.artifactId + "-backend"))

final SPA_PROJECT_DIR = new File(FRONTEND_DIR, request.artifactId)

final NODE_DIR = FRONTEND_DIR.getAbsolutePath() + java.io.File.separator + "node"

// ==============================================

def runProcess(String cmd, File dir, java.util.List<String> extraPaths) {
    def envMap = new java.util.HashMap<String, String>(System.env)
    def PATH = envMap.get("PATH");
    for (String path : extraPaths) {
    	PATH = path + java.io.File.pathSeparator + PATH;
    }
    envMap.put("PATH", PATH);
    def env = envMap.entrySet().stream().map {
        entry -> "${entry.getKey()}=${entry.getValue()}"
    }.collect(java.util.stream.Collectors.toList())

    def process = cmd.execute(env, dir)
    process.waitForProcessOutput((Appendable)System.out, System.err)
    if (process.exitValue() != 0) {
        throw new Exception("Command '$cmd' failed with code: ${process.exitValue()}")
    }
}

def runProcess(String cmd, File dir) {
    runProcess(cmd, dir, [])
}

// ==============================================

// Determine commands: Unix / Windows flavors

def __mvn = "sh ../mvnw"
def __npm = "./node/npm"
def __npx = "./node/npx"
def __ng   = "./node/node ./node_modules/@angular/cli/bin/ng.js"

// dot dot commands when running in sub-directory
def __dd_npm = "../node/npm"
def __dd_ng  = "../node/node ../node_modules/@angular/cli/bin/ng.js"

if (System.properties['os.name'].toLowerCase().contains('windows')) {
    __mvn = "cmd /C ..\\mvnw.cmd"
    __npm = "cmd /C .\\node\\npm.cmd"
    __npx = "cmd /C .\\node\\npx.cmd"
    __ng  = "cmd /C .\\node\\node.exe .\\node_modules\\@angular\\cli\\bin\\ng.js"

    // dot dot commands when running in sub-directory
    __dd_npm = "cmd /C ..\\node\\npm.cmd"
    __dd_ng  = "cmd /C ..\\node\\node.exe ..\\node_modules\\@angular\\cli\\bin\\ng.js"
} 

final mvn = __mvn
final npm = __npm
final npx = __npx
final ng  = __ng

//dot dot commands when running in sub-directory
final dd_npm = __dd_npm
final dd_ng  = __dd_ng

// ==============================================
println("Renaming $request.artifactId project modules...")

new File(PARENT_DIR, "frontend").renameTo(FRONTEND_DIR)
new File(PARENT_DIR, "backend").renameTo(BACKEND_DIR)

// ==============================================
println("Patching up Java Project...")

runProcess("$mvn -f keystore.xml generate-resources", BACKEND_DIR)

// ==============================================
println("Installing node, npm, and ng/cli...")

runProcess("$mvn package", FRONTEND_DIR)

// ==================================================
// ANGULAR ANGULAR ANGULAR
/// ==================================================
if ("angular".equalsIgnoreCase("$spaType")) {

	// ==============================================
	//
	// https://blog.logrocket.com/angular-modules-best-practices-for-structuring-your-app/
	//
	// Angular project layout conventions..
	// 1) Core: src/app/core
	//    Loaded with the application
	//    Components include:
	//      Auth, layout, error
	// 2) Shared: src/app/shared
	//      Commonly used directives, pipes, components
	//      Imported by both core and features
	//      Services should not be here
	// 3) Features: src/app/features
	//      Models and Components only used here
	//      Pages go here.
	//
	
	// ==============================================
	println("Creating Angular Project...")
	
	runProcess("$ng new $request.artifactId --package-manager=npm --ssr=false --style=css --routing=true --skip-install=true --skip-git=true", FRONTEND_DIR)
	
	// ==============================================
	println("Adding app config support...")
	
	runProcess("$npm run ngConfigMods", FRONTEND_DIR, [NODE_DIR])
	
	// ==============================================
	println("Creating Auth Services...")
	
	runProcess("$dd_npm install @azure/msal-browser @azure/msal-angular --save", SPA_PROJECT_DIR, [NODE_DIR])
	runProcess("$dd_ng generate service --skip-tests=true core/auth/auth", SPA_PROJECT_DIR)
	runProcess("$dd_ng generate component --flat --skip-tests=true --selector=app-login-logout-button core/auth/login-logout-button", SPA_PROJECT_DIR)
	runProcess("$npm run ngAuthMods", FRONTEND_DIR, [NODE_DIR])
	
	// ==============================================
	//println("Installing Tailwind CSS Support...")
	
	// https://tailwindcss.com/docs/guides/angular
	//runProcess("$npm install -D tailwindcss postcss autoprefixer", SPA_PROJECT_DIR, [NODE_DIR])
	//runProcess("$npx tailwindcss init", SPA_PROJECT_DIR, [NODE_DIR])
	//runProcess("$npm run ngTailwindCSSMods", FRONTEND_DIR, [NODE_DIR])
	
	// ==============================================
	println("Installing Bootstrap Support...")
	
	runProcess("$dd_npm install bootstrap bootstrap-icons --save", SPA_PROJECT_DIR, [NODE_DIR])
	runProcess("$npm run ngBootstrapMods", FRONTEND_DIR, [NODE_DIR])
	
	// ==============================================
	println("Creating UI Layout...")
	
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-layout-header core/layout/header", SPA_PROJECT_DIR)
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-layout-footer core/layout/footer", SPA_PROJECT_DIR)
	runProcess("$npm run ngLayoutMods", FRONTEND_DIR, [NODE_DIR])
	
	// ==============================================
	println("Creating UI Pages...")
	
	// Pages, home page, etc
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-feature-me features/main/me", SPA_PROJECT_DIR)
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-feature-help features/main/help", SPA_PROJECT_DIR)
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-feature-main-sidemenu features/main/sidemenu", SPA_PROJECT_DIR)
	runProcess("$npm run ngMainFeatureMods", FRONTEND_DIR, [NODE_DIR])
	
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-feature-events features/admin/events", SPA_PROJECT_DIR)
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-feature-support features/admin/support", SPA_PROJECT_DIR)
	runProcess("$dd_ng generate component --skip-tests=true --selector=app-feature-admin-sidemenu features/admin/sidemenu", SPA_PROJECT_DIR)
	runProcess("$npm run ngAdminFeatureMods", FRONTEND_DIR, [NODE_DIR])
	
	// ==============================================
	
	// Routing, Auth Guards
	
	runProcess("$npm run ngRoutingMods", FRONTEND_DIR, [NODE_DIR])
} else {
	println();
	println();
	println();
	println("${spaType} is not a supported SPA type.")
	println();
	println();
	println();
	println("${nonsense_var_to_break_script}")
}

// ==============================================

// Create Wrapper Scripts

final npmwFile = new File("$FRONTEND_DIR/npmw")
npmwFile.createNewFile()
npmwFile.append("""#!/bin/sh

./node/npm  \$*

""")

final ngwFile = new File("$FRONTEND_DIR/ngw")
ngwFile.createNewFile()
ngwFile.append("""#!/bin/sh

./node/node ./node_modules/@angular/cli/bin/ng.js \$*

""")

// ==============================================
println("Deleting bootstrapper project...")

// First delete bootstrapper files...
new File(FRONTEND_DIR, "README.md").delete()
new File(FRONTEND_DIR, "package.json").delete()
new File(FRONTEND_DIR, "package-lock.json").delete()
new File(FRONTEND_DIR, "tsconfig.json").delete()
new File(FRONTEND_DIR, "src").deleteDir()
new File(FRONTEND_DIR, "node_modules").deleteDir()
new File(FRONTEND_DIR, "build").deleteDir()
new File(FRONTEND_DIR, "dist").deleteDir()
new File(FRONTEND_DIR, "target").deleteDir()

// ==============================================
println("Copying SPA project to final location...")

// Move files up a directory
for (File f : SPA_PROJECT_DIR.listFiles()) {
	if (!f.toString().endsWith(request.artifactId)) {
		java.nio.file.Files.move(f.toPath(), FRONTEND_DIR.toPath().resolve(f.toPath().getFileName()))
	}
}

// Now that dir is empty, delete it
SPA_PROJECT_DIR.deleteDir()
