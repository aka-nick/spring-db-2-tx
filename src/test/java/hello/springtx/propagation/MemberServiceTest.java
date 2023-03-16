package hello.springtx.propagation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * MemberService        @Transactional: OFF
     * MemberRepository     @Transactional: ON
     * LogRepository        @Transactional: ON
     */
    @Test
    void outerTxOff_success() {
        String username = "outerTxOff_success";

        memberService.joinV1(username);

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * MemberService    @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON Exception
     */
    @Test
    void outerTxOff_fail() {
        String username = "outerTxOff_fail예외";

        assertThrows(RuntimeException.class, () -> memberService.joinV1(username));
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:OFF
     * LogRepository    @Transactional:OFF
     */
    // 각각의 리포지토리에 적용되어있는 별개의 트랜잭션을 묶는 가장 간단한 방법은, 두 리포지토리를 사용하는 MemberService에 트랜잭션을 적용하는 것이다.
    @Test
    void singleTx() {
        String username = "singleTx";

        memberService.joinV1(username); //서비스에만 트랜잭션이 있는 상태

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void outerTxOn_success() {
        String username = "outerTxOn_success";

        memberService.joinV1(username); //서비스에만 트랜잭션이 있는 상태

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON Exception
     */
    @Test
    void outerTxOn_fail() {
        String username = "outerTxOn_fail예외";

        // 예외가 발생하면
        assertThrows(RuntimeException.class, () -> memberService.joinV1(username));
        // 모든 데이터가 롤백된다
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
        /*
        논리트랜잭션은 별개지만
        하나의 물리트랜잭션으로 All or Nothing 하기로 했기 때문에
        결국 하나의 논리트랜잭션만 실패해도 전부 롤백된다
         */
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON Exception
     */
    @Test
    void recoverException_fail() {
        String username = "recoverException_faild예외";

        assertThrows(UnexpectedRollbackException.class, () -> memberService.joinV2(username));
        assertTrue(memberRepository.find(username).isPresent()); //...를 기대하지만... 전체 롤백으로 인해 member도 등록되지 않는다.
        assertTrue(logRepository.find(username).isEmpty());

        // 예외가 발생한 논리트랜잭션에서 이미 rollbackOnly=true로 마크하기 때문이다.
        // 그래서 내부 논리 트랜잭션에서는 롤백을 하고, 외부에서는 커밋을 시도하는데(여기(서비스)서 예외를 정상흐름으로 돌려놓는 로직이 있으니까)
        // 그러면 물리 트랜잭션은 UnexpectedRollbackException을 발생시킨다.
    }

}