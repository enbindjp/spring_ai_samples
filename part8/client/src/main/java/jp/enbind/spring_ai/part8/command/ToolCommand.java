package jp.enbind.spring_ai.part8.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.enbind.spring_ai.part8.tool.SimpleTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class ToolCommand {

    private static final Logger log = LoggerFactory.getLogger(ToolCommand.class);
    private final ApplicationContext context;

    private ChatClient client;

    public ToolCommand(ApplicationContext context) {
        this.context = context;
        client = ChatClient.builder(context.getBean(ChatModel.class))
                // (1)作成したツールを登録する
                .defaultTools(new SimpleTools())
                .build();
    }

    // (2) Tool Calling用に設定等はいらない
    @ShellMethod(key = "tool-query")
    public String toolQuery(String message){
        return client.prompt(new Prompt(message)).call().content();
    }
}
