package coyote.commons.log;

import java.util.HashMap;
import java.util.Map;

/**
 * This takes a log entry and parses it into a set of name-value pairs
 */
public class LogEntryMapper {

    private final ParsingMode mode = ParsingMode.APACHE;
    private final String logFormat = "";

    LogEntryMapper() {

    }

    LogEntryMapper(String format) {

    }

    public Map<String, String> map(String logEntry) {
        Map<String, String> retval = new HashMap<>();

        return retval;
    }

}
