package jpabook.jpashop.member.service;

import jpabook.jpashop.base.data.Address;
import jpabook.jpashop.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("회원 가입 테스트")
    void joinTest() throws Exception {
        Member member = Member.builder()
                .name("동민")
                .build();

        Long saveId = memberService.join(member);
        Member findMember = memberService.findOne(saveId);

        assertThat(findMember).isEqualTo(member);
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getName()).isEqualTo(member.getName());
    }

    @Test
    @DisplayName("중복 회원 테스트")
    void validateTest() throws Exception {
        Member member1 = new Member("member",
                new Address("서울", "동대문구", "망우로20길"));
        Member member2 = new Member("member",
                new Address("서울", "동대문구", "망우로30길"));

        memberService.join(member1);
        assertThrows(IllegalStateException.class, () -> {
            memberService.join(member2);
        }, "해당 예외가 발생하지 않았습니다! 예외 타입을 바꿔주세요!");
    }

}