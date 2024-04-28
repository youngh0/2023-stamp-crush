package com.stampcrush.backend.repository.eventoutbox;

import static org.junit.jupiter.api.Assertions.*;

import com.stampcrush.backend.entity.eventoutbox.StampAccumulateEventOutbox;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StampAccumulateEventOutboxRepositoryTest {

    @Autowired
    private StampAccumulateEventOutboxRepository stampAccumulateEventOutboxRepository;

    @Test
    void 이벤트를_조회한다() {
        StampAccumulateEventOutbox stampAccumulateEventOutbox = new StampAccumulateEventOutbox(UUID.randomUUID(), 1L,
                1L, 3);

        StampAccumulateEventOutbox saved = stampAccumulateEventOutboxRepository.save(stampAccumulateEventOutbox);
        Assertions.assertThat(stampAccumulateEventOutboxRepository.findById(saved.getId())).isNotEmpty();
    }
}
