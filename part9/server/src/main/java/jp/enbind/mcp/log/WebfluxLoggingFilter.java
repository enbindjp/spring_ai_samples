package jp.enbind.mcp.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *  WebFluxを使ったときにのHTTPヘッダ内容をログに出力するようのクラス
 *  (デバッグ用＆動作確認用）
 */
@Component
public class WebfluxLoggingFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(WebfluxLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // リクエストヘッダのログ出力
        exchange.getRequest().getHeaders()
                .forEach((name, values) -> logger.info("Request Header: {} = {}", name, values));

        // チェーンを続行し、レスポンスが返却された後にレスポンスヘッダをログ出力する
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    exchange.getResponse().getHeaders()
                            .forEach((name, values) -> logger.info("Response Header: {} = {}", name, values));
                });
    }
}
