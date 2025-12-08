plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "jp.enbind"
version = "0.0.1"
description = "Spring AI MCP Server demo application"

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.1.0"

dependencies {
    implementation("org.springframework.ai:spring-ai-starter-mcp-server")
    // 処理自体は使わないが、依存解決のために必要
    implementation("org.springframework.boot:spring-boot-starter-web")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}
