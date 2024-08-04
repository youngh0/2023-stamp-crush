package com.stampcrush.backend.application.manager.event.outbox;

import com.stampcrush.backend.application.manager.event.StampCreateEvent;
import com.stampcrush.backend.entity.eventoutbox.StampAccumulateEventOutbox;
import com.stampcrush.backend.repository.eventoutbox.StampAccumulateEventOutboxRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxListener {

    private final StampAccumulateEventOutboxRepository stampAccumulateEventOutboxRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    public void saveStampAccumulateOutbox(StampCreateEvent stampCreateEvent) {
        UUID eventId = stampCreateEvent.getEventId();
        Long cafeId = stampCreateEvent.getCafeId();
        Long customerId = stampCreateEvent.getCustomerId();
        int stampCount = stampCreateEvent.getStampCount();

        StampAccumulateEventOutbox stampOutbox = new StampAccumulateEventOutbox(eventId, cafeId, customerId, stampCount);
        stampAccumulateEventOutboxRepository.save(stampOutbox);
    }
}
