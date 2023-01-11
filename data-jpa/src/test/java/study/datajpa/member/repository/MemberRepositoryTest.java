package study.datajpa.member.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.member.entity.Member;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Rollback(false)
    void entityTest() throws Exception {
        Member member = new Member("user1");
        Member saveMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(member.getId()).isEqualTo(1);
        assertThat(member.getUsername()).isEqualTo("user1");
        assertThat(findMember).isEqualTo(member);
    }
}
