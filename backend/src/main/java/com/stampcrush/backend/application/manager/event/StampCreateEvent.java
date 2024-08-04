package com.stampcrush.backend.application.manager.event;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StampCreateEvent {

    private final UUID eventId;
    private final Long cafeId;
    private final Long customerId;
    private final int stampCount;
}
