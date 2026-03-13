package jp.enbind.spring_ai.part11.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
public class DefaultCommand {

    private static final Logger log = LoggerFactory.getLogger(DefaultCommand.class);

    private ApplicationContext context;

    public DefaultCommand(ApplicationContext context) {
        this.context = context;

    }

    @ShellMethod( key = "default-prompt")
    public String prompt(String message){

        //  デフォルトとのChatModelを使う
        ChatModel model = this.context.getBean(ChatModel.class);

        ChatClient client = ChatClient.create(model);
        String response = client.prompt(message).call().content();
        return response;
    }

    /**
     * 3つのAIプロバイダにすべてリクエストする
     * @param message
     */
    @ShellMethod( key = "all-prompt")
    public void allPrompt(String message){

        // OpenAIの場合
        ChatModel openAiChatModel = this.context.getBean("openAiChatModel", ChatModel.class);

        //  Google Gemini(GenAI)の場合
        ChatModel googleGenAiChatModel = this.context.getBean("googleGenAiChatModel", ChatModel.class);

        // Amazon Bedrockの場合
        ChatModel bedrockProxyChatModel = this.context.getBean("bedrockProxyChatModel", ChatModel.class);

        List<ChatModel> models = List.of(openAiChatModel, googleGenAiChatModel, bedrockProxyChatModel);

        for(ChatModel model : models) {
            ChatClient client = ChatClient.create(model);
            String response = client.prompt(message).call().content();
            log.info("{} - {}",model.getClass(),response);
        }
    }
}
