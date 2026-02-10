plugins {
	java
    id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "jp.enbind.spring-ai"
version = "0.0.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "1.1.0"
extra["springShellVersion"] = "3.4.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	implementation("org.springframework.shell:spring-shell-starter")

    //  MCPクライアントでは必要
    implementation("org.springframework.ai:spring-ai-starter-mcp-client")

}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
		mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
