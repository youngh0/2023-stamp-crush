package com.stampcrush.backend.application.manager.event;

import com.stampcrush.backend.entity.cafe.Cafe;
import com.stampcrush.backend.entity.user.Customer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Cache;

@Getter
@RequiredArgsConstructor
public class StampCreateEvent {

    private final Cafe cafe;
    private final Customer customer;
    private final int stampCount;
}
