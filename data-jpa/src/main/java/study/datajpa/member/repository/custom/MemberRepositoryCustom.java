package study.datajpa.member.repository.custom;

import study.datajpa.member.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
