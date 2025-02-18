import java.io.IOException;
public class Main {
    public static void main(String[] args) {
        try {
            SystemFunction.readUserInfo();
            SystemFunction.readGroupList();
            SystemFunction.readModelList();
            // 1. 先启动HTTP服务器
            Internet.startServer();

            // 2. 启动定时任务
            SystemFunction.startDaily8amTask();

            // 3. 保持主线程存活
            System.out.println("所有服务已启动，程序持续运行中...");
            Thread.sleep(Long.MAX_VALUE);
        } catch (IOException | InterruptedException e) {
            System.err.println("服务启动失败: " + e.getMessage());
            System.exit(1);
        }
    }
}
