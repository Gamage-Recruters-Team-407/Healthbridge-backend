package lk.gamage.backend.healthbridgebackend.scheduler;

import lk.gamage.backend.healthbridgebackend.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStatusScheduler {
    private static final Logger log = LoggerFactory.getLogger(AppointmentStatusScheduler.class);
    private final AppointmentService appointmentService;

    public AppointmentStatusScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void completeElapsedAppointments() {
        long updated = appointmentService.completeElapsedActiveAppointments();
        if (updated > 0) log.info("Marked {} elapsed appointments as completed", updated);
    }
}