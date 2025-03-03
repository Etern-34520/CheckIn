package indi.etern.checkIn;

import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.service.dao.RequestRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CheckInApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRecord {
    @Autowired
    private RequestRecordService requestRecordService;
    
    private RequestRecord record(Throwable throwable) {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
//        HttpServletResponse httpServletResponse = servletRequestAttributes.getResponse();
//        RequestRecord requestRecord = RequestRecord.from(httpServletRequest, httpServletResponse, throwable);
        RequestRecord requestRecord = new RequestRecord(
                "test",
                "test",
                LocalDateTime.now(),
                114514L,
                "0.0.0.0",
                RequestRecord.Type.VISIT,
                RequestRecord.Status.ERROR,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                null
        );
        Map<String, Object> throwableMap = new HashMap<>();
        throwableMap.put("name", throwable.getClass().getName());
        throwableMap.put("message", throwable.getMessage());
        throwableMap.put("stackTrace", Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString).toList());
        requestRecord.getExtraData().put("throwable", throwableMap);
        requestRecordService.save(requestRecord);
        return requestRecord;
    }
    
    @Test
    public void testException() {
        record(new RuntimeException("test"));
    }
}
