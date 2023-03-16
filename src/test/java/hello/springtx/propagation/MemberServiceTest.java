package hello.springtx.propagation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}