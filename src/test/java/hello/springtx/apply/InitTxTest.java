package hello.springtx.apply;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
@Slf4j
public class InitTxTest {

    @Autowired
    Hello hello;

    @Test
    void go() {
        // 초기화 코드는 빈 생성-등록 시 알아서 호출된다
//        hello.initV1(); // 직접 호출하면 트랜잭션은 적용된다.
    }

    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    static class Hello {
        @PostConstruct // 참고: @PostConstruct는 '빈이 생성된 후'를 말한다.
        @Transactional // 초기화 코드(@PostConstruct)가 먼저 실행되고 난 다음에 트랜잭션 AOP가 호출되기 때문에, 트랜잭션 적용이 안된다.
        public void initV1() {
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("hello init v1 active={}", active);
        }

        @EventListener(ApplicationReadyEvent.class) // ApplicationReadyEvent : 스프링 컨테이너 완성 시점의 이벤트
        @Transactional
        public void initV2() {
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("hello init v2 active={}", active);
        }
    }
}
