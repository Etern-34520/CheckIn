package indi.etern.checkIn.beans;

import indi.etern.checkIn.entities.traffic.DateTraffic;
import indi.etern.checkIn.service.dao.DateTrafficService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrafficInfo {
    private final DateTrafficService dateTrafficService;
    private List<DateTraffic> dateTraffics;
    protected TrafficInfo(DateTrafficService dateTrafficService, UserTrafficService userTrafficService) {
        this.dateTrafficService = dateTrafficService;
    }
    public List<DateTraffic> getDateTraffics() {
        dateTraffics = dateTrafficService.findAll();
        return dateTraffics;
    }
}
