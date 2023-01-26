package study.datajpa.member.repository.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.datajpa.member.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final EntityManager em;

    public List<Member> findQueryMember() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
}