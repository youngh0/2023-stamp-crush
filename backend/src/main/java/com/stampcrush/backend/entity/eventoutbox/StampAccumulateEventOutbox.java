package com.stampcrush.backend.entity.eventoutbox;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampAccumulateEventOutbox {

    @Id
    private UUID id;

    private Long cafeId;

    private Long customerId;

    private int stampCount;

    private boolean state = false;

    public StampAccumulateEventOutbox(UUID id, Long cafeId, Long customerId, int stampCount) {
        this.id = id;
        this.cafeId = cafeId;
        this.customerId = customerId;
        this.stampCount = stampCount;
    }

    public void success() {
        this.state = true;
    }
}
