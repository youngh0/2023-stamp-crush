package com.stampcrush.backend.application.manager.event.visitHistory;

import com.stampcrush.backend.application.manager.event.StampCreateEvent;
import com.stampcrush.backend.entity.cafe.Cafe;
import com.stampcrush.backend.entity.eventoutbox.StampAccumulateEventOutbox;
import com.stampcrush.backend.entity.user.Customer;
import com.stampcrush.backend.entity.visithistory.VisitHistory;
import com.stampcrush.backend.exception.CafeNotFoundException;
import com.stampcrush.backend.exception.CustomerNotFoundException;
import com.stampcrush.backend.exception.NotFoundException;
import com.stampcrush.backend.repository.cafe.CafeRepository;
import com.stampcrush.backend.repository.eventoutbox.StampAccumulateEventOutboxRepository;
import com.stampcrush.backend.repository.user.CustomerRepository;
import com.stampcrush.backend.repository.visithistory.VisitHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VisitHistoryListener {

    private final VisitHistoryRepository visitHistoryRepository;
    private final CafeRepository cafeRepository;
    private final CustomerRepository customerRepository;
    private final StampAccumulateEventOutboxRepository stampAccumulateEventOutboxRepository;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createVisitHistory(StampCreateEvent stampCreateEvent) {
        Cafe cafe = findCafe(stampCreateEvent.getCafeId());
        Customer customer = findCustomer(stampCreateEvent.getCustomerId());
        VisitHistory visitHistory = new VisitHistory(cafe, customer, stampCreateEvent.getStampCount());
        visitHistoryRepository.save(visitHistory);

        StampAccumulateEventOutbox stampAccumulateEventOutbox = findStampCreateEvent(stampCreateEvent);
        stampAccumulateEventOutbox.success();
    }

    private StampAccumulateEventOutbox findStampCreateEvent(StampCreateEvent stampCreateEvent) {
        return stampAccumulateEventOutboxRepository.findById(
                        stampCreateEvent.getEventId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 event 입니다"));
    }

    private Cafe findCafe(Long cafeId) {
        return cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CafeNotFoundException("존재하지 않는 카페입니다."));
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("존재하지 않는 고객입니다."));
    }
}
