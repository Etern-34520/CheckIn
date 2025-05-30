package indi.etern.checkIn.throwable.entity;

import indi.etern.checkIn.throwable.exam.ExamException;

public class SettingInvalidException extends ExamException {
    public SettingInvalidException(String s) {
        super(s);
    }
}