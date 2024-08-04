package com.stampcrush.backend.scheduling;

import com.stampcrush.backend.entity.eventoutbox.StampAccumulateEventOutbox;
import com.stampcrush.backend.repository.eventoutbox.StampAccumulateEventOutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EventOutboxScheduler {

    private final StampAccumulateEventOutboxRepository stampAccumulateEventOutboxRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    @Scheduled(cron = "*/10 * * * * *")
    public void scheduledStampCreateEvent() {
        List<StampAccumulateEventOutbox> falseStampCreateEvents = stampAccumulateEventOutboxRepository.findByStateIsFalse();
        falseStampCreateEvents.forEach(event -> applicationEventPublisher.publishEvent(event));
    }
}
