package jp.enbind.spring_ai.part6.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class RagQAChatCommand {

    private static final Logger log = LoggerFactory.getLogger(RagQAChatCommand.class);
    private final ApplicationContext context;
    private final VectorStore vectorStore;

    private final ChatClient client;

    public RagQAChatCommand(ApplicationContext context){
        this.context = context;
        vectorStore = context.getBean(MariaDBVectorStore.class);
        client = ChatClient.builder(context.getBean(ChatModel.class)).build();
    }

    @ShellMethod(key = "rag-qa-prompt")
    public String ragQaPrompt(String message){

        //  (1) QuestionAnswerAdvisorのインスタンスを作成
        RetrievalAugmentationAdvisor advisor =
                RetrievalAugmentationAdvisor.builder()
                        //  (2) 参考情報を設定するための方法
                        .documentRetriever(
                                VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .similarityThreshold(0.5)
                                        .topK(10)
                                        .build()
                        ).build();

        String template = """
                あなたはマンションの管理人です。丁寧にやさしい口調で答えてください。
                """;
        SystemPromptTemplate tmpl = new SystemPromptTemplate(template);

        var prompt = Prompt.builder().messages(
                tmpl.createMessage(),
                UserMessage.builder().text(message).build()
        ).build();

        //  (3) advisorsに設定する
        var response = client.prompt(prompt).advisors(advisor).call();

        return response.content();
    }
}
