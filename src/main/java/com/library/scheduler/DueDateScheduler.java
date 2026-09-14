package com.library.scheduler;

import com.library.model.Loan;
import com.library.repository.LoanRepository;
import com.library.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DueDateScheduler {

    private static final Logger log = LoggerFactory.getLogger(DueDateScheduler.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Autowired private LoanRepository loanRepository;
    @Autowired private NotificationService notificationService;

    // Daily at 9 AM — notify members with books due in 2 days
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDueSoonReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(2);
        List<Loan> dueSoon = loanRepository.findByDueDateAndReturnedFalse(targetDate);

        log.info("🔔 Sending due-soon reminders for {} loans", dueSoon.size());

        for (Loan loan : dueSoon) {
            String fcmToken = loan.getMember().getFcmToken();
            if (fcmToken != null && !fcmToken.isBlank()) {
                notificationService.notifyDueSoon(
                        fcmToken,
                        loan.getBook().getTitle(),
                        loan.getDueDate().format(DATE_FMT)
                );
            }
        }
    }

    // Daily at 10 AM — notify members with overdue books
    @Scheduled(cron = "0 0 10 * * *")
    public void sendOverdueReminders() {
        List<Loan> overdue = loanRepository.findOverdueLoans();

        log.info("🔔 Sending overdue reminders for {} loans", overdue.size());

        for (Loan loan : overdue) {
            String fcmToken = loan.getMember().getFcmToken();
            if (fcmToken != null && !fcmToken.isBlank()) {
                notificationService.notifyOverdue(
                        fcmToken,
                        loan.getBook().getTitle(),
                        loan.calculateFine()
                );
            }
        }
    }
}