package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.traffic.DateTraffic;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.repositories.DateTrafficRepository;
import indi.etern.checkIn.repositories.UserTrafficRepository;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserTrafficService {
    @Resource
    private DateTrafficRepository dateTrafficRepository;
    @Resource
    private UserTrafficRepository userTrafficRepository;
    
    public void log(int QQNumber, HttpServletRequest httpServletRequest){
        UserTraffic userTraffic = new UserTraffic(QQNumber,httpServletRequest);
        userTrafficRepository.save(userTraffic);
        final Optional<DateTraffic> optionalDateTraffic = dateTrafficRepository.findByLocalDate(LocalDate.now());
        if (optionalDateTraffic.isEmpty()){
            DateTraffic dateTraffic = new DateTraffic();
            dateTraffic.count(userTraffic);
            dateTrafficRepository.save(dateTraffic);
        } else {
            final DateTraffic dateTraffic = optionalDateTraffic.get();
            dateTraffic.count(userTraffic);
            dateTrafficRepository.save(dateTraffic);
        }
    }
    
    public Page<UserTraffic> findAll(Pageable pageable) {
        return userTrafficRepository.findAll(pageable);
    }
}
