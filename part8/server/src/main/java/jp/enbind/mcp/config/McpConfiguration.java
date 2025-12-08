package jp.enbind.mcp.config;

import jp.enbind.mcp.service.SimpleService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfiguration {

    @Bean
    public ToolCallbackProvider simpleTools() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(new SimpleService())
                .build();
    }
}
