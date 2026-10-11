package com.wmrt.dispatch.config;

import com.wmrt.dispatch.repository.TimeBlockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Seeds time_blocks for all 4 zones for the next 14 weekdays (Monday-Friday) on startup.
 * Each weekday gets 4 dispatch windows with preset capacity and cost.
 * Uses a count-check guard to avoid re-inserting already existing blocks (idempotent).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Dispatch windows: [startTime, endTime, cost] */
    private static final Object[][] TIME_SLOTS = {
            {"09:00", "12:00", 5990.0},
            {"12:00", "14:00", 4990.0},
            {"14:00", "17:00", 5990.0},
            {"17:00", "20:00", 6990.0}
    };

    private static final int CAPACITY = 10;
    private static final long[] ZONE_IDS = {1L, 2L, 3L, 4L, 5L};

    private final TimeBlockRepository timeBlockRepository;

    public DataSeeder(TimeBlockRepository timeBlockRepository) {
        this.timeBlockRepository = timeBlockRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("DataSeeder: starting time block seeding...");
        int inserted = 0;

        LocalDate cursor = LocalDate.now();
        int weekdaysSeeded = 0;

        while (weekdaysSeeded < 14) {
            if (isWeekend(cursor)) {
                cursor = cursor.plusDays(1);
                continue;
            }

            for (long zoneId : ZONE_IDS) {
                for (Object[] slot : TIME_SLOTS) {
                    String startTime = (String) slot[0];
                    String endTime   = (String) slot[1];
                    double cost      = (double) slot[2];

                    int count = timeBlockRepository.countByZoneDateStartTime(zoneId, cursor, startTime);
                    if (count == 0) {
                        timeBlockRepository.insert(zoneId, cursor, startTime, endTime, CAPACITY, cost);
                        inserted++;
                    }
                }
            }

            weekdaysSeeded++;
            cursor = cursor.plusDays(1);
        }

        log.info("DataSeeder: finished. Inserted {} new time block(s).", inserted);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
