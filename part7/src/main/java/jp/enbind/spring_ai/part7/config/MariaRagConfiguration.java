package jp.enbind.spring_ai.part7.config;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MariaRagConfiguration {

    /**
     * MariaDBVectorStoreオブジェクトの作成
     *
     * @see <a href="https://github.com/spring-projects/spring-ai/blob/main/auto-configurations/vector-stores/spring-ai-autoconfigure-vector-store-mariadb/src/main/java/org/springframework/ai/vectorstore/mariadb/autoconfigure/MariaDbStoreAutoConfiguration.java">SpringBoot側の標準実装</a>
     * @param jdbcTemplate
     * @param embeddingModel
     * @return MariaDBVectorStore
     */
    @Bean
    MariaDBVectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel){
        return MariaDBVectorStore.builder(jdbcTemplate,embeddingModel)
                .distanceType(MariaDBVectorStore.MariaDBDistanceType.COSINE)
                .vectorTableName("vector_store")
                .idFieldName("id")
                .contentFieldName("content")
                .metadataFieldName("metadata")
                .embeddingFieldName("embedding")
                .build();
    }
}
