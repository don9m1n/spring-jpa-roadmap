package jpabook.jpashop.member.repository;

import jpabook.jpashop.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 클래스에서는 테스트 종료 후 데이터를 롤백해주는 역할을 한다.
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @Rollback(false)
    void testMember() throws Exception {
        Member member = Member.builder()
                .name("이강인")
                .build();

        Long saveId = memberRepository.save(member);

        Member findMember = memberRepository.find(saveId);
        findMember.changeName("손흥민");

        // Auditing을 통해 update된 시간은 바로 적용이 안됨..
        em.flush();
        em.clear();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getName()).isEqualTo(member.getName());
        assertThat(findMember).isEqualTo(member);
        assertThat(findMember.getUpdatedDate()).isAfter(findMember.getCreatedDate());
    }
}