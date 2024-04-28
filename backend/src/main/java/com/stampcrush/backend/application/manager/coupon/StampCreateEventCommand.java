package com.stampcrush.backend.application.manager.coupon;

import com.stampcrush.backend.application.manager.event.StampCreateEvent;
import com.stampcrush.backend.entity.coupon.Coupon;
import java.util.UUID;

public class StampCreateEventCommand {

    private StampCreateEventCommand() {
    }

    public static StampCreateEvent createEvent(Coupon coupon, int stampCount) {
        return new StampCreateEvent(UUID.randomUUID(), coupon.getCafe(), coupon.getCustomer(), stampCount);
    }
}
