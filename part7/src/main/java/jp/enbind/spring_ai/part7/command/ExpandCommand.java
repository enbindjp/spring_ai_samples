package jp.enbind.spring_ai.part7.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
public class ExpandCommand {

    private static final Logger log = LoggerFactory.getLogger(ExpandCommand.class);
    private final ApplicationContext context;
    private final VectorStore vectorStore;

    private ChatClient.Builder builder;

    public ExpandCommand(ApplicationContext context) {
        this.context = context;
        this.vectorStore = context.getBean(MariaDBVectorStore.class);

        builder = ChatClient.builder(context.getBean(ChatModel.class));
    }

    /**
     * 抽象的な質問や解釈がいくつもあり得るような質問を複数にする
     *
     * （プロンプトテンプレート（日本語訳))
     *
     * 情報検索と検索エンジンの最適化の専門家として、与えられたクエリの異なるバージョンを{number}個作成してください。
     * 各バージョンは、元のクエリの中心的な意図を保ちつつ、トピックの異なる視点や側面を網羅する必要があります。目的は、検索スペースを拡大し、関連する情報を見つける可能性を高めることです。
     * 選択の理由やその他のテキストは追加しないでください。
     * クエリのバージョンを改行で区切って提示してください。
     *
     *
     * @param message
     * @return
     */
    @ShellMethod(key = "expand-multi-normal")
    public String expandMultiDefault(String message){
        Query query = new Query(message);
        QueryExpander expander = MultiQueryExpander.builder()
                .chatClientBuilder(builder)
                .numberOfQueries(3)
                .build();

        List<Query> queryList = expander.expand(query);

        StringBuilder sb = new StringBuilder();
        queryList.forEach(q -> {
            log.info("query: {}", q.context());
            sb.append(q.text());
            sb.append("\n");
        });
        return sb.toString();
    }

    @ShellMethod(key = "expand-multi")
    public String expandMultiTemplate(String message){

        String templateText = """
                質問者はマンションの住人か、所有者です。その前提で、マンションの理事会、もしくは管理人に質問しています。
                その状況を想定して、情報検索と検索エンジンの最適化の専門家として、与えられた質問の異なるバージョンを {number} 個作成してください。
                多くの場合には、そのものに対する質問だけではなく、利用制限、料金、使い方、または、時間や日程に関する質問などのように解釈を広げるようにしてください。
                各バージョンは、元の質問の中心的な意図を保ちつつ、トピックの異なる視点や側面を網羅する必要があります。
                目的は、検索時における解釈を拡大し、関連する情報を見つける可能性を高めることです。
                また、質問のバリエーションが見つからない場合、マンション生活で考えられる事や管理人の仕事に対する質問という前提で範囲を広げてください。
                
                Do not explain your choices or add any other text.
                Provide the query variants separated by newlines.
                
                質問: {query}
                
                Query variants:
                """;

        Query query = new Query(message);
        QueryExpander expander = MultiQueryExpander.builder()
                .promptTemplate(PromptTemplate.builder().template(templateText).build())
                .chatClientBuilder(builder)
                .numberOfQueries(5)
                .build();

        List<Query> queryList = expander.expand(query);

        StringBuilder sb = new StringBuilder();
        queryList.forEach(q -> {
            log.info("query: {}", q.context());
            sb.append(q.text());
            sb.append("\n");
        });
        return sb.toString();
    }
}
