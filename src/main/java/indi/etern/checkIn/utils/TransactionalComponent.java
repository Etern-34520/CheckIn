package indi.etern.checkIn.utils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalComponent {
    
    public interface Cell {
        void run();
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void required(Cell cell) throws Exception {
        cell.run();
    }
}