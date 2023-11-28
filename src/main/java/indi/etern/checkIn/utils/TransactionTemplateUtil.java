package indi.etern.checkIn.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TransactionTemplateUtil {
    @Getter
    private static TransactionTemplate transactionTemplate;
    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        TransactionTemplateUtil.transactionTemplate = transactionTemplate;
    }
}
