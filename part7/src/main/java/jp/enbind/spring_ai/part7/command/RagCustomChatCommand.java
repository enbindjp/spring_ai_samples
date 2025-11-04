package jp.enbind.spring_ai.part7.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
public class RagCustomChatCommand {

    private static final Logger log = LoggerFactory.getLogger(RagCustomChatCommand.class);
    private final ApplicationContext context;
    private final VectorStore vectorStore;

    private final ChatClient client;

    private final ChatClient.Builder builder;

    public RagCustomChatCommand(ApplicationContext context){
        this.context = context;
        vectorStore = context.getBean(MariaDBVectorStore.class);
        client = ChatClient.builder(context.getBean(ChatModel.class)).build();
        builder = ChatClient.builder(context.getBean(ChatModel.class));
    }

    @ShellMethod(key = "rag-custom-prompt")
    public String ragCustomPrompt(String message){

        var translateTemplate = """
                ユーザからの質問を、マンションの管理規約や使用細則のドキュメントにクエリを実行した際により良い結果が得られるように書き換えてください。
                特にユーザからのクエリはマンション住人の質問と解釈し、相手はマンション管理人もしくはオーナーに対してと解釈してください。
                ただし、質問が抽象的と解釈できる部分は、無理に具体化せず複数の解釈がしやすいように複数のキーワードを含むようにしてください。
                また、無関係な情報については削除し、クエリは簡潔な一文で出力してください。
                元のクエリ:{query}
                書き換えたクエリ:
                """;

        var multiExpandTemplate = """
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

        //  QuestionAnswerAdvisorのインスタンスを作成
        RetrievalAugmentationAdvisor advisor =
                RetrievalAugmentationAdvisor.builder()

                        // (1) 質問を変換する
                        .queryTransformers(
                                List.of( // (2) 複数指定可能
                                    RewriteQueryTransformer.builder()
                                        .chatClientBuilder(builder)
                                        .promptTemplate(new PromptTemplate(translateTemplate))
                                        .build()
                                )
                        )
                        // (2) 質問のバリエーションを作る
                        .queryExpander(
                                MultiQueryExpander.builder()
                                        .promptTemplate(new PromptTemplate(multiExpandTemplate))
                                        .numberOfQueries(3)
                                        .chatClientBuilder(builder)
                                        .build()
                        )

                        //  (3) 参考情報を設定するための方法(前回と同様)
                        .documentRetriever(
                                VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .similarityThreshold(0.5)
                                        .topK(10)
                                        .build()
                        ).build();

        String sytemTemplate = """
                あなたはマンションの管理人です。丁寧にやさしい口調で答えてください。
                """;
        SystemPromptTemplate tmpl = new SystemPromptTemplate(sytemTemplate);

        var prompt = Prompt.builder().messages(
                tmpl.createMessage(),
                UserMessage.builder().text(message).build()
        ).build();

        var response = client.prompt(prompt).advisors(advisor).call();

        return response.content();
    }
}
