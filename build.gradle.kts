import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "org.bricolages"
version = "1.2.1"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClassName = "org.bricolages.mys3dump.MyS3Dump"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.1")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.932")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("com.jcraft:jsch:0.1.53")
    implementation("commons-cli:commons-cli:1.3.1")

    // JAXB
    implementation("javax.xml.bind:jaxb-api:+")
    implementation("com.sun.xml.bind:jaxb-core:+")
    implementation("com.sun.xml.bind:jaxb-impl:+")
    implementation("com.sun.activation:javax.activation:+")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.+")
    testImplementation("org.mockito:mockito-core:3.3.+")
    testImplementation("org.mockito:mockito-junit-jupiter:3.3.+")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.16")
    annotationProcessor("org.projectlombok:lombok:1.18.6")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.6")
}

tasks.compileJava {
    options.compilerArgs = listOf(
        "-Xlint:deprecation",
        "-Xlint:unchecked"
    )
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("skipped", "passed", "failed")
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
