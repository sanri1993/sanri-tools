package com.sanri.tools;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

public class CronMain {

    @Test
    public void testSpringCron(){
        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator("1-2/1 * * * * ?");
        Date current = new Date();
        int count = 10;
        while (count --> 0) {
            current = cronSequenceGenerator.next(current);
            System.out.println(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(current));
        }
    }
}
