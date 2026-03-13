package jp.enbind.spring_ai.part11.command;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TokenCommand {
    private static final Logger log = LoggerFactory.getLogger(TokenCommand.class);

    private ApplicationContext context;

    public TokenCommand(ApplicationContext context) {
        this.context = context;
    }

    @ShellMethod( key = "token-count")
    public int tokenCount(String message){

        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        //  (1) "gpt-4o-mini"の場合
        Encoding enc = registry.getEncoding(ModelType.GPT_4O_MINI.getEncodingType());

        return enc.countTokens(message);
    }
}
