package indi.etern.checkIn.action.traffic;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.service.dao.UserTrafficService;

import java.time.LocalDate;
import java.util.*;

@Action("getTrafficByDate")
public class GetTrafficByDateAction extends TransactionalAction {
    private LocalDate localDate;

    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        List<UserTraffic> userTrafficList = UserTrafficService.singletonInstance.findAllByDate(localDate);
        LinkedHashMap<String,Object> base = new LinkedHashMap<>();
        base.put("type","traffics");
        ArrayList<Object> array = new ArrayList<>();
        userTrafficList.stream().forEachOrdered(userTraffic -> {
            LinkedHashMap<String,Object> element = new LinkedHashMap<>();
            element.put("id",userTraffic.getId());
            element.put("ip",userTraffic.getIP());
            element.put("qqNumber",userTraffic.getQQNumber());
            element.put("localDateTime",userTraffic.getLocalDateTime().toString());
            element.put("localTime",userTraffic.getLocalTime().toString());
            element.put("localDate",userTraffic.getLocalDate().toString());
            array.add(element);
        });
        base.put("traffics",array);
        return Optional.of(base);
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        //as 2024-02-01
        localDate = LocalDate.parse(String.valueOf(dataMap.get("date")));
    }
}
