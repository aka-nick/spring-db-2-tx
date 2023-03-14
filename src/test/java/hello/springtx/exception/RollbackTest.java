package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예외 발생 시 트랜잭션 AOP는 예외 종류에 따라 트랜잭션을 커밋(체크)하거나 롤백(언체크)한다.
 */
@SpringBootTest
@Slf4j
public class RollbackTest {

     @Autowired
     RollbackService service;

     @Test
     void runtime() {
          service.runtimeException();
     }

     @Test
     void checked() throws MyException {
          service.checkedException();
     }

     @Test
     void rollbackFor() throws MyException {
          service.rollbackFor();
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
