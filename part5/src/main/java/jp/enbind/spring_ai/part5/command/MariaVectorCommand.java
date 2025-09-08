package jp.enbind.spring_ai.part5.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ShellComponent
public class MariaVectorCommand {

    private static final Logger log = LoggerFactory.getLogger(MariaVectorCommand.class);

    private final ApplicationContext context;
    private final VectorStore vectorStore;

    public MariaVectorCommand(ApplicationContext context){
        this.context = context;
        //  MariaDBでのベクトルストアインスタンスを取得
        vectorStore = context.getBean(MariaDBVectorStore.class);
    }

    @ShellMethod(key = "maria-vec-store")
    public String vectorAdd(@ShellOption(value = "--text") String text ,
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

    @ShellMethod(key = "maria-vec-search")
    public void searchMetadata(
            @ShellOption(value = "--query") String query,
            @ShellOption(value = "--category") String category) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        var op1 = builder.eq("category",category);
        var op2 = builder.in("tags",List.of(category));

        Filter.Expression expression = builder.or(op1,op2).build();

        // double distance = 1 - request.getSimilarityThreshold();

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .filterExpression(expression)
                        .topK(5)
                        .similarityThreshold(0.9)
                        .build();


        List<Document> results = vectorStore.similaritySearch(searchRequest);

        results.forEach(doc -> {
            Map<String, Object> metadata = doc.getMetadata();
            var distance = metadata.get("distance");

            log.info("Score : {} , Text : {} : Distance : {}]", doc.getScore(), doc.getText(), distance);
        });
    }

    /**
     * SQLだけで実行する
     * @param query
     */
    @ShellMethod(key = "maria-sql")
    public String nativeSql(String query, String category){

        //JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);

        EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
        float[] embedding = embeddingModel.embed(query);

        Optional<JdbcTemplate> op = vectorStore.getNativeClient();
        if(op.isPresent()){
            JdbcTemplate jdbcTemplate = op.get();

            final String sql = String.format(
                    "SELECT * FROM (select id, content, metadata, VEC_DISTANCE_COSINE(embedding, ?) as distance "
                            + "from vector_store) as t  where ( JSON_VALUE(metadata, '$.category') = ? ) order by distance asc LIMIT 10");

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    sql, embedding, category);

            StringBuffer sb = new StringBuffer();
            for(Map<String, Object> result : results){
                sb.append(String.format("Score : %s , Text : %s \n",result.get("distance"), result.get("content")));
            };
            return sb.toString();
        }
        else{
            return "";
        }
    }
}
