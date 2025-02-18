import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import org.json.JSONException; // 用于处理JSON相关异常

import java.lang.System;
public class AiResponse {
    public static String API_URL = "";
    public static String API_KEY = "";
    public static String LOCAL_API_URL = "";
    public static String askByApi(String question){
        try {
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen2.5-72b-instruct");
            requestBody.put("messages", new JSONObject[]{
                    new JSONObject().put("role", "user").put("content", question)
            });
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            // 发送 POST 请求
            HttpResponse<String> response = Unirest.post(API_URL)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .body(requestBody.toString())
                    .asString();

            // 解析响应
            if (response.getStatus() == 200) {
                JSONObject responseBody = new JSONObject(response.getBody());
                return responseBody.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                System.err.println("Request failed, status code: " + response.getStatus());
                System.err.println("Response: " + response.getBody());
                return null;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String askLocally(String question) {
        StringBuilder fullResponse = new StringBuilder();
        try {
            // 构建流式请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", SystemFunction.getModel());
            requestBody.put("prompt", question);
            requestBody.put("stream", true); // 启用流式传输

            InputStream inputStream = Unirest.post(LOCAL_API_URL)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .asObject(InputStream.class)
                    .getBody();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    JSONObject chunk = new JSONObject(line);
                    if (chunk.has("response")) {
                        fullResponse.append(chunk.getString("response"));
                    }

                    // 检查是否结束
                    if (chunk.optBoolean("done", false)) {
                        break;
                    }
                }
            }
            return fullResponse.toString();
        } catch (UnirestException | IOException e) {
            System.err.println("Stream request failed: " + e.getMessage());
            SystemFunction.isLocalServerOnline = false;
            SystemFunction.setResponseMode("API");//本地服务离线，强制将回复模式换为API
            return null;
        } catch (JSONException e) {
            System.err.println("Response parsing failed: " + e.getMessage());
            return !fullResponse.isEmpty() ? fullResponse.toString() : null;
        }
    }

    public static String getResponse(Message message){
        String userInput = message.getTextMessage();
        String response = "";
        if(userInput != null){
            if(SystemFunction.getResponseMode().equals("API")){
                response = AiResponse.askByApi(SystemFunction.getPrompt()+ "user id: " + message.getUserId() + " request: " + message.getTextMessage());
            }else if(SystemFunction.getResponseMode().equals("local")){
                if(!SystemFunction.isLocalServerOnline){
                    response = AiResponse.askLocally(SystemFunction.getPrompt()+ "user id: " + message.getUserId() + " request: " + message.getTextMessage());
                }else{
                    if(checkLocalServer()){
                        response = AiResponse.askLocally(SystemFunction.getPrompt()+ "user id: " + message.getUserId() + " request: " + message.getTextMessage());
                        SystemFunction.isLocalServerOnline = true;
                    }else{
                        SystemFunction.sendFeedback(message, "本地服务离线，使用API服务");
                        response = AiResponse.askByApi(SystemFunction.getPrompt()+ "user id: " + message.getUserId() + " request: " + message.getTextMessage());
                    }
                }
            }
        }
        return response;
    }

    private static boolean checkLocalServer() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 23544), 100);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
//    public static String getResponse(Message message, String responseMode){
//        String userInput = message.getTextMessage();
//        String response = "";
//        if(userInput != null){
//            if(responseMode.equals("API")){
//                response = AiResponse.askByApi(SystemFunction.getPrompt()+ "user id: " + message.getUserId() + " request: " + message.getTextMessage());
//            }else if(responseMode.equals("local")){
//                response = AiResponse.askLocally(SystemFunction.getPrompt()+ "user id: " + message.getUserId() + " request: " + message.getTextMessage());
//            }
//        }
//        return response;
//    }
}
