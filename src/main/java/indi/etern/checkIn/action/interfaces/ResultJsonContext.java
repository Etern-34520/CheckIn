package indi.etern.checkIn.action.interfaces;

import java.util.Optional;

public interface ResultJsonContext<O extends OutputData> extends ResultContext<O> {
    Optional<String> getOptionalJsonResult();
}