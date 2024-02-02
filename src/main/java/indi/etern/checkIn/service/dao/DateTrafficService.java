package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.traffic.DateTraffic;
import indi.etern.checkIn.repositories.DateTrafficRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DateTrafficService{
    @Resource
    private DateTrafficRepository dateTrafficRepository;
    public void save(DateTraffic dateTraffic){
        dateTrafficRepository.save(dateTraffic);
    }
    public DateTraffic getByLocalDate(LocalDate localDate){
        return dateTrafficRepository.findByLocalDate(localDate).orElse(new DateTraffic());
    }
    public List<DateTraffic> getAllByLocalDate(List<LocalDate> localDates){
        List<DateTraffic> dateTraffics = new ArrayList<>();
        for (LocalDate localDate:localDates){
            dateTraffics.add(getByLocalDate(localDate));
        }
        return dateTraffics;
    }
    public List<DateTraffic> getAllFromTo(LocalDate from,LocalDate to){
        if (from.isAfter(to)){
            throw new DateTrafficServiceException("\"to\" date cannot after the \"from\" date");
        } else {
            List<LocalDate> localDates = new ArrayList<>();
            LocalDate localDate = from;
            while (!localDate.minusDays(1).equals(to)) {
                localDates.add(localDate);
                localDate = localDate.plusDays(1);
            }
            final List<DateTraffic> allByIdWithFill = dateTrafficRepository.findAllByIdWithFill(localDates);
            allByIdWithFill.sort(new DateTrafficComparator());
            return allByIdWithFill;
        }
    }
    
    public Page<DateTraffic> findAll(Pageable pageable) {
        return dateTrafficRepository.findAll(pageable);
    }

    public List<DateTraffic> findAll() {
        return dateTrafficRepository.findAll(Sort.by(Sort.Direction.DESC,"localDate"));
    }
    
    private static class DateTrafficComparator implements Comparator<DateTraffic> {
        @Override
        public int compare(DateTraffic o1, DateTraffic o2) {
            return o1.getLocalDate().compareTo(o2.getLocalDate());
        }
    }
}
