package jp.enbind.spring_ai.part3.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Map;

@ShellComponent
public class TemplateCommand {

    private static final Logger log = LoggerFactory.getLogger(OutputCommand.class);

    private final ApplicationContext context;
    private ChatClient client;

    public TemplateCommand(ApplicationContext context){
        this.context = context;
        ChatModel model = context.getBean(ChatModel.class);
        client = ChatClient.builder(model).build();
    }

    @ShellMethod(key = "template-prompt")
    public String tempalte(String message){
        String templateText = """
                あなたは勉強をサポートするAIロボットです。以下の質問に対して丁寧な返答を手順をおって説明して下さい。その際にはそれぞれの手順は200文字以内で説明してください。
                {question}
                """;

        PromptTemplate template = PromptTemplate.builder()
                .template(templateText)
                .variables(Map.of("question",message))
                .build();

        Prompt prompt = template.create();
        return client.prompt(prompt).call().content();
    }
}
