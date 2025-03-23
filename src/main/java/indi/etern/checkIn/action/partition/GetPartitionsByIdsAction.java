package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Action("getPartitionsByIds")
public class GetPartitionsByIdsAction extends BaseAction1<GetPartitionsByIdsAction.Input, GetPartitionsByIdsAction.Output> {
    public record Input(@Nonnull List<String> ids) implements InputData {}
    public record Output(List<Map<String,Object>> partitions) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final PartitionService partitionService;

    public GetPartitionsByIdsAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, Output> context) {
        final Input input = context.getInput();
        List<Partition> partitions = partitionService.findAllByIds(input.ids);
        ArrayList<Map<String,Object>> partitionDataList = new ArrayList<>();
        for (Partition partition : partitions) {
            partitionDataList.add(partition.toInfoMap());
        }
        context.resolve(new Output(partitionDataList));
    }
}
