package indi.etern.checkIn.service.exam.specialPartitionLimit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialPartitionLimit {
    int partitionId;
    boolean minLimitEnabled;
    int minLimit;
    boolean maxLimitEnabled;
    int maxLimit;
    
    public boolean checkMax(int count) {
        return !maxLimitEnabled || count <= maxLimit;
    }
    
    public boolean checkMin(int count) {
        return !minLimitEnabled || count >= minLimit;
    }
}