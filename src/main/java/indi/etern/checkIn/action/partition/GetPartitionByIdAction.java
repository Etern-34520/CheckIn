package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getPartitionById")
public class GetPartitionByIdAction extends PartitionMapResultAction {
    private final PartitionService partitionService;
    private Integer partitionId;
    
    public GetPartitionByIdAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        Optional<Partition> optionalPartition = partitionService.findById(partitionId);
        if (optionalPartition.isPresent()) {
            Partition partition = optionalPartition.get();
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            LinkedHashMap<String, Object> partition1 = new LinkedHashMap<>();
            partition1.put("id", partition.getId());
            partition1.put("name", partition.getName());
            result.put("partition", partition1);
            return Optional.of(result);
        } else {
            return getOptionalErrorMap("Partition not found");
        }
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = ((Number) dataMap.get("id")).intValue();
    }
}
