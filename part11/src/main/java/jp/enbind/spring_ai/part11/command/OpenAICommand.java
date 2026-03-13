package jp.enbind.spring_ai.part11.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@ShellComponent
public class OpenAICommand {

    private static final Logger log = LoggerFactory.getLogger(OpenAICommand.class);

    private ApplicationContext context;

    public OpenAICommand(ApplicationContext context) {
        log.info("OpenAICommand : {}",context);
        this.context = context;
    }

    @ShellMethod( key = "openai-prompt")
    public String prompt(String message){
        ChatModel openAiChatModel = this.context.getBean("openAiChatModel", ChatModel.class);
        ChatClient client = ChatClient.create(openAiChatModel);

        Prompt prompt = Prompt.builder()
                .chatOptions(ChatOptions.builder()
                        .maxTokens(10)
                        .model("gpt-4o-mini")
                        .build()
                )
                .content(message)
                .build();

        var response = client.prompt(prompt).call().chatResponse();
        var usage = response.getMetadata().getUsage();

        log.info("input token : {}",usage.getPromptTokens());
        log.info("output token : {}",usage.getCompletionTokens());
        log.info("total token : {}",usage.getTotalTokens());

        String reason = response.getResult().getMetadata().getFinishReason();

        log.info("reason : {}",reason);

        return response.getResult().getOutput().getText();
    }
}
