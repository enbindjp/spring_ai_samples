package jp.enbind.spring_ai.part6.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class QAChatCommand {

    private static final Logger log = LoggerFactory.getLogger(QAChatCommand.class);
    private final ApplicationContext context;
    private final VectorStore vectorStore;

    private ChatClient client;

    public QAChatCommand(ApplicationContext context){
        this.context = context;
        vectorStore = context.getBean(MariaDBVectorStore.class);

        client = ChatClient.builder(context.getBean(ChatModel.class))
                .build();
    }

    @ShellMethod(key = "qa-prompt")
    public String qaPrompt(String message,String keyword){

        //  (1) 検索リクエストを作成する
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(10).similarityThreshold(0.5)
                // .query(message)  (2) ここで指定することは意味がない
                .build();

        //  (3) QuestionAnswerAdvisorのインスタンスを作成
        QuestionAnswerAdvisor advisor =
                QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build();

        // (4) システムのプロンプトを設定
        String template = """
                あなたはマンションの管理人です。丁寧にやさしい口調で答えてください。
                """;
        SystemPromptTemplate tmpl = new SystemPromptTemplate(template);

        var prompt = Prompt.builder().messages(
                tmpl.createMessage(),
                // (6) 入力された質問メッセージ
                UserMessage.builder().text(message).build()
        ).build();

        //  (7) advisorsに設定する
        var response = client.prompt(prompt).advisors(advisor).call();

        return response.content();
    }

    /**
     * 最も簡単な使い方
     * @param message
     * @return
     */
    @ShellMethod(key = "qa-prompt-simple")
    public String qaPromptSimple(String message){

        QuestionAnswerAdvisor advisor =
                QuestionAnswerAdvisor.builder(vectorStore)
                        .build();

        var response = client.prompt(message).advisors(advisor).call();
        return response.content();
    }
}