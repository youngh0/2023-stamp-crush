package com.stampcrush.backend.application.manager.coupon;

import com.stampcrush.backend.application.manager.NamedLockService;
import com.stampcrush.backend.application.manager.coupon.dto.StampCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CouponCommandFacadeService {

    private final NamedLockService namedLockService;
    private final ManagerCouponCommandService managerCouponCommandService;

    public void createStamp(StampCreateDto stampCreateDto) {
        namedLockService.executeWithLock(
                "create_stamp" + stampCreateDto.getCouponId(),
                3000,
                () ->  managerCouponCommandService.createStamp(stampCreateDto)
        );
    }
}
