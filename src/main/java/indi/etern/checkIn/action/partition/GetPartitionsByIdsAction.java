package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.*;

@Action("getPartitionsByIds")
public class GetPartitionsByIdsAction extends TransactionalAction {
    private final PartitionService partitionService;
    private List<Integer> partitionIds;

    public GetPartitionsByIdsAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        List<Partition> partitions = partitionService.findAllByIds(partitionIds);
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        ArrayList<Object> partitionsList = new ArrayList<>();
        for (Partition partition : partitions) {
            partitionsList.add(partition.toInfoMap());
        }
        map.put("partitions", partitionsList);
        return Optional.of(map);
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        partitionIds = ((List<Number>) dataMap.get("ids")).stream().map(Number::intValue).toList();
    }
}
