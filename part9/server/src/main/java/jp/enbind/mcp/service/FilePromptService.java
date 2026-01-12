package jp.enbind.mcp.service;

import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *  McpPromptの利用例
 */
@Component
public class FilePromptService {

    @McpPrompt(
            name = "file-mcp-prompt",
            description = "このサービスを使う上でのプロンプト")
    public McpSchema.GetPromptResult documentSummaryPrompt(
            @McpArg(name = "question", required = true) String question ){

        String promptText = String.format(
                "%s \n%s\n質問:%s",
                "あなたは、社内ドキュメントを管理し、その情報を扱う為のAIエージェントです。",
                "ファイルの内容の扱いについて十分気を遣い、質問を取り扱ってください。また、・・・・",
                question
        );

        return new McpSchema.GetPromptResult("サービスを扱うためのプロンプト",
                List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(promptText))));
    }
}
