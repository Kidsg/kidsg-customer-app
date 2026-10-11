import fs from 'fs';

// 1. Patch android/build.gradle
const rootBuildGradle = 'C:/Users/admin/Projects/kidsg-partner-app/android/build.gradle';
let rootContent = fs.readFileSync(rootBuildGradle, 'utf8');

const subprojectsBlock = `
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    afterEvaluate { project ->
        if (project.hasProperty('android')) {
            project.android {
                compileOptions {
                    sourceCompatibility JavaVersion.VERSION_17
                    targetCompatibility JavaVersion.VERSION_17
                }
            }
        }
    }
    tasks.withType(JavaCompile).configureEach {
        options.compilerArgs << "-Xlint:-options"
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
`;

if (!rootContent.includes('subprojects {')) {
  rootContent = rootContent.replace(/allprojects\s*\{\s*repositories\s*\{\s*google\(\)\s*mavenCentral\(\)\s*\}\s*\}/, subprojectsBlock.trim());
  fs.writeFileSync(rootBuildGradle, rootContent, 'utf8');
  console.log('Patched android/build.gradle');
}

// 2. Patch android/app/build.gradle to explicitly declare Java 17
const appBuildGradle = 'C:/Users/admin/Projects/kidsg-partner-app/android/app/build.gradle';
let appContent = fs.readFileSync(appBuildGradle, 'utf8');

if (!appContent.includes('compileOptions')) {
  appContent = appContent.replace(
    'buildTypes {',
    `compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    buildTypes {`
  );
  fs.writeFileSync(appBuildGradle, appContent, 'utf8');
  console.log('Patched android/app/build.gradle');
}

// 3. Patch node_modules/@capacitor/android/capacitor/build.gradle
const capBuildGradle = 'C:/Users/admin/Projects/kidsg-partner-app/node_modules/@capacitor/android/capacitor/build.gradle';
if (fs.existsSync(capBuildGradle)) {
  let capContent = fs.readFileSync(capBuildGradle, 'utf8');
  capContent = capContent.replace(/JavaVersion\.VERSION_21/g, 'JavaVersion.VERSION_17');
  fs.writeFileSync(capBuildGradle, capContent, 'utf8');
  console.log('Patched capacitor/build.gradle to Java 17');
}
