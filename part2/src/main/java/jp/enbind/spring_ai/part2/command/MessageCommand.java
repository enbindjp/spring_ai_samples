package jp.enbind.spring_ai.part2.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

@ShellComponent
public class MessageCommand {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ApplicationContext context;

    public MessageCommand(ApplicationContext context){
        this.context = context;
    }

    @ShellMethod(key = "message-create")
    public String createMessage(String userText){

        SystemMessage systemMessage = new SystemMessage("あなたは専門家のためのAIアシスタントです。曖昧な回答や不確実な場合には「わかりません」と答えてください。");
        UserMessage userMessage = new UserMessage(userText);

        Prompt prompt = Prompt.builder()
                .messages(systemMessage,userMessage).build();

        ChatModel model = context.getBean(ChatModel.class);
        ChatClient client = ChatClient.builder(model).build();
        ChatClient.CallResponseSpec response = client.prompt(prompt).call();

        var result = response.chatResponse().getResult();
        if(result != null){
            AssistantMessage message = result.getOutput();
            return message.getText();
        }
        else{
            return "回答エラー";
        }
    }

    @ShellMethod(key = "message-image")
    public String imageMessage(String filename,String userText){
        File file = new File(filename);

        ChatModel model = context.getBean(ChatModel.class);
        ChatClient client = ChatClient.builder(model).build();

        FileSystemResource resource = new FileSystemResource(file);
        Media media = new Media(MimeTypeUtils.IMAGE_PNG, resource);

        UserMessage userMessage = UserMessage.builder().text(userText).media(media).build();

        Prompt prompt = Prompt.builder()
                .messages(userMessage)
                .build();

        return client.prompt(prompt).call().content();
    }
}
