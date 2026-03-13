package jp.enbind.spring_ai.part11.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfigure {

    /**
     * このコードは実際はBean登録はできない
     *
     * @param builder
     * @return
     */
    public ChatClient createChatClient(ChatClient.Builder builder){
        return builder.clone()
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gpt-4o-mini")
                                .build()
                ).build();
    }

    /**
     * OpenAIのChatModelを使ってChatClientをBeanとして登録場合の例
     * @param chatModel
     * @return
     */
    @Bean(name = "customOpenAIChatClient")
    public ChatClient createOpenAIChatModel(@Qualifier("openAiChatModel") ChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("gpt-4o-mini")
                                .build()
                ).build();
    }
}
