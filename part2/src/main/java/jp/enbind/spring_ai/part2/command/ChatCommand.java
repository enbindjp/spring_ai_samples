package jp.enbind.spring_ai.part2.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class ChatCommand {

    private static final Logger log = LoggerFactory.getLogger(ChatCommand.class);

    private final ApplicationContext context;

    public ChatCommand(ApplicationContext context){
        this.context = context;
    }

    @ShellMethod(key = "chat-client")
    public String chatClientPrompt(String message){
        // (1)
        ChatModel model = context.getBean(ChatModel.class);
        // (2)
        ChatClient client = ChatClient.builder(model).build();
        // (3)
        UserMessage userMessage = new UserMessage(message);
        // (4)
        Prompt prompt = new Prompt(userMessage);
        // (5)
        ChatClient.ChatClientRequestSpec request = client.prompt(prompt);
        // (6)
        ChatClient.CallResponseSpec response = request.call();

        return response.content();
    }

    @ShellMethod(key = "chat-model")
    public String chatModelPrompt(String message){
        ChatModel model = context.getBean(ChatModel.class);
        ChatResponse response = model.call(new Prompt(message));
        return response.getResult().getOutput().getText();
    }
}
