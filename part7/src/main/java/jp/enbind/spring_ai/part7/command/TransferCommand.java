package jp.enbind.spring_ai.part7.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TransferCommand{

    private static final Logger log = LoggerFactory.getLogger(TransferCommand.class);
    private final ApplicationContext context;
    private final VectorStore vectorStore;

    private ChatClient.Builder builder;

    public TransferCommand(ApplicationContext context) {
        this.context = context;
        this.vectorStore = context.getBean(MariaDBVectorStore.class);

        builder = ChatClient.builder(context.getBean(ChatModel.class));
    }

    /**
     * 質問を状況に合わせて作り直す
     * @param message
     * @return
     */
    @ShellMethod(key = "translate-rewrite")
    public String translateRewrite(String message){

        var template = """
                ユーザからの質問を、マンションの管理規約や使用細則のドキュメントにクエリを実行した際により良い結果が得られるように書き換えてください。
                特にユーザからのクエリはマンション住人の質問と解釈し、相手はマンション管理人もしくはオーナーに対してと解釈してください。
                ただし、質問が抽象的と解釈できる部分は、無理に具体化せず複数の解釈がしやすいように複数のキーワードを含むようにしてください。
                また、無関係な情報については削除し、クエリは簡潔な一文で出力してください。
                元のクエリ:{query}
                書き換えたクエリ:
                """;

        Query query = new Query(message);
        QueryTransformer transformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .promptTemplate(PromptTemplate.builder().template(template).build())
                .build();

        Query transformedQuery = transformer.transform(query);
        return transformedQuery.text();
    }

    /**
     * 質問を翻訳する
     * @param message
     * @return
     */
    @ShellMethod(key = "translate-english")
    public String trEnglish(String message){
        Query query = new Query(message);
        QueryTransformer transformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetLanguage("english")
                .build();

        Query transformedQuery = transformer.transform(query);
        return transformedQuery.text();
    }



    @ShellMethod(key = "translate-compress")
    public String trCompress(String question1, String question2){

        //  ここで一つ目の質問と回答を作る
        ChatClient client = builder.build();
        AssistantMessage answer = client.prompt(question1).call().chatResponse().getResult().getOutput();

        log.info("answer : {}",answer);

        //  2つめの質問
        Query query = Query.builder()
                .text(question2)
                .history(new UserMessage(question1),
                        answer)
                .build();

        QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();

        Query transformedQuery = queryTransformer.transform(query);
        return transformedQuery.text();
    }
}
