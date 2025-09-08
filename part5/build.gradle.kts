plugins {
	java
	id("org.springframework.boot") version "3.4.5"
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

extra["springAiVersion"] = "1.0.1"
extra["springShellVersion"] = "3.4.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	//	OpenAIを使う場合
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("org.springframework.shell:spring-shell-starter")

	//implementation("org.springframework.ai:spring-ai-vector-store")

	//	3.5.1以上でないとエラーになる
	implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
	implementation("org.springframework.ai:spring-ai-starter-vector-store-mariadb")

	implementation("org.springframework.ai:spring-ai-pdf-document-reader")
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
