package indi.etern.checkIn.action.interfaces;

public interface OutputData {
    enum Result {SUCCESS, WARNING, ERROR}
    Result result();
}
