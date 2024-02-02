package indi.etern.checkIn.service.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.traffic.DateTraffic;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.repositories.DateTrafficRepository;
import indi.etern.checkIn.repositories.UserTrafficRepository;
import indi.etern.checkIn.service.web.WebSocketService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Service
public class UserTrafficService {
    public static UserTrafficService singletonInstance;
    @Resource
    private DateTrafficRepository dateTrafficRepository;
    @Resource
    private UserTrafficRepository userTrafficRepository;

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    public UserTrafficService(WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
        UserTrafficService.singletonInstance = this;
    }

    public void log(long QQNumber, HttpServletRequest httpServletRequest) {
        UserTraffic userTraffic = new UserTraffic(QQNumber, httpServletRequest);
        userTrafficRepository.save(userTraffic);
        final Optional<DateTraffic> optionalDateTraffic = dateTrafficRepository.findByLocalDate(LocalDate.now());
        if (optionalDateTraffic.isEmpty()) {
            DateTraffic dateTraffic = new DateTraffic();
            dateTraffic.count(userTraffic);
            dateTrafficRepository.save(dateTraffic);
        } else {
            final DateTraffic dateTraffic = optionalDateTraffic.get();
            dateTraffic.count(userTraffic);
            dateTrafficRepository.save(dateTraffic);
        }
        try {
            webSocketService.sendMessageToAll("{\"type\":\"trafficLog\",\"traffic\":"+ objectMapper.writeValueAsString(userTraffic) +"}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Page<UserTraffic> findAll(Pageable pageable) {
        return userTrafficRepository.findAll(pageable);
    }
    
    
    public Integer count(long qqNumber) {
        return userTrafficRepository.countByQqNumber(qqNumber);
    }
    
    public UserTraffic getById(int id) {
        return userTrafficRepository.findById(id).orElseThrow();
    }
    
    public Optional<UserTraffic> findById(int id) {
        return userTrafficRepository.findById(id);
    }
    
    public Set<UserTraffic> findAllByInfoContainsKeyOf(Long qq, String key) {
        List<UserTraffic> userTraffics = userTrafficRepository.findAllByQqNumber(qq);
        Set<UserTraffic> result = new TreeSet<>((o1, o2) -> o1.getLocalDateTime().isBefore(o2.getLocalDateTime()) ? 1 : -1);
        for (UserTraffic userTraffic : userTraffics) {
            if (userTraffic.getAttributesMap().containsKey(key)) {
                result.add(userTraffic);
            } else if (userTraffic.getHeaderMap().containsKey(key)) {
                result.add(userTraffic);
            }
        }
        return result;
    }
    
    public Set<UserTraffic> findAllByInfoContainsEntryOf(Long qq, String key, String value) {
        List<UserTraffic> userTraffics = userTrafficRepository.findAllByQqNumber(qq);
        Set<UserTraffic> result = new TreeSet<>((o1, o2) -> o1.getLocalDateTime().isBefore(o2.getLocalDateTime()) ? 1 : -1);
        for (UserTraffic userTraffic : userTraffics) {
            if (userTraffic.getAttributesMap().containsKey(key) && userTraffic.getAttributesMap().get(key).equals(value)) {
                result.add(userTraffic);
            } else if (userTraffic.getHeaderMap().containsKey(key) && userTraffic.getHeaderMap().get(key).equals(value)) {
                result.add(userTraffic);
            }
        }
        return result;
    }
    
    public Optional<UserTraffic> findLastByInfoContainsEntryOf(Long qq, String key, String value) {
        final Set<UserTraffic> userTraffics = findAllByInfoContainsEntryOf(qq, key, value);
        if (userTraffics.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(userTraffics.iterator().next());
    }

    public void save(UserTraffic userTraffic) {
        userTrafficRepository.save(userTraffic);
    }

    public List<UserTraffic> findAllByDate(LocalDate localDate) {
        return userTrafficRepository.findAllByLocalDate(localDate, Sort.by(Sort.Direction.DESC,"localTime"));
    }
}
