package jp.enbind.spring_ai.part10.command;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.HashMap;
import java.util.Map;

@ShellComponent
public class McpClientCommand {

    private static final Logger log = LoggerFactory.getLogger(McpClientCommand.class);
    private final ApplicationContext context;

    //  ここは書き換えてください
    private static String MCP_SERVER_URL = "http://192.168.100.40:8080/mcp";

    public McpClientCommand(ApplicationContext context){
        this.context = context;
    }

    /**
     * MCPでのツール一覧情報を取得する
     * @return
     */
    @ShellMethod(key = "mcp-tools")
    public Map<String,String> mcpTools(){

        // (1) StreamableHTTPとして接続する
        var transport = HttpClientStreamableHttpTransport
                .builder(MCP_SERVER_URL).build();

        var toolMap = new HashMap<String,String>();
        // (2) MCP Clientを動機型として作成
        try(McpSyncClient mcpClient = McpClient.sync(transport).build()) {

            //  (3)初期化
            mcpClient.initialize();

            //  (4)認識しているツール一覧を取得
            var tools = mcpClient.listTools().tools();

            for (var tool : tools) {
                // (5) ツールの名前と説明を取得する
                toolMap.put(tool.name(),tool.description());
            }
        }
        return toolMap;
    }

    /**
     *
     * @param pattern
     * @return
     */
    @ShellMethod(key = "mcp-tool-find-files")
    public String mapToolFindFiles(String pattern){

        var transport = HttpClientStreamableHttpTransport
                .builder(MCP_SERVER_URL).build();

        try(McpSyncClient mcpClient = McpClient.sync(transport).build()) {

            //  初期化までは同じ
            mcpClient.initialize();

            //  (1) ツールを実行するリクエストを作成する
            var request = new McpSchema.CallToolRequest(
                    //  (2) 実行したいMCP上のツール名
                    "find_file_by_name_pattern_sync",
                    //  (3) ツールへの引数を設定する
                    Map.of("pattern", pattern)
            );

            //  (4) ツールの実行
            var response = mcpClient.callTool(request);

            //  (5) ツールの結果を取得（このメソッドは戻りデータが必ず1つとMCPサーバ側で定義されている）
            var content = response.content().get(0);

            // (6) ツールの結果
            if(content instanceof McpSchema.TextContent textContent){
                String jsonText = textContent.text();
                //  JSONテキストの結果を解析して、自分達が利用しやすいようにする
                return jsonText;
            }
        }
        return "Error";
    }

}