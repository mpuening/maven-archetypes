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

def spawnProcess(String cmd, File dir) {
    def process = cmd.execute(null, dir)
    process.waitForProcessOutput((Appendable)System.out, System.err)
    if (process.exitValue() != 0) {
        throw new Exception("Command '$cmd' failed with code: ${process.exitValue()}")
    }
}

// ==============================================
println("Renaming $request.artifactId project modules...")

def generatedFrontendDir = new File(parentDir, "frontend")
generatedFrontendDir.renameTo(frontendDir)

def generatedBackendDir = new File(parentDir, "backend")
generatedBackendDir.renameTo(backendDir)

// ==============================================
println("Installing node, npm, and ng/cli...")

spawnProcess("mvn package", frontendDir)

// ==============================================
println("Creating Angular Project...")

def ng = "./node/node ./node_modules/@angular/cli/bin/ng.js"

spawnProcess("$ng new $request.artifactId --package-manager=npm --ssr=false --style=css --routing=true --skip-install=true --skip-git=true", frontendDir)

// ==============================================
//println("Installing Tailwind CSS Support...")

// https://tailwindcss.com/docs/guides/angular
//spawnProcess("../node/npm install -D tailwindcss postcss autoprefixer", angularProjectDir)
//spawnProcess("../node/npx tailwindcss init", angularProjectDir)
//spawnProcess("./node/npm run TailwindCSSMods", frontendDir)

// ==============================================
println("Installing Bootstrap Support...")

// https://tailwindcss.com/docs/guides/angular
spawnProcess("../node/npm install bootstrap bootstrap-icons --save", angularProjectDir)
spawnProcess("./node/npm run BootstrapMods", frontendDir)

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
