package study.datajpa.member.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.member.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void entityTest() throws Exception {
        Member member = new Member("user1");
        Member saveMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(member.getId());

        assertThat(member.getId()).isEqualTo(1);
        assertThat(member.getUsername()).isEqualTo("user1");
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void crud() throws Exception {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deleteCount = memberJpaRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    void memberByUsernameAndAge() throws Exception {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<Member> members = memberJpaRepository.findByUsernameAndAgeGreaterThan("member1", 20);

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getAge()).isEqualTo(20);
        assertThat(members.get(0)).isEqualTo(member2);
    }

    @Test
    void returnTypeTest1() throws Exception {
        Member member = new Member("AAA");
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findMemberByUsername("asdad");
        System.out.println("member = " + findMember);
    }

    @Test
    void returnTypeTest2() throws Exception {
        Member member = new Member("AAA");
        memberJpaRepository.save(member);

        List<Member> members = memberJpaRepository.findListByUsername("asdad");
        System.out.println("members = " + members);
    }

    @Test
    @Rollback(false)
    void paging() throws Exception {
        for (int i = 1; i <= 10; i++) {
            memberJpaRepository.save(new Member("member" + i, 20));
        }

        List<Member> memberByPaging = memberJpaRepository.findListByPaging(20, 0, 5);
        long count = memberJpaRepository.count(20);

        assertThat(memberByPaging.size()).isEqualTo(5);
        assertThat(count).isEqualTo(10);
    }

    @Test
    @Rollback(false)
    void bulkUpdate() throws Exception {
        for (int i = 1; i <= 10; i++) {
            memberJpaRepository.save(new Member("member" + i, i * 10));
        }

        int row = memberJpaRepository.bulkAgePlus(50); // 수정!!

        List<Member> members = memberJpaRepository.findAll();
        assertThat(row).isEqualTo(6);
    }

    @Test
    void JpaAuditingTest() throws Exception {

        Member member = memberJpaRepository.save(new Member("member1", 10)); // @PrePersist

        Thread.sleep(1000);
        member.changeName("동민");

        em.flush(); // @PreUpdate
        em.clear();

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        System.out.println("생성일 : " + findMember.getCreatedDate());
        System.out.println("수정일 : " + findMember.getLastModifiedDate());
    }

}