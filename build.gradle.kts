import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    id("application")
    id("java")
    id("com.github.spotbugs") version("6.0.0")
    kotlin("jvm") version "1.9.22"
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
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.8.3")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.9")


    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

spotbugs {
    toolVersion.set("4.9.3")
    reportsDir.set(file("${project.rootDir}/reports/spotbugs"))
    reportLevel.set(Confidence.LOW)
    effort.set(Effort.MAX)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("spotbugsMain", SpotBugsTask::class) {
    reports {
        create("html") {
            required.set(true)
            outputLocation.set(file("${project.rootDir}/reports/spotbugs/reports.html"))
        }
    }
}

tasks.named("run") {
    doFirst {
        if (!project.hasProperty("testProps")) {
            (this as JavaExec).args("--test-props=")
        }
    }
}