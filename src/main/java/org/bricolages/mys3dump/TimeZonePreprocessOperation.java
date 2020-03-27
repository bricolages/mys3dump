package org.bricolages.mys3dump;

import org.apache.log4j.Logger;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shimpei-kodama on 2016/11/07.
 */
class TimeZonePreprocessOperation implements PreprocessOperation {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final DateTimeFormatter srcDateTimeFormat = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("yyyy-MM-dd['T'][ ]HH:mm:ss")).appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).toFormatter();
    private final DateTimeFormatter dstDateTimeFormat = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).toFormatter();
    private final ZoneOffset srcOffset;
    private final ZoneOffset dstOffset;
    private List<Integer> applicableTypes = Arrays.asList(Types.TIMESTAMP);
    private final Boolean isValidConversion;

    public TimeZonePreprocessOperation(ZoneOffset srcZoneOffset, ZoneOffset dstZoneOffset) {
        srcOffset = srcZoneOffset;
        dstOffset = dstZoneOffset;
        isValidConversion = srcOffset.compareTo(dstOffset) == 0 ? false : true;
        logger.info("Init TimeZonePreprocessOperation: src-zone-offset=" + srcOffset.toString() + ", dst-zone-offset=" + dstOffset.toString());
    }

    public String apply(String value) {
        if (srcOffset.compareTo(dstOffset) == 0 || value.equals("0000-00-00 00:00:00")) {
            return value;
        }
        LocalDateTime local_time = LocalDateTime.parse(value, srcDateTimeFormat);
        OffsetDateTime src_time = OffsetDateTime.of(local_time, srcOffset);
        return src_time.withOffsetSameInstant(dstOffset).format(dstDateTimeFormat);
    }

    public Boolean isApplicableTo(int type) {
        return applicableTypes.contains(type);
    }

    public Boolean isValid() {
        return isValidConversion;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(src-zone-offset=" + srcOffset + ",dst-zone-offset=" + dstOffset + ")";
    }
}
