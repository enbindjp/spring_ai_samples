package jp.enbind.spring_ai.part3.command;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;
import java.util.Map;

@ShellComponent
public class OutputCommand {

    private static final Logger log = LoggerFactory.getLogger(OutputCommand.class);

    private final ApplicationContext context;
    private ChatClient client;

    public OutputCommand(ApplicationContext context){
        this.context = context;
        ChatModel model = context.getBean(ChatModel.class);
        client = ChatClient.builder(model).build();
    }

    // 例 ) 日本で有名なお城の名称をおしえてください
    @ShellMethod(key = "output-list")
    public List<String> listOutput(String message){
        ListOutputConverter converter = new ListOutputConverter();
        String format = converter.getFormat();

        String template = """
                質問に対して回答を5つ作成してください
                {subject}
                
                {format}
                """;

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("subject",message,"format",format))
                .build();

        Prompt prompt = promptTemplate.create();

        String response = client.prompt(prompt).call().content();

        List<String> list = converter.convert(response);
        return list;
    }


    @ShellMethod(key = "output-map")
    public Map<String,Object> mapOutput(String message){
        MapOutputConverter converter = new MapOutputConverter();
        String format = converter.getFormat();

        String template = """
                質問に対してもっとも典型的な答えを1つと、質問のカテゴリー名称とその回答に対するおすすめを理由を50文字以内で作成してください
                {subject}
                
                {format}
                """;

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("subject",message,"format",format))
                .build();

        Prompt prompt = promptTemplate.create();

        String response = client.prompt(prompt).call().content();

        Map<String,Object> map = converter.convert(response);
        return map;
    }

    @JsonPropertyOrder({"name","reason","access"})
    public record Spot(String name,String reason,String access) {}

    @ShellMethod(key = "output-bean")
    public Spot beanOutput(String message){
        BeanOutputConverter<Spot> converter = new BeanOutputConverter<>(Spot.class);
        String format = converter.getFormat();

        String template = """
                質問に対してもっとも典型的な答えを1つと、質問のカテゴリー名称とその回答に対するおすすめを理由を50文字以内で作成してください
                {subject}
                
                {format}
                """;

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("subject",message,"format",format))
                .build();

        Prompt prompt = promptTemplate.create();

        String response = client.prompt(prompt).call().content();

        Spot spot = converter.convert(response);
        return spot;
    }

    @ShellMethod(key = "output-bean-list")
    public List<Spot> beanOutputList(String message){
        BeanOutputConverter<List<Spot>> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<Spot>>() {
                }
        );
        String format = converter.getFormat();

        String template = """
                質問に対してもっとも典型的な答えを3つと、質問のカテゴリー名称とその回答に対するおすすめを理由を50文字以内で作成してください
                {subject}
                
                {format}
                """;

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("subject",message,"format",format))
                .build();

        Prompt prompt = promptTemplate.create();

        String response = client.prompt(prompt).call().content();

        List<Spot> list = converter.convert(response);
        return list;
    }
}
