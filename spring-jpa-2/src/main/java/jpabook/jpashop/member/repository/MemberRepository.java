package jpabook.jpashop.member.repository;

import jpabook.jpashop.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

// @Repostiry 안의 @Component로 인해 컴포넌트 스캔에 의해 스프링 빈으로 등록
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

    public Long count(String name) {
        return em.createQuery("select count(m) from Member m where m.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    public Member findOne(Long memberId) {
        return em.find(Member.class, memberId);
    }
}
