package com.stampcrush.backend.repository.eventoutbox;

import com.stampcrush.backend.entity.eventoutbox.StampAccumulateEventOutbox;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface StampAccumulateEventOutboxRepository extends Repository<StampAccumulateEventOutbox, Long> {

    StampAccumulateEventOutbox save(StampAccumulateEventOutbox stampAccumulateEventOutbox);

    Optional<StampAccumulateEventOutbox> findById(UUID id);

    List<StampAccumulateEventOutbox> findByStateIsFalse();
}
