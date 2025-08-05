package jp.enbind.spring_ai.part4.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ShellComponent
public class VectorCommand {
    private static final Logger log = LoggerFactory.getLogger(VectorCommand.class);

    private final ApplicationContext context;
    private final SimpleVectorStore vectorStore;

    public VectorCommand(ApplicationContext context){
        this.context = context;
        EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
        vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * ベクターを作成する
     * @param message
     */
    @ShellMethod(key = "vector-create")
    public String create(String message){
        EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
        float[] vector = embeddingModel.embed(message);
        return Arrays.toString(vector);
    }

    @ShellMethod(key = "vector-store-simple")
    public String store(String message){
        Document doc = new Document(message);
        vectorStore.add(List.of(doc));
        return "登録しました";
    }
    /**
     *
     * @param text 登録するテキスト
     * @param category カテゴリ
     * @param tags その他タグ
     * @return
     */
    @ShellMethod(key = "vector-store")
    public String storeText(@ShellOption(value = "--text") String text ,
                                @ShellOption(value = "--category" , defaultValue = "general") String category,
                                @ShellOption(value = "--tags", defaultValue = "") String tags){

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category",category);

        List<String> tagList = List.of(tags.split(","));
        metadata.put("tags",tagList);

        Document doc = Document.builder()
                .metadata(metadata)
                .text(text)
                .build();
        vectorStore.add(List.of(doc));
        return "登録しました";
    }

    @ShellMethod(key = "vector-search-simple")
    public String search(String query){
        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query) // 検索ワード
                        .topK(5) // 最大で取得する件数(SQLのlimit相当)
                        .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        StringBuffer sb = new StringBuffer();
        results.forEach(doc -> {
            sb.append(String.format("Score : %s , Text : %s , Category : %s\n", doc.getScore(), doc.getText(),doc.getMetadata().get("category")));
        });
        return sb.toString();
    }

    @ShellMethod(key = "vector-search")
    public String searchCategory(
            @ShellOption(value = "--query") String query,
            @ShellOption(value = "--category") String category,
            @ShellOption(value = "--threshold", defaultValue = "0.0") String threshold)
    {

        double doubleThreshold = Double.parseDouble(threshold);

        Filter.Expression expression =
                new Filter.Expression(
                        Filter.ExpressionType.EQ,
                        new Filter.Key("category"),
                        new Filter.Value(category)
                );

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .filterExpression(expression)
                        .similarityThreshold(doubleThreshold)
                        .topK(5)
                        .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        StringBuffer sb = new StringBuffer();
        results.forEach(doc -> {
            sb.append(String.format("Score : %s , Text : %s , Category : %s\n", doc.getScore(), doc.getText(),doc.getMetadata().get("category")));
        });
        return sb.toString();
    }

    @ShellMethod(key = "vector-search-builder")
    public String searchBuilderMetadata(
            @ShellOption(value = "--query") String query,
            @ShellOption(value = "--category") String category,
            @ShellOption(value = "--threshold", defaultValue = "0.0") String threshold)
    {

        var doubleThreshold = Double.parseDouble(threshold);

        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        var op1 = builder.eq("category",category);
        var op2 = builder.in("tags",List.of(category));

        Filter.Expression expression = builder.or(op1,op2).build();

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .filterExpression(expression)
                        .similarityThreshold(doubleThreshold)
                        .topK(5)
                        .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        StringBuffer sb = new StringBuffer();
        results.forEach(doc -> {
            sb.append(String.format("Score : %s , Text : %s , Category : %s\n", doc.getScore(), doc.getText(),doc.getMetadata().get("category")));
        });
        return sb.toString();
    }

    @ShellMethod(key = "vector-search")
    public String searchMetadataString(
            @ShellOption(value = "--query") String query,
            @ShellOption(value = "--category") String category,
            @ShellOption(value = "--threshold", defaultValue = "0.0") String threshold)

    {
        double doubleThreshold = Double.parseDouble(threshold);
        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .filterExpression("category == '" + category + "'")
                        .similarityThreshold(doubleThreshold)
                        .topK(5)
                        .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        StringBuffer sb = new StringBuffer();
        results.forEach(doc -> {
            sb.append(String.format("Score : %s , Text : %s , Category : %s\n", doc.getScore(), doc.getText(),doc.getMetadata().get("category")));
        });
        return sb.toString();
    }

    @ShellMethod(key = "vector-save")
    public void save(String filename){
        vectorStore.save(new File(filename));
    }

    @ShellMethod(key = "vector-load")
    public void load(String filename){
        vectorStore.load(new File(filename));
    }
}
