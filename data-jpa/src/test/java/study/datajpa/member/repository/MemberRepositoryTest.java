package study.datajpa.member.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;
import study.datajpa.member.entity.Member;
import study.datajpa.member.entity.MemberDto;
import study.datajpa.team.entity.Team;
import study.datajpa.team.repository.TeamRepository;

import javax.print.attribute.standard.PageRanges;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

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

    @Test
    void queryTest4() throws Exception {
        Team teamA = new Team("T1");
        Team teamB = new Team("DK");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("유재석", 40, teamA);
        Member member2 = new Member("전소민", 38, teamB);
        Member member3 = new Member("박은빈", 32, teamA);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<MemberDto> memberDtos = memberRepository.findMemberDto();

        assertThat(memberDtos.size()).isEqualTo(3);
        assertThat(memberDtos.get(0).getTeamName()).isEqualTo("T1");
        assertThat(memberDtos.get(1).getTeamName()).isEqualTo("DK");
        assertThat(memberDtos.get(2).getTeamName()).isEqualTo("T1");
    }

    @Test
    void collectionBindingTest() throws Exception {
        Member member1 = new Member("김영한");
        Member member2 = new Member("박은빈");
        Member member3 = new Member("유재석");
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> members = memberRepository.findByNames(Arrays.asList("박은빈", "유재석", "김영한"));
        assertThat(members.size()).isEqualTo(3);
    }

    @Test
    void returnTypeTest() throws Exception {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 10);
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member member = memberRepository.findMemberByUsername("AAA");
        System.out.println(member);
    }

    @Test
    @Rollback(false)
    void paging() throws Exception {
        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("member" + i, i * 10));
        }

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));
        Slice<Member> page = memberRepository.findByAgeGreaterThanEqual(30, pageRequest);

        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);
        //assertThat(page.getTotalPages()).isEqualTo(1); // 총 페이지 개수
        //assertThat(page.getTotalElements()).isEqualTo(3); // 전체 데이터의 개수
        assertThat(page.getNumber()).isEqualTo(0); // 현재 페이지 번호 (시작값 0)
        assertThat(page.isFirst()).isTrue(); // 첫 번째 페이지인가
        assertThat(page.isLast()).isTrue(); // 마지막 페이지인가
        assertThat(page.hasNext()).isFalse(); // 다음 페이지가 있는가
    }

    @Test
    void paging2() throws Exception {
        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("member" + i, i * 10));
        }

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));
        Page<Member> byAge = memberRepository.findBy(pageRequest);
    }

    @Test
    void paging3() throws Exception {
        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("member" + i, i * 10));
        }

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAgeGreaterThanEqual(30, pageRequest);

        Page<MemberDto> map = page.map(m -> new MemberDto(m));
    }

    @Test
    @Rollback(false)
    void bulkUpdate() throws Exception {
        for (int i = 1; i <= 10; i++) {
            memberRepository.save(new Member("member" + i, i * 10));
        }

        int row = memberRepository.bulkUpdate(60);

        System.out.println(memberRepository.findAll());
        assertThat(row).isEqualTo(5);
    }
}
