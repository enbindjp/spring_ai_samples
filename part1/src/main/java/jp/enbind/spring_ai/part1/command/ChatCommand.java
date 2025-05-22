package jp.enbind.spring_ai.part1.command;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class ChatCommand {
    private ApplicationContext context;

    public ChatCommand(ApplicationContext context) {
        this.context = context;
    }

    @ShellMethod( key = "chat-prompt")
    public String prompt(String message){
        ChatModel model = this.context.getBean(ChatModel.class);
        ChatClient client = ChatClient.create(model);
        String response = client.prompt(message).call().content();
        return response;
    }
}
