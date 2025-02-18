import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.lang.System;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Internet {

    private static final String targetUrl = "http://127.0.0.1:4000";
    private static final String accessToken = "gEHG1cmmGLDm5b69xGdd";

    // 启动HTTP服务器监听127.0.0.1:4000
    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 3000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // 使用默认的 executor
        server.start();
        System.out.println("Server started on 127.0.0.1:3000");
    }

    // 自定义请求处理器
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // 读取请求体
                InputStream requestBody = exchange.getRequestBody();
                String requestText = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                Message message= new Message(requestText);
                System.out.println("Received POST request: " + requestText);
                Internet.handleMessageActions(message);

                // 处理请求并生成响应
                String responseText = "Request received: " + requestText;
                exchange.sendResponseHeaders(200, responseText.length());
                OutputStream os = exchange.getResponseBody();
                os.write(responseText.getBytes());
                os.close();
            } else {
                // 如果不是POST请求，返回405 Method Not Allowed
                String responseText = "Method Not Allowed";
                exchange.sendResponseHeaders(405, responseText.length());
                OutputStream os = exchange.getResponseBody();
                os.write(responseText.getBytes());
                os.close();
            }
        }
    }

    protected static void handleMessageActions(Message message) {
        if(message.getTextMessage().trim().startsWith("/")){
            if(message.getUserId() == SystemFunction.getOwner()){
                SystemFunction.ownerCommand(message);
            }else{
                SystemFunction.sendFeedback(message, "无权限！");
            }
            return;
        }
        if (message.shouldRespondToAt()) {
            SystemFunction.sendFeedback(message, AiResponse.getResponse(message));
        }
    }

    // 发送 POST 请求
    private static void sendPostRequest(String type, String jsonText) {
        String url = targetUrl + "/send_" + type + "_msg";
        try {
            HttpResponse<String> response = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(jsonText)
                    .asString();
            if (response.getStatus() == 200) {
                System.out.println("Message sent successfully: " + response.getBody());
            } else {
                System.out.println("Failed to send message. Status code: " + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendText(String type, String targetId, String text) {
        String jsonText = JsonMaker.toJsonText(type, targetId, text);
        sendPostRequest(type, jsonText);
    }

    public static void sendPrivateMsg(String targetId, String text){
        sendText("private", targetId, text);
    }

    public static void sendGroupMsg(String targetId, String text){
        sendText("group", targetId, text);
    }
}