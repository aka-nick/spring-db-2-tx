package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    // 각각의 트랜잭션을 사용하는 예제
    public void joinV1(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("=== memberRepository 호출 시작 ===");
        memberRepository.save(member);
        log.info("=== memberRepository 호출 종료 ===");
        log.info("=== logRepository 시작 ===");
        logRepository.save(logMessage);
        log.info("=== logRepository 종료 ===");
    }

    // 로그 저장 중 예외가 발생했을 때 회원 가입에 영향을 주지 않도록, 로그에서 발생하는 예외는 잡기로 하는 시나리오
    public void joinV2(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("=== memberRepository 호출 시작 ===");
        memberRepository.save(member);
        log.info("=== memberRepository 호출 종료 ===");
        log.info("=== logRepository 시작 ===");
        try {
            logRepository.save(logMessage);
        }
        catch (RuntimeException e) {
            log.info("로그 저장에 실패했습니다. logMesage = {}", logMessage);
            log.info("정상 흐름 반환");
        }
        log.info("=== logRepository 종료 ===");
    }

}
