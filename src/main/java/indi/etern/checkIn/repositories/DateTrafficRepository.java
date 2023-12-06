package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.traffic.DateTraffic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DateTrafficRepository extends JpaRepository<DateTraffic,LocalDate> {
    default Optional<DateTraffic> findByLocalDate(LocalDate localDate){
        return findById(localDate);
    }
    
    default List<DateTraffic> findAllByIdWithFill(List<LocalDate> ids){
        final List<DateTraffic> dateTraffics = findAllById(ids);
        for (DateTraffic dateTraffic:dateTraffics) {
            ids.remove(dateTraffic.getLocalDate());
        }
        if (!ids.isEmpty()){
            for (LocalDate localDate:ids) {
                dateTraffics.add(new DateTraffic(localDate));
            }
        }
        return dateTraffics;
    }
}
