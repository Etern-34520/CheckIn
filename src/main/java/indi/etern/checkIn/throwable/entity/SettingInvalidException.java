package indi.etern.checkIn.throwable.entity;

import indi.etern.checkIn.throwable.exam.ExamException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettingInvalidException extends ExamException {
    public SettingInvalidException(String s) {
        super(s);
    }
}