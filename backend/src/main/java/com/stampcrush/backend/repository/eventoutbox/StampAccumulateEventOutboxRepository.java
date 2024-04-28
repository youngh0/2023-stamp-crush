package com.stampcrush.backend.repository.eventoutbox;

import com.stampcrush.backend.entity.eventoutbox.StampAccumulateEventOutbox;
import org.springframework.data.repository.Repository;

public interface StampAccumulateEventOutboxRepository extends Repository<StampAccumulateEventOutbox, Long> {

    StampAccumulateEventOutbox save(StampAccumulateEventOutbox stampAccumulateEventOutbox);
}
