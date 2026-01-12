package jp.enbind.mcp.service;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  McpResourceの利用例
 */
@Service
public class FileResourceService {

    private static final Logger log = LoggerFactory.getLogger(FileResourceService.class);

    @McpResource(
            uri = "file-content://{fileId}",
            name = "get_file_content",
            description = "ファイルIDからファイル内容を取得する")
    public ReadResourceResult getFileContent(String fileId){

        String fileData = "ファイルの内容の参照を実際にはここで実装";

        return new ReadResourceResult(List.of(
                new McpSchema.TextResourceContents(
                        "file-id://" + fileId,
                        "text/plain",
                        fileData
                )
        ));
    }

    /**
     * text/plainならば、以下の用に簡易的に記述する事も可能
     * @param fileId
     * @return
     */
    @McpResource(
            uri = "file-content2://{fileId}",
            name = "get_file_string_content",
            description = "ファイルIDからファイルの内容を取得します"
    )
    public String getFileContentString(
            @McpArg(name = "fileId", required = true)
            String fileId){
        return "this is file content";
    }
}