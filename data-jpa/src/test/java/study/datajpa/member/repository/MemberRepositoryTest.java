package study.datajpa.member.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.member.entity.Member;

import java.util.List;

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

    @Test
    void crud() throws Exception {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    void memberByUsernameAndAge() throws Exception {
        Member member1 = new Member("유재석", 52);
        Member member2 = new Member("유재석", 38);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("유재석", 40);
        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getAge()).isEqualTo(52);
        assertThat(members.get(0)).isEqualTo(member1);
    }

    @Test
    void queryTest1() throws Exception {
        Member member1 = new Member("유재석", 40);
        Member member2 = new Member("유재석", 38);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findMember("유재석", 40);
        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getAge()).isEqualTo(40);
        assertThat(members.get(0)).isEqualTo(member1);
    }

    @Test
    void queryTest2() throws Exception {
        Member member1 = new Member("유재석", 40);
        Member member2 = new Member("유재석", 38);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findMember("유재석");
        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    void queryTest3() throws Exception {
        Member member1 = new Member("유재석", 40);
        Member member2 = new Member("전소민", 38);
        Member member3 = new Member("박은빈", 32);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<String> names = memberRepository.findUsernameList();
        assertThat(names.size()).isEqualTo(3);
        assertThat(names.get(0)).isEqualTo("유재석");
        assertThat(names.get(1)).isEqualTo("전소민");
        assertThat(names.get(2)).isEqualTo("박은빈");
    }
}
