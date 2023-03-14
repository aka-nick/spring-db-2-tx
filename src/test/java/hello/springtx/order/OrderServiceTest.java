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
            service.order(order); /** 비즈니스 처리 예외는 try-catch로 잡아서,  */
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족 사실과 입금 계좌 번호 알림"); /** catch절에서 고객에게 다른 방식을 시도할 수 있도록 안내하는 등, 비즈니스 상황에 맞는 처리를 한다 */
        }
        /** 꼭 예외를 던져서 지금처럼 할 필요는 없고, Enum과 같은 것으로 비즈니스 예외 상황을 잘 정의하고 리턴값을 넘겨받아 검증하는 식으로 구현할 수도 있다 */


        assertThat(repository.findById(order.getId()).get().getPayStatus()).isEqualTo("대기");
    }
}
