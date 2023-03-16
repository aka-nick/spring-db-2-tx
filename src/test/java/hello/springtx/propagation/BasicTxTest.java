package hello.springtx.propagation;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    // 커밋, 롤백
    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus s = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(s);

        log.info("트랜잭션 커밋 완료");
    }
    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus s = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(s);

        log.info("트랜잭션 롤백 완료");
    }

    // 트랜잭션 두 번 사용
    /*
    커넥션풀을 사용하고 있기 때문에 같은 커넥션을 사용한다.
    하지만 한번 커밋을 했기 때문에 첫번째 커넥션은 반납이 되고, 두번째 나오는 커넥션은 다른 커넥션이다.
    다시 말해, 내부의 물리적인 커넥션은 재사용됐지만, 커넥션풀에서 제공한 커넥션 자체는 별도의 새로운 커넥션이다.

    한 트랜잭션은 하나의 커넥션 객체를 사용하고
    두 개의 트랜잭션은 두 개의 커넥션 객체가 사용되나, 커넥션 객체 내부의 실제커넥션 자체는 같을 수도 있다.
     */
    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus s1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        txManager.commit(s1);

        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus s2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 커밋 시작");
        txManager.commit(s2);

        log.info("트랜잭션2 커밋 완료");

    }
    /*
    커밋과 롤백을 연이어 수행하더라도 별개의 트랜잭션이라면 결국 위와 비슷하게 동작한다.
     */
    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus s1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        txManager.commit(s1);

        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus s2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 롤백 시작");
        txManager.rollback(s2);

        log.info("트랜잭션2 롤백 완료");
    }

    // 트랜잭션 전파(스프링이 제공하는 트랜잭션 전파 기능)
    /*
    외부 트랜잭션이 수행 중인데, 내부 트랜잭션이 추가로 수행됨
    - 하나의 트랜잭션이 끝나지 않았는데 또 다른 트랜잭션을 수행하면
    - 스프링은 하나의 트랜잭션(이전에 만들어진 트랜잭션)에 뒤늦게 수행된 트랜잭션을 참여(하나의 커넥션풀 커넥션 객체로 수행)시켜서 하나로 수행시킨다.
    - 이를 위해 필요한 개념은 물리트랜잭션/논리트랜잭션이다.
        - 물리 트랜잭션은 실제 데이터베이스에 적용되는 트랜잭션을 뜻한다.
        - 논리 트랜잭션은 트랜잭션 매니저를 통해 사용하는 트랜잭션을 뜻한다. 트랜잭션이 추가되는 경우에만 논리 트랜잭션을 따지는 경우가 발생한다.
    - 왜 트랜잭션 개념을 두 개로 나눠야 할까?
        - 트랜잭션이 중첩될 때 커밋/롤백 경우의 수가 너무 많아진다.
        - 원칙을 세우기 위해 필요하다.
    - 원칙
        - 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
        - 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다.
        - All or Nothing.
     */
    @Test
    void inner_commit() {
        log.info("트랜잭션 outer 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("isNewTransaction() = {}", outer.isNewTransaction());

            // 내부 트랜잭션 시작 (외부와 내부를 쉽게 파악하기 위해 인덴트 차이를 둔다)
            log.info("트랜잭션 inner 시작");
            TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
            log.info("isNewTransaction() = {}", inner.isNewTransaction()); // 기존 트랜잭션에 참여되기 때문에 새 트랜잭션이 아니라고 나온다
            log.info("트랜잭션 inner 커밋 시작");
            txManager.commit(inner); // 내부 트랜잭션은 커밋을 시도해도 커밋이 안된다.
            log.info("트랜잭션 inner 커밋 완료");

        log.info("트랜잭션 outer 커밋 시작");
        txManager.commit(outer); // 실제로는 여기서'만' 커밋이 된다!
        log.info("트랜잭션 outer 커밋 완료");
    }

    // 트랜잭션 전파 - 외부 롤백
    @Test
    void outer_rollback() {
        log.info("트랜잭션 outer 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 inner 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션 inner 커밋 시작");
        txManager.commit(inner);
        log.info("트랜잭션 inner 커밋 완료");

        log.info("트랜잭션 outer 롤백 시작");
        txManager.rollback(outer);
        log.info("트랜잭션 outer 롤백 완료");
    }

    // 트랜잭션 전파 - 내부 롤백
    /*
    transactionStatus 안에는 'rollback-only'라는 플래그가 있다.
    여러 트랜잭션이 중첩되더라도 하나의 논리트랜잭션이 롤백되는 순간 공유되는 전체(물리)트랜잭션의 rollback-only 플래그는 true가 된다.
    rollback-only true의 의미는 롤백만 가능하다(커밋은 불가능하다)는 의미이다.
    이를 통해서 스프링은 All or Nothing의 커밋/롤백 전략을 수행한다.
     */
    @Test
    void inner_rollback() {
        log.info("트랜잭션 outer 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 inner 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션 inner 롤백 시작");
        txManager.rollback(inner);
        log.info("트랜잭션 inner 롤백 완료");

        log.info("트랜잭션 outer 커밋 시작");
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
        log.info("트랜잭션 outer 커밋 완료");
    }
}
