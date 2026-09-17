plugins {
    id("com.android.library") version "9.4.0"
    `maven-publish`
}

group = "com.github.halilozel1903"
version = "1.0.0"

android {
    namespace = "com.halilozel.codeblockview"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    resourcePrefix = "cbv_"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing { singleVariant("release") { withSourcesJar() } }
}

dependencies {
    api("com.google.android.material:material:1.14.0")
    implementation("androidx.core:core-ktx:1.19.0")
    testImplementation("junit:junit:4.13.2")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                artifactId = "CodeBlockView"
                pom {
                    name.set("CodeBlockView")
                    description.set("An Android code card with Kotlin syntax highlighting, line numbers and copy support.")
                    url.set("https://github.com/halilozel1903/CodeBlockView")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/license/mit")
                        }
                    }
                    developers { developer { id.set("halilozel1903"); name.set("Halil Özel") } }
                    scm {
                        url.set("https://github.com/halilozel1903/CodeBlockView")
                        connection.set("scm:git:https://github.com/halilozel1903/CodeBlockView.git")
                    }
                }
            }
        }
    }
}
