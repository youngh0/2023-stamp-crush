package com.stampcrush.backend.application.manager.event;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import com.stampcrush.backend.entity.cafe.Cafe;
import com.stampcrush.backend.entity.eventoutbox.StampAccumulateEventOutbox;
import com.stampcrush.backend.entity.user.Customer;
import com.stampcrush.backend.entity.visithistory.VisitHistory;
import com.stampcrush.backend.exception.CafeNotFoundException;
import com.stampcrush.backend.exception.CustomerNotFoundException;
import com.stampcrush.backend.exception.NotFoundException;
import com.stampcrush.backend.exception.StampCrushException;
import com.stampcrush.backend.repository.cafe.CafeRepository;
import com.stampcrush.backend.repository.eventoutbox.StampAccumulateEventOutboxRepository;
import com.stampcrush.backend.repository.user.CustomerRepository;
import com.stampcrush.backend.repository.visithistory.VisitHistoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.slack.api.webhook.WebhookPayloads.payload;

@RequiredArgsConstructor
@Component
public class StampAccumulatingEventHandler {

    @Value("${slack.webhook.url}")
    private String webHookUrl;

    private final Slack slackClient = Slack.getInstance();

    private final CafeRepository cafeRepository;

    @TransactionalEventListener
    public void process(StampCreateEvent stampCreateEvent) {
        Cafe cafe = findCafe(stampCreateEvent.getCafeId());
        String userPhone = cafe.getTelephoneNumber();
        int stampCount = stampCreateEvent.getStampCount();
        String cafeName = cafe.getName();
        try {
            slackClient.send(webHookUrl, payload(p -> p
                    .text("스탬프 적립 발생")
                    .attachments(List.of(createStampInfo(cafeName, userPhone, stampCount)))
            ));
        } catch (IOException e) {
            throw new StampCrushException(String.format("%s 번호 스탬프 %d개 적립 알림 전송 실패", userPhone, stampCount), e);
        }
    }

    private Cafe findCafe(Long cafeId) {
        return cafeRepository.findById(cafeId)
                .orElseThrow(() -> new CafeNotFoundException("존재하지 않는 카페입니다."));
    }

    private Attachment createStampInfo(String cafeName, String phone, int stampCount) {
        String requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(LocalDateTime.now());
        return Attachment.builder()
                .title("스탬프 적립 시간" + requestTime)
                .fields(List.of(
                                generateField("카페 명", cafeName),
                                generateField("유저 핸드폰", phone),
                                generateField("적립 스탬프", String.valueOf(stampCount))
                        )
                )
                .build();
    }

    private Field generateField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(true)
                .build();
    }
}
