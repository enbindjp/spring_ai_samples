package jp.enbind.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainApplication {
    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider simpleTools(DateService dateService) {
        log.info("simpleTools invoked");
        return MethodToolCallbackProvider.builder()
                .toolObjects(dateService).build();
    }


}
