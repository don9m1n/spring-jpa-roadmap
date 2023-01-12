package study.datajpa.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.member.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // public abstract 생략
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

}
