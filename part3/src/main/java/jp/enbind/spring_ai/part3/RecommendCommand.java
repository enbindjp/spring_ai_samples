package jp.enbind.spring_ai.part3.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Map;

@ShellComponent
public class RecommendCommand {

    private static final Logger log = LoggerFactory.getLogger(RecommendCommand.class);

    private final ApplicationContext context;
    private ChatClient botClient;
    private ChatClient checkClient;

    public RecommendCommand(ApplicationContext context){
        this.context = context;
        ChatModel model = context.getBean(ChatModel.class);

        ChatMemory chatMemory = context.getBean(ChatMemory.class);
        var chatAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        botClient = ChatClient.builder(model)
                .defaultAdvisors(chatAdvisor)
                .defaultSystem("あなたはお客様の旅行を支援するアドバイザーロボットAIです。お客様に失礼のないような答えを作成してください")
                .build();

        checkClient = ChatClient.builder(model)
                .defaultSystem("あなたはお客様と旅行支援をするオペレータの会話について指導する先輩アドバイザーロボットAIです。分かりやすく簡潔にお願いします")
                .build();
    }

    record Question(String category,String description){};

    @ShellMethod(key = "recommend")
    public String recommend(String message){

        BeanOutputConverter<Question> converter = new BeanOutputConverter<>(Question.class);
        String format = converter.getFormat();

        String template = """
                与えられたメッセージを以下の分類項目のいずれかで分類し、内容を要約してください。
                {subject}
                
                {format}
                
                分類項目：あいさつ、質問、要望、その他
                """;

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("subject",message,"format",format))
                .build();

        Prompt prompt = promptTemplate.create();
        String response = checkClient.prompt(prompt).call().content();
        Question q = converter.convert(response);

        log.info("question: {}", q);

        if(q.category.equals("あいさつ")){

            String replyTemplate = """
                お客様から以下の挨拶がありました。以下の状況を参考に最大100文字以内にて、丁寧に挨拶をかえしてください。
                天気：晴れ
                温度：連日高温が続いている
                
                {subject}
                """;
            PromptTemplate reply = PromptTemplate.builder()
                    .template(replyTemplate)
                    .variables(Map.of("subject",message))
                    .build();

            return botClient.prompt(reply.create()).call().content();
        }
        else if(q.category.equals("質問")){
            String replyTemplate = """
                お客様から以下の質問を受けました。質問に文脈に合わせて目的地や旅行日程が明確になるような質問してください。
                {subject}
                """;
            PromptTemplate reply = PromptTemplate.builder()
                    .template(replyTemplate)
                    .variables(Map.of("subject",message))
                    .build();

            return botClient.prompt(reply.create()).call().content();
        }
        else{
            //  ここでカテゴリ別に返事の方向性を決める
        }

        return "すいません、もう一度、表現をかえて入力していただけますか？";

    }
}
