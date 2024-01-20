# Angular Bootstrap Project

If you are looking for an Angular project here, you will not find one. This
Maven Archetype project scripts the process of creating an Angular project.

My experience with UI frameworks is that they move fast, and keeping up with
the latest trends and practices is much harder than a Java Spring Boot app.

## What is this project?

This project is a super basic `npm` project. Its purpose is to install the tools
necessary to script the process of creating an Angular project with my customizations.
This includes these main components:

* @angular/cli
* typescript
* ts-morph

We need the Angular CLI to create the initial project, and the TypeScript Morph
project is used to make small modifications to files.

The scripts to make the modifications are in the `src` directory.

The `archetype-post-generate.groovy` script is responsible for invoking the scripts.
The thought process is that the commands in the groovy script are those one would
run from the command line. The typescript files make the modifications to files.

## Instructions used to build this project

The following are the NPM commands to get the tools needed to create the project:

```
npm init
npm install @angular/cli --save-dev
npm install typescript --save-dev
npm install ts-morph --save-dev
npm install ts-node --save-dev
```

To initialize TypeScript for the project, run the following command:

```
npx tsc --init --rootDir src --outDir build \
  --esModuleInterop --resolveJsonModule --lib es6 \
  --module commonjs --allowJs true --noImplicitAny true
```

Edit the `tsconfig.json`, by clearing out the noise, and appending the following property after the compiler options:

```
  ,
  "include": [
    "src/"
  ]
```

Create a `src` directory and add an a simple `index.html` and `index.js` files in it.

To be able to build a distribution such that Spring Boot might want to deploy, add webpack to the project:

```
npm install webpack webpack-cli  file-loader --save-dev
```

To compile code and build the project, add the following command the scripts property in `package.json`

```
    "build": "npx tsc && webpack --mode='development'",
```

To build the project, run the following command:

```
npm run build
```

## TS Morph Notes

The documentation can be found in the following URL:

```
https://ts-morph.com/manipulation/
```

One can create a `typescript` in the `src `folder, for example `HelloWorld.ts`, and then
set up a run commands like so in the `package.json` file:

```
    "HelloWorld": "node build/HelloWorld.js",
```

```
npm run build
npm run HelloWorld
```