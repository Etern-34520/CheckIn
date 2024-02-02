package indi.etern.checkIn.api.webSocket.action.traffic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import indi.etern.checkIn.api.webSocket.action.TransactionalAction;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.service.dao.UserTrafficService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GetTrafficByDateAction extends TransactionalAction {
    private final LocalDate localDate;
    public GetTrafficByDateAction(LocalDate localDate){
        this.localDate = localDate;
    }
    /**
     * @param localDateString as 2024-02-01
     * */
    public GetTrafficByDateAction(String localDateString){
        this.localDate = LocalDate.parse(localDateString);
    }
    @Override
    public String requiredPermissionName() {
        return null;
    }

    @Override
    protected Optional<JsonObject> doAction() throws Exception {
        List<UserTraffic> userTrafficList = UserTrafficService.singletonInstance.findAllByDate(localDate);
        JsonObject base = new JsonObject();
        base.addProperty("type","traffics");
        JsonArray array = new JsonArray();
        userTrafficList.stream().forEachOrdered(userTraffic -> {
            JsonObject element = new JsonObject();
            element.addProperty("id",userTraffic.getId());
            element.addProperty("ip",userTraffic.getIP());
            element.addProperty("qqNumber",userTraffic.getQQNumber());
            element.addProperty("localDateTime",userTraffic.getLocalDateTime().toString());
            element.addProperty("localTime",userTraffic.getLocalTime().toString());
            element.addProperty("localDate",userTraffic.getLocalDate().toString());
            array.add(element);
        });
        base.add("traffics",array);
        return Optional.of(base);
    }
}
