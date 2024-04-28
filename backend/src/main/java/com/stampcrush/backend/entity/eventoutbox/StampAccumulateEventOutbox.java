package com.stampcrush.backend.entity.eventoutbox;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class StampAccumulateEventOutbox {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long cafeId;

    private Long customerId;

    private int stampCount;

    private boolean state = false;

    public void success() {
        this.state = true;
    }
}
