package jp.enbind.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 *  MCP Tool側からファイル検索したときのルートファイルパスを管理する設定用クラス
 */
@Configuration
@ConfigurationProperties(prefix = "mcp.file")
public class FileServiceConfiguration {

    private File dir;

    /**
     * 検索するためのディレクトリ設定
     * @param directory
     */
    public void setDirectory(String directory) {
        var file = new File(directory);
        if(file.exists() && file.isDirectory()){
            this.dir = file;
        }
    }

    /**
     * ディレクトリを取得する
     * @return
     */
    public File getDir() {
        return dir;
    }
}
