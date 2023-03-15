package hello.springtx.propagation;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
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
        txManager.commit(s2);

        log.info("트랜잭션2 롤백 완료");
    }
}
