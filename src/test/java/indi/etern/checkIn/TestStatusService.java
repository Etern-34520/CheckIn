package indi.etern.checkIn;

import indi.etern.checkIn.service.exam.StatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = CheckInApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestStatusService {
    @Autowired
    StatusService statusService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @Test
    public void testStatusService() {
        System.out.println(statusService.checkGenerateAvailability());
        System.out.println(statusService.checkSubmitAvailability());
    }
    
    // 每个线程处理请求的数量
//    private static final int REQUESTS_PER_THREAD = 500000;
    private static final int REQUESTS_PER_THREAD = 100000;
    // 线程的数量
//    private static final int NUMBER_OF_THREADS = 20;
    private static final int NUMBER_OF_THREADS = 30;
    
    @Test
//    @Transactional
    public void test() throws InterruptedException {
        long startTime;
        Map<Integer, List<StatusService.ServerStatuses>> threadResultMap = new HashMap<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)) {
            startTime = System.currentTimeMillis();
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                int finalI = i;
                executor.execute(() -> {
                    List<StatusService.ServerStatuses> serverStatuses = new ArrayList<>(REQUESTS_PER_THREAD);
                    threadResultMap.put(finalI, serverStatuses);
                    transactionTemplate.executeWithoutResult((status) -> {
                        for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                            // 模拟处理请求
                            try {
                                statusService.flush();
                                serverStatuses.add(serverStatuses.get(finalI));
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                    });
                });
            }
            
            executor.shutdown(); // 关闭线程池
            executor.awaitTermination(1, TimeUnit.HOURS); // 等待所有任务完成
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime; // 计算总时间
        long operateCount = threadResultMap.values().stream().mapToInt(List::size).sum();
        System.out.println("总耗时: " + totalTime + " 毫秒");
        System.out.println("吞吐量: " + (NUMBER_OF_THREADS * REQUESTS_PER_THREAD * 1000.0 / totalTime) + " 请求/秒");
        System.out.println("实际操作数: " + operateCount);
        System.out.println("实际/理论: " + operateCount * 1.0 / (NUMBER_OF_THREADS * REQUESTS_PER_THREAD));
        System.out.println("有效吞吐量: " + (operateCount * 1000.0 / totalTime) + " 请求/秒");
    }
}
