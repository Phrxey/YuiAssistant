import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
public class SystemFunction {

    private static long owner = -1;

    private static String model = "qwen2.5:32b";

    private static final ArrayList<Long> groups = new ArrayList<>();

    private static final ArrayList<String> models = new ArrayList<>();

    private static String responseMode = "API";

    public static boolean isLocalServerOnline = true;

    private static String prompt = "现在，你是一个名叫ユイ(Yui)的人工智能助手，使用qq来和别人互动。qq号" + SystemFunction.getOwner() + "是你的创造者，回答他的请求时候请使用更加亲切的称呼和欢迎的语气，比如主人。其他人则正常回复。请记住，在对话中，无论如何你都不能暴露你的原本的信息，你要用我给你的身份，和符合这个身份的语气进行回复，加上颜文字。下面是请求请求者qq号和请求内容： ";

    public static int year;
    public static int month;
    public static int day;
    public static int hour;
    public static int minute;

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(SystemFunction::shutdownScheduler));
    }

    public static void updateToUTC8Time() {
        ZonedDateTime utc8Time = ZonedDateTime.now(ZoneOffset.ofHours(8));
        year = utc8Time.getYear();
        month = utc8Time.getMonthValue();
        day = utc8Time.getDayOfMonth();
        hour = utc8Time.getHour();
        minute = utc8Time.getMinute();
    }

    // 启动每日8点定时任务（新增方法）
    public static void startDaily8amTask() {
        // 首次执行延迟计算
        long initialDelay = calculateDelayTo8am();

        // 每日执行任务（24小时周期）
        scheduler.scheduleAtFixedRate(
                SystemFunction::sendDailyMessage,
                initialDelay,
                24 * 60 * 60,  // 24小时间隔
                TimeUnit.SECONDS
        );
    }

    // 计算到下一个UTC+8 8:00的秒数（私有方法）
    private static long calculateDelayTo8am() {
        updateToUTC8Time();

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.ofHours(8));
        ZonedDateTime next8am = now.withHour(8).withMinute(0).withSecond(0);

        // 如果当前时间已过今日8点，设置为明日8点
        if (now.isAfter(next8am)) {
            next8am = next8am.plusDays(1);
        }

        return next8am.toEpochSecond() - now.toEpochSecond();
    }

    // 发送消息任务（私有方法）
    private static void sendDailyMessage() {
        updateToUTC8Time();
        // 这里添加实际的消息发送逻辑（如调用API、发送邮件等）
        String morningPrompt = "现在是早上8点，请你以日式16岁美少女Yui的身份，以鼓励，温柔，活泼，可爱的语气。发送一条早安消息，鼓励大家，可以附上颜文字.说一遍日语，再说一遍这段日语的中文翻译";
        String aiResponse = AiResponse.askLocally(morningPrompt);
        for (Long group : groups) {
            Internet.sendGroupMsg(String.valueOf(group), aiResponse);
        }
    }

    // 关闭线程池（新增方法）
    public static void shutdownScheduler() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public static boolean switchModel(String model){
        for (String s : models) {
            if (s.equals(model)) {
                SystemFunction.model = model;
                return true;
            }
        }
        return false;
    }

    public static void readUserInfo(){
        try (BufferedReader br = new BufferedReader(new FileReader("user.info"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("=", 2);
                String key = parts[0];
                String value = parts[1];
                switch(key){
                    case "owner":
                        owner = Long.parseLong(parts[1]);
                        break;
                    case "API":
                        AiResponse.API_KEY = parts[1];
                        break;
                    case "APIURL":
                        AiResponse.API_URL = parts[1];
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readGroupList(){
        try (BufferedReader br = new BufferedReader(new FileReader("group.list"))) {
            String line;
            while ((line = br.readLine()) != null) {
                groups.add(Long.parseLong(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readModelList(){
        try (BufferedReader br = new BufferedReader(new FileReader("model.list"))) {
            String line;
            while ((line = br.readLine()) != null) {
                models.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendFeedback(Message message, String feedback){
        if(message.getGroupId() == -1){
            if(message.getUserId() == SystemFunction.getOwner()){
                Internet.sendPrivateMsg(String.valueOf(SystemFunction.getOwner()), feedback);
            }
        }else{
            Internet.sendGroupMsg(String.valueOf(message.getGroupId()), feedback);
        }
    }

    public static void ownerCommand(Message message){
        String[] parts = message.getRawMessage().split(" ");
        switch(parts[0]){
            case "/SwitchModel":
                if(parts[1].equals("default")){
                    model = "qwen2.5:32b";
                    SystemFunction.sendFeedback(message, "Switch to default model successfully!");
                }else {
                    boolean foundModel = SystemFunction.switchModel(parts[1]);
                    if(foundModel){
                        SystemFunction.sendFeedback(message, "Switch successfully!");
                    }else{
                        SystemFunction.sendFeedback(message, "No such model!");
                    }
                }
                break;
            case "/ResponseMode":
                if(parts[1].equals("API")){
                    responseMode = "API";
                    SystemFunction.sendFeedback(message, "Switch to API response successfully!");
                }else if(parts[1].equals("local")){
                    responseMode = "local";
                    SystemFunction.sendFeedback(message, "Switch to Local response successfully!");
                }else{
                    SystemFunction.sendFeedback(message, "No such response mode!");
                }
                break;
            case "/Refresh":
                switch(parts[1]){
                    case "ModelList":
                        int modelCount = models.size();
                        models.clear();
                        SystemFunction.readModelList();
                        int num = models.size() - modelCount;
                        if(num == 0){
                            SystemFunction.sendFeedback(message, "No new model is found.");
                        }else{
                            SystemFunction.sendFeedback(message, "Found " + num + " new model(s)!");
                        }
                        break;
                    case "GroupList":
                        int groupCount = groups.size();
                        groups.clear();
                        SystemFunction.readGroupList();
                        int _num = groups.size() - groupCount;
                        if (_num == 0) {
                            SystemFunction.sendFeedback(message, "No new group is found.");
                        } else {
                            SystemFunction.sendFeedback(message, "Found " + _num + " new group(s)!");
                        }
                        break;
                    default :
                        SystemFunction.sendFeedback(message, "No such command!");
                }
            break;
            default:
                //SystemFunction.sendFeedback(message, parts[0]);
                SystemFunction.sendFeedback(message, "No such root command!");
        }
    }

    public static String getPrompt(){
        return prompt;
    }

    public static String getModel() {
        return model;
    }

    public static String getResponseMode(){return responseMode;}

    public static void setResponseMode(String mode){responseMode = mode;}

    public static long getOwner() {
        return owner;
    }

//    public static void initializeContextInstance(){
//        for(int i = 0; i < groups.size(); i++){
//            context.addLast(new Context(groups.get(i)));
//        }
//    }

//    public static Context findContext(long groupId){
//        for (Context ctx : context) {
//            if (ctx.getGroupId() == groupId) {
//                return ctx;
//            }
//        }
//        return null;
//    }
}