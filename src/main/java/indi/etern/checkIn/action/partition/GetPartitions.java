package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.utils.TransactionTemplateUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getPartitions")
public class GetPartitions extends TransactionalAction {
    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        TransactionTemplateUtil.getTransactionTemplate().execute((res) -> {
            ArrayList<Object> partitionList = new ArrayList<>();
            result.put("partitions", partitionList);
            PartitionService.singletonInstance.findAll().forEach(partition -> {
                LinkedHashMap<String,Object> partitionInfo = new LinkedHashMap<>();
                partitionInfo.put("id", partition.getId());
                partitionInfo.put("name", partition.getName());
                partitionInfo.put("empty", partition.getQuestionLinks().isEmpty());
                partitionList.add(partitionInfo);
            });
            return null;
        });
        return Optional.of(result);
    }
}
