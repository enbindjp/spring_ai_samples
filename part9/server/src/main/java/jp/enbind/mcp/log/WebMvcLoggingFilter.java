package jp.enbind.mcp.log;

import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 *  WebMVCを使ったときにのHTTPヘッダ内容をログに出力するようのクラス
 *  (デバッグ用＆動作確認用）
 */
@Component
public class WebMvcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WebMvcLoggingFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try{
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        }
        finally{
            // --- BEFORE (リクエスト処理前) のログ ---
            logRequest(wrappedRequest);
            logResponse(wrappedRequest, wrappedResponse);

            // 重要: ContentCachingResponseWrapperを使った場合、
            // 最後にこのメソッドを呼ばないとレスポンスボディがクライアントに送信されません。
            wrappedResponse.copyBodyToResponse();
        }
    }

    // 処理後のリクエストおよびレスポンス情報をログに出力するメソッド
    private void logRequest(ContentCachingRequestWrapper request) {
        String uri = request.getRequestURI();
        var encoding = request.getCharacterEncoding();
        byte[] data = request.getContentAsByteArray();
        String requestPayload = getPayload(data,encoding);

        // リクエストヘッダのログ出力
        request.getHeaderNames().asIterator()
                .forEachRemaining(name -> log.info("Request Header: {} = {}",
                        name, request.getHeader(name)));

        log.info("-> REQUEST: Method={}, URI={}, Length={}, Payload={}",
                request.getMethod(), uri, data.length,requestPayload);
    }

    // レスポンスのログ出力
    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        var encoding = response.getCharacterEncoding();
        log.info("Response Encoding: {}", encoding);
        String responsePayload = getPayload(response.getContentAsByteArray(), null);

        // レスポンスヘッダのログ出力
        response.getHeaderNames()
                .forEach(name -> log.info("Response Header: {} = {}", name, response.getHeader(name)));

        log.info("<- RESPONSE: URI={}, Status={}, Response={}",
                request.getRequestURI(), response.getStatus(), responsePayload);
    }

    // バイト配列からペイロード文字列を安全に取得するユーティリティ
    private String getPayload(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "[No Payload]";
        }

        // 最大出力長を制限（例：1000文字）
        int length = Math.min(content.length, 1000);
        try {
            return new String(content, 0, length, encoding != null ? encoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "[Unsupported Encoding]";
        }
    }

}