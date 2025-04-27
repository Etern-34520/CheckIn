package indi.etern.checkIn.action.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface OutputData {
    enum Result {SUCCESS, WARNING, ERROR}
    @JsonIgnore
    Result result();
}
