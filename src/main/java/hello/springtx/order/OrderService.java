package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("오더 호출");
        repository.save(order);

        log.info("결제 진입");
        if (order.getUsername().equals("예외")) {
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외");
        }
        else if (order.getUsername().equals("잔고 부족")) {
            log.info("비즈니스 예외 발생 - 비즈니스");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고가 부족합니다.");
        }
        else {
            log.info("정상 승인");
            order.setPayStatus("완료");
        }
        log.info( "결제 프로세스 완료");
    }
}
