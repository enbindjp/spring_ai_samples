package jp.enbind.spring_ai.part2.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.MimeTypeUtils;

import java.io.File;

@ShellComponent
public class ChatClientCommand {

    private static final Logger log = LoggerFactory.getLogger(ChatModelCommand.class);

    private final ApplicationContext context;
    private ChatClient client;

    public ChatClientCommand(ApplicationContext context){
        this.context = context;
    }

    /**
     *  履歴を維持したChatClientの作成
     */
    @ShellMethod(key = "client-start")
    public String startClient(String systemMessage){
        ChatModel model = context.getBean(ChatModel.class);
        ChatMemory chatMemory = context.getBean(ChatMemory.class);
        MessageChatMemoryAdvisor chatAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.client= ChatClient.builder(model)
                .defaultAdvisors(chatAdvisor)
                .build();

        var response = client.prompt(new Prompt(new SystemMessage(systemMessage))).call();
        return response.content();
    }

    @ShellMethod(key = "client-user")
    public String textPrompt(String message){
        var response = client.prompt(new Prompt(new UserMessage(message))).call();
        return response.content();
    }

    @ShellMethod(key = "client-image")
    public String imagePrompt(String filename,String userText){
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

    // --- 以下、その他参考コード ----


    /**
     * システムメッセージを追加で指定する方法
     */
    protected ChatClient systemMessageClient(){
        ChatModel model = context.getBean(ChatModel.class);
        ChatMemory chatMemory = context.getBean(ChatMemory.class);
        MessageChatMemoryAdvisor chatAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(model)
                .defaultAdvisors(chatAdvisor)
                .defaultSystem("明確な回答が導けない場合には「わかりません」とだけ答えてください")
                .build();
    }

    /**
     * AIチャットにおいて利用するモデルなどを変えたり、Temperatureなどの値を変えて実行したい場合
     */
    protected ChatClient optionsMessageClient(){
        ChatModel model = context.getBean(ChatModel.class);
        ChatMemory chatMemory = context.getBean(ChatMemory.class);
        var chatAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        var options = ChatOptions.builder()
                .model("gpt-3.5-turbo")
                .temperature(2.0)
                .maxTokens(100)
                .build();

        return ChatClient.builder(model)
                .defaultAdvisors(chatAdvisor)
                .defaultSystem("回答は50文字以内で簡潔にお願いします")
                .defaultOptions(options)
                .build();
    }
}
