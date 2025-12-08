package jp.enbind.spring_ai.part8.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class McpCommand {

    private static final Logger log = LoggerFactory.getLogger(McpCommand.class);
    private final ApplicationContext context;

    private ChatClient client;

    public McpCommand(ApplicationContext context, ToolCallbackProvider tools) {
        this.context = context;

        client = ChatClient
                .builder(context.getBean(ChatModel.class))
                .defaultToolCallbacks(tools)
                .build();
    }

    @ShellMethod(key = "mcp-query")
    public String mcpQuery(String message){
        return client.prompt(new Prompt(message)).call().content();
    }
}
