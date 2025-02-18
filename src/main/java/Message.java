import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.System;
public class Message {
    // 实例变量（非静态）
    private long selfId;
    private long userId;
    private long groupId;
    private long time;
    private long messageId;
    private String messageType;
    private String subType;
    private String rawMessage;
    private String messageArrayType;

    private String textMessage;

    // 不同消息类型对应的字段（根据case分支添加）
    private String textContent;
    private String imageFile;
    private String imageUrl;
    private long imageSize;
    private String fileName;
    private String fileUrl;
    private long fileSize;
    private long atTargetQQ;

    public Message(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            // 解析基础字段（保持不变）
            this.selfId = jsonObject.getLong("self_id");
            this.userId = jsonObject.getLong("user_id");
            this.time = jsonObject.getLong("time");
            this.messageId = jsonObject.getLong("message_id");
            this.messageType = jsonObject.getString("message_type");
            this.subType = jsonObject.getString("sub_type");
            this.rawMessage = jsonObject.getString("raw_message");
            this.groupId = parseGroupId(jsonObject);

            // 解析消息数组（保持不变）
            JSONArray messageArray = jsonObject.getJSONArray("message");
            parseMessageArray(messageArray);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }

    // 解析群组ID（示例方法）
    private long parseGroupId(JSONObject jsonObject) {
        return "group".equals(this.messageType) ?
                jsonObject.getLong("group_id") :
                -1; // 非群组消息返回-1
    }

    // 解析消息数组
    private void parseMessageArray(JSONArray messageArray) {
        for (int i = 0; i < messageArray.length(); i++) {
            JSONObject messageObject = messageArray.getJSONObject(i);
            this.messageArrayType = messageObject.getString("type");
            JSONObject dataObject = messageObject.getJSONObject("data");

            switch (this.messageArrayType) {
                case "text" -> handleText(dataObject);
                case "image" -> handleImage(dataObject);
                case "file" -> handleFile(dataObject);
                case "at" -> handleAt(dataObject);
                default -> System.out.println("Unknown type: " + this.messageArrayType);
            }
        }
    }

    // 文本消息处理
    private void handleText(JSONObject data) {
        this.textContent = data.getString("text");
        textMessage = textContent;
        System.out.println("Text Message: " + textContent);
    }

    // 图片消息处理
    private void handleImage(JSONObject data) {
        this.imageFile = data.getString("file");
        this.imageUrl = data.getString("url");
        this.imageSize = data.getLong("file_size");
        System.out.println("Image File: " + imageFile);
        System.out.println("Image URL: " + imageUrl);
        System.out.println("Image Size: " + imageSize);
    }

    // 文件消息处理
    private void handleFile(JSONObject data) {
        this.fileName = data.getString("file");
        this.fileUrl = data.getString("url");
        this.fileSize = data.getLong("file_size");
        System.out.println("File Name: " + fileName);
        System.out.println("File URL: " + fileUrl);
        System.out.println("File Size: " + fileSize);
    }

    // @消息处理
    private void handleAt(JSONObject data) {
        this.atTargetQQ = data.getLong("qq");
        System.out.println("At Message - Target QQ: " + atTargetQQ);

        if (this.selfId == atTargetQQ) {
            if (this.userId == SystemFunction.getOwner()) {
                Internet.sendGroupMsg(String.valueOf(groupId), "是的，主人，我在！");
            }else{
                Internet.sendGroupMsg(String.valueOf(groupId), "您好，我在！");
            }
        } else {
            System.out.println("Someone else was mentioned");
        }
    }

    public boolean shouldRespondToAt() {
        return ((this.selfId == atTargetQQ) || (this.messageType.equals("private") && this.userId == SystemFunction.getOwner()));
    }

    public boolean isFromOwner() {
        return userId == SystemFunction.getOwner();
    }

    public long getGroupId() {
        return groupId;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String getMessageArrayType(){
        return messageArrayType;
    }

    public long getUserId(){
        return userId;
    }

    public String getTextMessage(){
        return textMessage;
    }

    public boolean isFromPrivate(){
        return messageType.equals("private");
    }
}