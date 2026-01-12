package jp.enbind.mcp.service;

import io.modelcontextprotocol.spec.McpSchema.*;
import jp.enbind.mcp.config.FileServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.PathMatcher;
import java.nio.file.FileSystems;

import org.springaicommunity.mcp.context.McpAsyncRequestContext;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.List;

/**
 * McpToolの同期型実装例
 */
@Component
public class FileToolService {

    private static final Logger log = LoggerFactory.getLogger(FileToolService.class);

    private final FileServiceConfiguration config;

    public FileToolService(FileServiceConfiguration config){
        this.config = config;
    }

    /**
     * このコードは、spring.ai.mcp.server.type=SYNC の場合に有効
     *
     */
    @McpTool(name = "get_root_directory_sync",
            description = "ワークスペース上やプロジェクトに関するファイルの内容に関して質問された場合に、そのファイルが配置されたルートとなるディレクトリのパスを返す")
    public String getDirectory(){
        return config.getDir().getAbsolutePath();
    }

    @McpTool(
            name = "find_file_by_name_pattern_sync",
            title = "ファイルの検索",
            description = "ワークスペースや業務に関するファイル名に関して検索があった場合に、その条件に一致するファイルURI(file-content://で始まる）のリストを提供します。"
    )
    public List<String> findFileByNamePatternSync(
            @McpToolParam(description = "ファイル名に含まれれる文字列やパターン。**/*.txtなどのようにして検索する事も可能")
            String pattern){

        var dir = config.getDir();

        try {
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
            return fileIdList;
        } catch (IOException e) {
            log.error("Error searching files: ", e);
            return List.of();
        }
    }

    /**
     * このコードは、spring.ai.mcp.server.type=SYNC の場合には有効
     * @param context
     * @param pattern
     * @return
     */
    @McpTool(
            name = "get_filepath_by_name_pattern_progress",
            description = "ワークスペースや業務に関するファイル名に関して検索があった場合に、その条件に一致するファイルURI(file-content://で始まる）のリストを提供します。")
    public List<String> getFilenameMatchListProgress(
            McpSyncRequestContext context,
            @McpToolParam(description = "検索したいファイル名のパターン", required = true) String pattern){
        log.info("getFilenameMatch invoked : pattern({})",pattern);

        context.progress(p -> p.progress(0).message("検索を開始しました"));

        try {
            Thread.sleep(1000);

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
            context.progress(p -> p.progress(100).message("検索が完了しました"));
            return fileIdList;
        }
        catch (Exception e) {

        }
        return List.of();
    }

    /**
     * McpResouceとして実装せずに、McpToolとして実装した場合
     * @param fileUri
     * @return
     */
    @McpTool(
            name = "get_file_content_string_by_file_path",
            title = "ファイル内容の取得",
            description = "file-content://から始まるファイルパスを元に、ファイルのデータ内容を文字列で返します"
    )
    public String getFileContentString(String fileUri){
        if(fileUri.startsWith("file-content://")){
            String filename = fileUri.substring("file-content://".length());
            log.info("filename:{}",filename);

            File dir = this.config.getDir();
            Path filePath = dir.toPath().resolve(filename);

            if (!filePath.startsWith(dir.toPath())) {
                return "";
            }

            try {
                return Files.readString(filePath);
            } catch (IOException e) {
                return "";
            }

        } else{
            return "";
        }
    }
}
