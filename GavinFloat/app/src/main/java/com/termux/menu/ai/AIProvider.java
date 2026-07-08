package com.termux.menu.ai;

import java.util.List;
import java.util.Map;

/** AI 供应商接口 — 重写版，无外部依赖 */
public interface AIProvider {
    String getFormatType();
    String getDisplayName();
    Map<String, String> buildHeaders();
    byte[] buildRequestBody(String model, List<RequestMessage> messages, String systemPrompt, boolean stream);
    String parseResponse(String responseBody);
    String parseStreamChunk(String line);
    boolean isStreamComplete(String line);
    String parseError(int statusCode, String responseBody);

    class RequestMessage {
        public String role;
        public String content;
        public RequestMessage(String role, String content) { this.role = role; this.content = content; }
    }
}
