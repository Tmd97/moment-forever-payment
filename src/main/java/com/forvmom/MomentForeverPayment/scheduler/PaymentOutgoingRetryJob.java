package com.forvmom.MomentForeverPayment.scheduler;
import com.forvmom.MomentForeverPayment.scheduler.OutgoingPaymentPublisher;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutgoingRetryJob implements Job {

    private final OutgoingPaymentPublisher publisher;

    public PaymentOutgoingRetryJob(OutgoingPaymentPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void execute(JobExecutionContext context) {
        publisher.publishPendingEvents();
    }
}