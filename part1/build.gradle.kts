plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "jp.enbind.spring_ai"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		//languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "1.0.0"
extra["springShellVersion"] = "3.4.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("org.springframework.shell:spring-shell-starter")

	// testImplementation("org.springframework.boot:spring-boot-starter-test")
	// testImplementation("io.projectreactor:reactor-test")
	// testImplementation("org.springframework.shell:spring-shell-starter-test")
	// testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
