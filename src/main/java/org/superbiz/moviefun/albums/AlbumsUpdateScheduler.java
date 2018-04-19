package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final AlbumsUpdater albumsUpdater;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AlbumsUpdateScheduler(DataSource dataSource, AlbumsUpdater albumsUpdater) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.albumsUpdater = albumsUpdater;
    }

    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    public void run() {
        try {
            if (startAlbumSchedulerTask()) {
                logger.debug("Starting albums update");
                albumsUpdater.update();
                Instant instant = Instant.now();
                Timestamp timestamp = new Timestamp(instant.toEpochMilli());
                jdbcTemplate.update("UPDATE album_scheduler_task SET started_at = ?", timestamp);
                logger.debug("Finished albums update");
            } else {
                logger.debug("Nothing to start");
            }

        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }

    private boolean startAlbumSchedulerTask() {
        LocalDateTime startedAt = getStartedAt();
        return LocalDateTime.now().plusMinutes(2).isAfter(startedAt);
    }

    private LocalDateTime getStartedAt() {
        Timestamp startedAt = jdbcTemplate.queryForObject("SELECT started_at FROM album_scheduler_task", Timestamp.class);
        return Optional
                .ofNullable(startedAt)
                .map(Timestamp::toLocalDateTime)
                .orElse(LocalDateTime.now().plusMinutes(1));
    }
}
