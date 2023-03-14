package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예외 발생 시 트랜잭션 AOP는 예외 종류에 따라 트랜잭션을 커밋(체크)하거나 롤백(언체크)한다.
 *
 * 왜냐면,
 * 체크 예외는 비즈니스적 의미가 있는 예외 / 언체크(런타임) 예외는 복구 불가능한 시스템 상의 예외라고 기본적으로 판단한다
 * (물론 기본 전략일 뿐이고, 비즈니스에 따라 적절하게 판단해서 처리할 필요가 있다.)
 */
@SpringBootTest
@Slf4j
public class RollbackTest {

     @Autowired
     RollbackService service;

     @Test
     void runtime() {
          Assertions.assertThatThrownBy(() -> service.runtimeException())
                  .isInstanceOf(RuntimeException.class);
     }

     @Test
     void checked() throws MyException {
          Assertions.assertThatThrownBy(() -> service.checkedException())
                  .isInstanceOf(MyException.class);
     }

     @Test
     void rollbackFor() throws MyException {
          Assertions.assertThatThrownBy(() -> service.rollbackFor())
                  .isInstanceOf(MyException.class);
     }


     @TestConfiguration
     static class RollbackServiceConfig {
          @Bean
          RollbackService rollbackService() {
               return new RollbackService();
          }
     }

     static class RollbackService {
          // 런타임 예외 발생:롤백
          @Transactional
          public void runtimeException() {
               log.info("call runtime");
               throw new RuntimeException();
          }

          // 체크 예외 발생:커밋
          @Transactional
          public void checkedException() throws MyException {
               log.info("체크드");
               throw new MyException();
          }

          // 체크 예외 발생:강제 롤백(rollbackFor)
          @Transactional(rollbackFor = MyException.class)
          public void rollbackFor() throws MyException {
               log.info("롤백 포");
               throw new MyException();
          }
     }

     static class MyException extends Exception{

     }

}
