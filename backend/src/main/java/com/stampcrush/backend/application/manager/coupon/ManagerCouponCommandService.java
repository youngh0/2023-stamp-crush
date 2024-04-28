package com.stampcrush.backend.application.manager.coupon;

import com.stampcrush.backend.application.manager.coupon.dto.StampCreateDto;
import com.stampcrush.backend.application.manager.event.StampCreateEvent;
import com.stampcrush.backend.config.lock.NamedLock;
import com.stampcrush.backend.entity.cafe.Cafe;
import com.stampcrush.backend.entity.cafe.CafeCouponDesign;
import com.stampcrush.backend.entity.cafe.CafePolicy;
import com.stampcrush.backend.entity.coupon.Coupon;
import com.stampcrush.backend.entity.coupon.CouponStatus;
import com.stampcrush.backend.entity.user.Customer;
import com.stampcrush.backend.entity.user.Owner;
import com.stampcrush.backend.exception.CafeNotFoundException;
import com.stampcrush.backend.repository.cafe.CafeCouponDesignRepository;
import com.stampcrush.backend.repository.cafe.CafeRepository;
import com.stampcrush.backend.repository.coupon.CouponRepository;
import com.stampcrush.backend.repository.user.CustomerRepository;
import com.stampcrush.backend.repository.user.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Transactional()
@Service
public class ManagerCouponCommandService {

    private final CouponRepository couponRepository;
    private final CafeRepository cafeRepository;
    private final CustomerRepository customerRepository;
    private final CafeCouponDesignRepository cafeCouponDesignRepository;
    private final OwnerRepository ownerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Long createCoupon(Long ownerId, Long cafeId, Long customerId) {
        Cafe cafe = findCafeById(cafeId);
        Owner owner = findOwner(ownerId);

        cafe.validateOwnership(owner);

        Customer customer = findCustomer(customerId);
        CafePolicy cafePolicy = findCafePolicy(cafe);
        CafeCouponDesign cafeCouponDesign = findCafeCouponDesign(cafe);
        List<Coupon> existCoupons = couponRepository.findByCafeAndCustomerAndStatus(cafe, customer, CouponStatus.ACCUMULATING);
        expireExistCoupons(existCoupons);

        Coupon coupon = issueCoupon(customer, cafe, cafeCouponDesign, cafePolicy);
        couponRepository.save(coupon);
        return coupon.getId();
    }

    private void expireExistCoupons(List<Coupon> existCoupons) {
        if (!existCoupons.isEmpty()) {
            for (Coupon coupon : existCoupons) {
                coupon.expire();
            }
        }
    }

    private Cafe findCafeById(Long cafeId) {
        return cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CafeNotFoundException("존재하지 않는 카페입니다."));
    }

    private Coupon issueCoupon(Customer customer, Cafe cafe, CafeCouponDesign cafeCouponDesign, CafePolicy cafePolicy) {
        LocalDate expiredDate = LocalDate.now().plusMonths(cafePolicy.getExpirePeriod());
        return new Coupon(expiredDate, customer, cafe, cafeCouponDesign, cafePolicy);
    }

    @NamedLock(lockKey = "accumulateStamp", lockType = "couponId")
    public void createStamp(StampCreateDto stampCreateDto, Long couponId) {
        Customer customer = findCustomer(stampCreateDto.getCustomerId());
        Owner owner = findOwner(stampCreateDto.getOwnerId());
        Cafe cafe = findCafe(owner);
        Coupon coupon = findCoupon(couponId, customer, cafe);
        isCorrectStampCountInCoupon(stampCreateDto, coupon);

        int earningStampCount = stampCreateDto.getEarningStampCount();
        StampCreateEvent stampCreateEvent = StampCreateEventCommand.createEvent(coupon, earningStampCount);
        eventPublisher.publishEvent(stampCreateEvent);
        // 스탬프를 적립해도 쿠폰을 모두 채우지 않을 때
        if (coupon.isLessThanMaxStampAfterAccumulateStamp(earningStampCount)) {
            coupon.accumulate(earningStampCount);
            return;
        }
        // 스탬프 적립 시 쿠폰이 REWARD 상태로 바뀔 때
        if (coupon.isSameMaxStampAfterAccumulateStamp(earningStampCount)) {
            accumulateMaxStampAndMakeReward(coupon, earningStampCount);
            return;
        }

        // 적립하려는 스탬프 개수가 쿠폰의 남은 스탬프보다 많을 때
        accumulateStampOverCoupon(customer, cafe, coupon, earningStampCount);
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(IllegalArgumentException::new);
    }

    private Owner findOwner(Long ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(IllegalArgumentException::new);
    }

    private Cafe findCafe(Owner owner) {
        List<Cafe> cafes = cafeRepository.findAllByOwner(owner);
        if (cafes.isEmpty()) {
            throw new CafeNotFoundException("존재하지 않는 카페입니다.");
        }
        return cafes.stream()
                .findAny()
                .get();
    }

    private Coupon findCoupon(Long couponId, Customer customer, Cafe cafe) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(IllegalArgumentException::new);
        if (coupon.isNotAccessible(customer, cafe)) {
            throw new IllegalArgumentException();
        }
        return coupon;
    }

    private void isCorrectStampCountInCoupon(StampCreateDto stampCreateDto, Coupon coupon) {
        if (coupon.getStampCount() != stampCreateDto.getCurrentStampCount()) {
            throw new IllegalArgumentException("incorrect stamp count");
        }
    }

    private void accumulateMaxStampAndMakeReward(Coupon coupon, int earningStampCount) {
        coupon.accumulate(earningStampCount);
    }

    private void accumulateStampOverCoupon(Customer customer, Cafe cafe, Coupon coupon, int earningStampCount) {
        int restStampCountForReward = coupon.calculateRestStampCountForReward();
        accumulateMaxStampAndMakeReward(coupon, restStampCountForReward);

        earningStampCount -= restStampCountForReward;
        CafePolicy cafePolicy = findCafePolicy(cafe);
        CafeCouponDesign cafeCouponDesign = findCafeCouponDesign(cafe);
        makeRewardCoupons(customer, cafe, cafePolicy, cafeCouponDesign, earningStampCount);
        issueAccumulatingCoupon(customer, cafe, cafePolicy, cafeCouponDesign, earningStampCount);
    }

    private CafeCouponDesign findCafeCouponDesign(Cafe cafe) {
        return cafeCouponDesignRepository.findByCafeAndIsActivateTrue(cafe)
                .orElseThrow(IllegalArgumentException::new);
    }

    private CafePolicy findCafePolicy(Cafe cafe) {
        return cafe.getActiveCafePolicy();
    }

    private void makeRewardCoupons(Customer customer, Cafe cafe, CafePolicy cafePolicy, CafeCouponDesign cafeCouponDesign, int restStamp) {
        int rewardCouponCount = cafePolicy.calculateRewardCouponCount(restStamp);
        for (int i = 0; i < rewardCouponCount; i++) {
            Coupon rewardCoupon = issueCoupon(customer, cafe, cafeCouponDesign, cafePolicy);
            rewardCoupon.accumulateMaxStamp();
            couponRepository.save(rewardCoupon);
        }
    }

    private void issueAccumulatingCoupon(Customer customer, Cafe cafe, CafePolicy cafePolicy, CafeCouponDesign cafeCouponDesign, int earningStampCount) {
        int accumulatingStampCount = earningStampCount % cafePolicy.getMaxStampCount();
        if (accumulatingStampCount == 0) {
            return;
        }
        Coupon accumulatingCoupon = issueCoupon(customer, cafe, cafeCouponDesign, cafePolicy);
        couponRepository.save(accumulatingCoupon);
        accumulatingCoupon.accumulate(accumulatingStampCount);
    }
}
