package study.datajpa.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> { }
