import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.provider.gradleKotlinDslOf

group = "com.github.aesteve"
version = "0.0.1-SNAPSHOT"

val vertxVersion = "3.6.2"
val protobufJavaVersion = "3.6.1"
val grpcVersion = "1.16.1"

plugins {
    java
    idea
    id("com.google.protobuf") version("0.8.7")
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
}

dependencies {
    if (JavaVersion.current().isJava9Compatible) {
        // Workaround for @javax.annotation.Generated
        // see: https://github.com/grpc/grpc-java/issues/3633
        compile("javax.annotation:javax.annotation-api:1.3.1")
        compile("javax.xml.bind:jaxb-api:2.2.11")
    }
    compile("io.vertx:vertx-grpc:$vertxVersion")

    testCompile("io.vertx:vertx-junit5:$vertxVersion")
    testCompile("org.testcontainers:testcontainers:1.10.5")
    testRuntime("ch.qos.logback:logback-core:1.2.3")
    testRuntime("ch.qos.logback:logback-classic:1.2.3")
}


sourceSets {
    create("generated") {
        proto {
            srcDir("$projectDir/src/generated/main/java")
        }
    }
}

idea {
    module {
        generatedSourceDirs = setOf(File("build/generated/source"))
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufJavaVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.vertx:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Wrapper> { // wait for release 0.8.8 of protobuf plugin in order to upgrade to Gradle 5.1.1
    gradleVersion = "4.10.3"
}
