package jp.enbind.spring_ai.part5.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ShellComponent
public class ELTCommand {
    private static final Logger log = LoggerFactory.getLogger(ELTCommand.class);
    private final ApplicationContext context;

    private List<Document> documents;

    public ELTCommand(ApplicationContext context){
        this.context = context;
        documents = new ArrayList<>();
    }

    /**
     * PDFからテキストを抜き出す
     *
     * @param filename
     */
    @ShellMethod(key = "elt-extract-pdf")
    public void extractFromPdf(@ShellOption(value = "--filename") String filename,
                               @ShellOption(value = "--page", defaultValue = "1") int perPage,
                               @ShellOption(value = "--margin-top", defaultValue = "0") int marginTop,
                               @ShellOption(value = "--margin-bottom", defaultValue = "0") int marginBottom
                               ) {
        File file = new File(filename);
        if (!file.exists()) {
            return;
        }

        // (1) リソース型に変換する
        Resource resource = new FileSystemResource(file);
        // (2) インスタンスを作成する
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource,
                // (3) 読み取りのルールを設定
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(marginTop) // マージン(上)を設定
                        .withPageBottomMargin(marginBottom) // マージン(下)を設定
                        .withPagesPerDocument(perPage) // Document毎のページ数
                        .build()
        );

        //  (3) データを取り出す
        List<Document> output = reader.get();

        log.info("documents.size : {}",output.size());
        for (Document doc : output) {
            var metadata = doc.getMetadata();
            log.info("doc.text : metadata({}) - {}", metadata, doc.getText());
        }

        //  変換処理などで処理する為にインスタンス変数として保存
        documents = output;
    }

    /**
     * 現状の状態を把握する為にデータをダンプする
     */
    @ShellMethod(key = "elt-dump-docs")
    public void dumpDocs(){
        if (documents.isEmpty()) {
            return;
        }
        for( int i = 0 ; i < documents.size() ; i++ ){
            var doc = documents.get(i);
            var metadata = doc.getMetadata();

            for(String key : metadata.keySet()){
                Object value = metadata.get(key);
                if(value instanceof List){
                    List list = (List)value;
                    for(Object obj : list){
                        log.info("{} : key : {} , value : {}",i,key,obj);
                    }
                }
                else{
                    log.info("{} - key : {} , value : {}",i,key,value);
                }
            }
            log.info("{} - text:{}", i,doc.getText().trim().substring(0,20) + "....");
        }
    }

    /**
     * 処理するデータ量が多すぎる場合などに、範囲で指定してデータを作り変える
     * @param start
     * @param end
     */
    @ShellMethod(key = "elt-split-docs")
    public void splitDocs(@ShellOption(value = "--start", defaultValue = "0") int start, @ShellOption(value = "--end", defaultValue = "0") int end){
        if (documents.isEmpty()) {
            return;
        }

        if(end < 1 || end > documents.size() ){
            end = documents.size();
        }
        //  こまかい、エラー制御はないので注意！！
        //  指定した範囲のDocumentに作り変える
        List<Document> output = new ArrayList<>(documents.subList(start, end));
        documents = output;
    }

    /**
     *  TokenTextSplitterの利用例
     *
     *  テキストをトークン毎に分割する
     */
    @ShellMethod(key = "elt-transform-token")
    public void transformToken() {
        if (documents.isEmpty()) {
            return;
        }
        //  (1) 分割用のインスタンスを作成
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800) // トークン数の目標サイズ(デフォルト:800)
                .withKeepSeparator(true) // 改行を保持(デフォルト:true)
                .build();

        //  (2) 分割を実行
        List<Document> splitDocs = splitter.apply(documents);

        log.info("splitDocs.size : {}", splitDocs.size());
        for (Document doc : splitDocs) {
            var metadata = doc.getMetadata();
            log.info("doc.text : metadata({}) - {}", metadata, doc.getText());
        }
        documents = splitDocs;
    }


    /**
     *  KeywordMetadataEnricher
     *
     *  Documentにキーワードを付与する
     */
    @ShellMethod(key = "elt-transform-keyword")
    public void transformKeyword(@ShellOption(value = "count", defaultValue = "5") int count){

        if (documents.isEmpty()) {
            return;
        }

        // (1) AIを使うためChatModelを準備する
        ChatModel model = context.getBean(ChatModel.class);

        // (2) キーワードを作成する為のプロンプト
        String template = """
            以下の内容について、キーワードを%s個作成してください。また、出力はカンマ区切りで出力してください。
            
            内容:{context_str}
            キーワード:
            """;

        // (3) テンプレートを作成
        PromptTemplate prompt = PromptTemplate.builder()
                .template(String.format(template, count))
                .build();

        // (4) インスタンスを作成
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher
                .builder(model)
                .keywordsTemplate(prompt)
                // .keywordCount(count)  テンプレートを設定する場合にはいらない
                .build();

        // (5) 変換処理の実行
        List<Document> output = enricher.apply(documents);

        documents = output;
    }

    /**
     *  SummaryMetadataEnricherの利用例
     */
    @ShellMethod(key = "elt-transform-summary")
    public void summaryTransformMetadata(){
        if (documents.isEmpty()) {
            return;
        }

        // それぞれの内容をサマライズする為のプロンプトメッセージ
        // デフォルトのテンプレートが英語なので、要約も英語になってしまう可能性がある
        String template = """
                このセクションの内容:
                {context_str}
                
                このセクションでの主要なトピックスについて要約してください。
                サマリー:
                """;

        ChatModel model = context.getBean(ChatModel.class);
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(
                model,
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT,SummaryMetadataEnricher.SummaryType.NEXT),
                template,
                MetadataMode.ALL
        );


        List<Document> output = enricher.apply(documents);
        log.info("output.size : {}", output.size());

        for(Document doc : output){
            Map<String, Object> metadata = doc.getMetadata();
            log.info("doc.metadata : {}", metadata);
        }

        documents = output;
    }

    /**
     *  ベクターストアに保存する
     */
    @ShellMethod(key = "elt-write-mariadb")
    public void writeMariaDB(){

        // List<Document> documents = ...

        VectorStore vectorStore = context.getBean(MariaDBVectorStore.class);
        vectorStore.add(documents);
    }

    /**
     *  ベクターストア(SimpleVectorStore)に保存する
     */
    @ShellMethod(key = "elt-write-simplestore")
    public void writeSimpleStore(){
        EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(documents);
    }

    /**
     * 出力をファイルに書き出す
     */
    @ShellMethod(key = "elt-write-file")
    public void writeFile(@ShellOption(value = "--filename") String filename){
        FileDocumentWriter writer = new FileDocumentWriter(filename, true, MetadataMode.ALL, false);
        writer.accept(documents);
    }
}
