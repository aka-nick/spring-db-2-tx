package hello.springtx.order;


import static org.assertj.core.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    OrderService service;

    @Autowired
    OrderRepository repository;

    @Test
    void order() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("정상");

        service.order(order);

        Order find = repository.findById(order.getId()).get();
        assertThat(find.getPayStatus()).isEqualTo("완료");
    }

    @Test
    void orderUnchecked() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("예외");

        assertThatThrownBy(() ->service.order(order))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void orderBusinessException() {
        Order order = new Order();
        order.setUsername("잔고 부족");

        try {
            service.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족 사실과 입금 계좌 번호 알림");
        }
        assertThat(repository.findById(order.getId()).get().getPayStatus()).isEqualTo("대기");
    }
}
