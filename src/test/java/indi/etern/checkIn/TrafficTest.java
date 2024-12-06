package indi.etern.checkIn;

import indi.etern.checkIn.service.dao.TrafficRecordService;
import org.apache.catalina.connector.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = CheckInApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class TrafficTest {
    @Autowired
    TrafficRecordService trafficRecordService;
    @Test
    @Transactional
    public void testLog(){
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        Request httpServletRequest = new Request(null);
        httpServletRequest.setCoyoteRequest(coyoteRequest);
        httpServletRequest.setRemoteAddr("127.0.0.1");
//        userTrafficService.log(114514,httpServletRequest);
    }
}
