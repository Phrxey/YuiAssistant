import org.json.*;

public class JsonMaker {
    public static String toJsonText(String type, String qqId, String msg){
        JSONObject jsonObject = new JSONObject();
        if(type.equals("private")){
            jsonObject.put("user_id", qqId);
        }else if(type.equals("group")){
            jsonObject.put("group_id", qqId);
        }

        JSONArray messageArray = new JSONArray();

        JSONObject msgObject = new JSONObject();
        msgObject.put("type", "text");

        JSONObject dataObject = new JSONObject();
        dataObject.put("text", msg);

        msgObject.put("data", dataObject);
        messageArray.put(msgObject);

        jsonObject.put("message", messageArray);
        return jsonObject.toString(4);
    }
}