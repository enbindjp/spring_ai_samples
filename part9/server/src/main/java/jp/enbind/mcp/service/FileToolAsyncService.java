package jp.enbind.mcp.service;

import io.modelcontextprotocol.spec.McpSchema;
import jp.enbind.mcp.config.FileServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpAsyncRequestContext;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * 非同期型として動作させる場合の実装例
 */
@Component
public class FileToolAsyncService {

    private static final Logger log = LoggerFactory.getLogger(FileToolAsyncService.class);

    private final FileServiceConfiguration config;

    public FileToolAsyncService(FileServiceConfiguration config){
        this.config = config;
    }

    /**
     * このコードは、spring.ai.mcp.server.type=ASYNC の場合には有効
     * @return
     */
    @McpTool(name = "get_root_directory_async",
            description = "仕事やプロジェクトに関するファイルの内容に関して質問された場合に、そのファイルの元となるルートとなるディレクトリのパスを返す")
    public Mono<String> getDirectoryAsync(){
        return Mono.just(config.getDir().getAbsolutePath());
    }

    /**
     * このコードは、spring.ai.mcp.server.type=ASYNC の場合には有効
     * @param pattern
     * @return
     */
    @McpTool(
            name = "get_filepath_by_name_pattern_async",
            description = "ワークスペースや業務に関するファイル名に関して検索があった場合に、その条件に一致するファイルURI(file-content://で始まる）のリストを提供します。")
    public Mono<List<String>> getFilenameMatchListAsync(
            @McpToolParam(description = "検索したいファイル名のパターン", required = true) String pattern){
        log.info("getFilenameMatch invoked : pattern({})",pattern);

        try {
            Thread.sleep(5000);

            var dir = config.getDir();
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            List<String> fileList = Files.walk(dir.toPath())
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(dir.toPath().relativize(path)))
                    .map(Path::toString)
                    .toList();

            List<String> fileIdList = new ArrayList<>();
            for(String file : fileList){
                String filename = dir.toPath().relativize(Path.of(file)).toString();
                fileIdList.add("file-content://" + filename);
            }
            return Mono.just(fileIdList);
        }
        catch (Exception e) {

        }
        return Mono.just(List.of());
    }
}
