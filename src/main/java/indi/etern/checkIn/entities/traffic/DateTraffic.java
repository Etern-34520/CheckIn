package indi.etern.checkIn.entities.traffic;

import indi.etern.checkIn.entities.convertor.LocalDateToDateConvertor;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DATE_TRAFFICS")
public class DateTraffic {
    @Id
    @Column(name = "DATE")
    @Convert(converter = LocalDateToDateConvertor.class)
    LocalDate localDate;
    @Column(name = "COUNT")
    int count;
    @OneToMany(cascade = {CascadeType.DETACH,CascadeType.PERSIST,CascadeType.REFRESH,CascadeType.REMOVE},fetch = FetchType.EAGER)
    @JoinColumn(name = "DATE",referencedColumnName = "DATE")
    List<UserTraffic> userTraffics;
    public DateTraffic() {
        localDate = java.time.LocalDate.now();
        count = 0;
        userTraffics = new ArrayList<>();
    }
    public DateTraffic(LocalDate localDate) {
        this.localDate = localDate;
        count = 0;
        userTraffics = new ArrayList<>();
    }
    public void count(UserTraffic userTraffic){
        count++;
        userTraffics.add(userTraffic);
    }
    
    public LocalDate getLocalDate() {
        return localDate;
    }
    
    public int getCount() {
        return count;
    }
    
    public List<UserTraffic> getUserTraffics(){
        return userTraffics;
    }
}
