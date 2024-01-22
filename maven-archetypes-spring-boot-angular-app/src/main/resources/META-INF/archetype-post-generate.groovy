//
// Spring Boot Angular App Post Processor
//
println()
println("Beginning Spring Boot Angular App Post Processor")
println()

// ==============================================

def parentDir = new File(request.outputDirectory, request.artifactId)
def frontendDir = new File(parentDir, (request.artifactId + "-frontend"))
def backendDir = new File(parentDir, (request.artifactId + "-backend"))

def angularProjectDir = new File(frontendDir, request.artifactId)

// ==============================================

def runProcess(String cmd, File dir) {
    def process = cmd.execute(null, dir)
    process.waitForProcessOutput((Appendable)System.out, System.err)
    if (process.exitValue() != 0) {
        throw new Exception("Command '$cmd' failed with code: ${process.exitValue()}")
    }
}

def npm = "./node/npm"
def npx = "./node/npx"
def ng = "./node/node ./node_modules/@angular/cli/bin/ng.js"

// ==============================================
println("Renaming $request.artifactId project modules...")

def generatedFrontendDir = new File(parentDir, "frontend")
generatedFrontendDir.renameTo(frontendDir)

def generatedBackendDir = new File(parentDir, "backend")
generatedBackendDir.renameTo(backendDir)

// ==============================================
println("Installing node, npm, and ng/cli...")

runProcess("mvn package", frontendDir)

// ==============================================
println("Creating Angular Project...")

runProcess("$ng new $request.artifactId --package-manager=npm --ssr=false --style=css --routing=true --skip-install=true --skip-git=true", frontendDir)

// ==============================================
//println("Installing Tailwind CSS Support...")

// https://tailwindcss.com/docs/guides/angular
//runProcess("$npm install -D tailwindcss postcss autoprefixer", angularProjectDir)
//runProcess("$npx tailwindcss init", angularProjectDir)
//runProcess("$npm run TailwindCSSMods", frontendDir)

// ==============================================
println("Installing Bootstrap Support...")

runProcess("../$npm install bootstrap bootstrap-icons --save", angularProjectDir)
runProcess("$npm run BootstrapMods", frontendDir)

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
println("Creating UI Layout...")

runProcess("../$ng generate component --skip-tests=true --selector=app-layout-header core/layout/header", angularProjectDir)
runProcess("../$ng generate component --skip-tests=true --selector=app-layout-footer core/layout/footer", angularProjectDir)
runProcess("$npm run LayoutMods", frontendDir)

// Might need properties passed in to indicate which  is active

// ==============================================

// Pages, home page, etc

// ==============================================

// Routing, Auth Guards

// ==============================================
println("Deleting bootstrapper project...")

// First delete bootstrapper files...
new File(frontendDir, "README.md").delete()
new File(frontendDir, "package.json").delete()
new File(frontendDir, "package-lock.json").delete()
new File(frontendDir, "tsconfig.json").delete()
new File(frontendDir, "src").deleteDir()
new File(frontendDir, "node_modules").deleteDir()
new File(frontendDir, "build").deleteDir()
new File(frontendDir, "dist").deleteDir()
new File(frontendDir, "target").deleteDir()

// ==============================================
println("Copying angular project to final location...")

// Move files up a directory
for (File f : angularProjectDir.listFiles()) {
	if (!f.toString().endsWith(request.artifactId)) {
		java.nio.file.Files.move(f.toPath(), frontendDir.toPath().resolve(f.toPath().getFileName()))
	}
}

// Now that dir is empty, delete it
angularProjectDir.deleteDir()
