plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "jp.enbind"
version = "0.0.1-SNAPSHOT"
description = "Spring AI MCP Server demo application"

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.1.2"

dependencies {
    implementation("org.springframework.ai:spring-ai-starter-mcp-server")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
    //implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}
