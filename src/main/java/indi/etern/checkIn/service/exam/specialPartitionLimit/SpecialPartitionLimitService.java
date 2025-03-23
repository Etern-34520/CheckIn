package indi.etern.checkIn.service.exam.specialPartitionLimit;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class SpecialPartitionLimitService {
    private final SettingService settingService;
    private Map<String,SpecialPartitionLimit> specialPartitionLimits;
    public static SpecialPartitionLimitService singletonInstance;
    
    public SpecialPartitionLimitService(SettingService settingService) {
        this.settingService = settingService;
        singletonInstance = this;
    }
    
    public static Map<String, SpecialPartitionLimit> from(Collection<Map<String, Object>> values) {
        Map<String, SpecialPartitionLimit> partitionLimitMap = new HashMap<>();
        values.forEach(dataMap -> {
            SpecialPartitionLimit specialPartitionLimit = new SpecialPartitionLimit();
            final String partitionId1 = dataMap.get("partitionId").toString();
            specialPartitionLimit.partitionId = partitionId1;
            specialPartitionLimit.minLimitEnabled = (boolean) dataMap.get("minLimitEnabled");
            specialPartitionLimit.minLimit = (int) dataMap.get("minLimit");
            specialPartitionLimit.maxLimitEnabled = (boolean) dataMap.get("maxLimitEnabled");
            specialPartitionLimit.maxLimit = (int) dataMap.get("maxLimit");
            partitionLimitMap.put(partitionId1,specialPartitionLimit);
        });
        return partitionLimitMap;
    }
    
    public void flush() {
        SettingItem settingItem4 = settingService.getItem("generating","specialPartitionLimits");
        //noinspection unchecked
        Map<String,Map<String,Object>> map = settingItem4.getValue(Map.class);
        specialPartitionLimits = SpecialPartitionLimitService.from(map.values());
    }
    public SpecialPartitionLimit getLimitOf(Partition partition) {
        flush();
        return specialPartitionLimits.get(partition.getId());
    }
    public Map<Partition,SpecialPartitionLimit> getLimits(Collection<Partition> partitions) {
        flush();
        Map<Partition,SpecialPartitionLimit> map = new HashMap<>();
        partitions.forEach(partition -> map.put(partition,specialPartitionLimits.get(partition.getId())));
        return map;
    }
    
    public Map<Partition,SpecialPartitionLimit> getAll() {
        flush();
        Map<Partition,SpecialPartitionLimit> map = new HashMap<>();
        specialPartitionLimits.forEach((id,limit) -> map.put(Partition.ofId(id),limit));
        return map;
    }
}