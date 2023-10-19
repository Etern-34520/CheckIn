package indi.etern.checkIn.dao;

public class DaoException extends RuntimeException {
    public DaoException(String objectNotFound) {
        super(objectNotFound);
    }
    public DaoException(Throwable cause) {
        super(cause);
    }
}
