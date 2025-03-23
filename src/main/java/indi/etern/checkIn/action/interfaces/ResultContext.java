package indi.etern.checkIn.action.interfaces;

public interface ResultContext<O extends OutputData> {
    O getOutput();
}