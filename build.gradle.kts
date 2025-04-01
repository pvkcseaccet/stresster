plugins {
    id("application")
    id("java")
}

group = "com.stresster"
version = "1.0-BASERELEASE"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.stresster.StressTester")
}


dependencies {
    implementation("org.apache.commons:commons-lang3:3.0")
    implementation("commons-io:commons-io:2.18.0")
    compileOnly("org.projectlombok:lombok:1.18.36")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("org.json:json:20250107")
    implementation("org.thymeleaf:thymeleaf:3.1.1.RELEASE")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("run") {
    doFirst {
        if (!project.hasProperty("testProps")) {
            (this as JavaExec).args("--test-props=")
        }
    }
}