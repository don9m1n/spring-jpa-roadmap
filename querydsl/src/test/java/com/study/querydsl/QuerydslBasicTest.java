package com.study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.QTeam;
import com.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.study.querydsl.entity.QMember.member;
import static com.study.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public QuerydslBasicTest(@Autowired EntityManager em) {
        this.em = em;
        query = new JPAQueryFactory(em);
    }

    @BeforeEach
    public void before() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        // member1을 찾아라.
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
        Member findMember = query
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        Member findMember = query
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                        .and(member.age.between(10, 30))
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    void searchAndParam() {
        Member findMember = query
                .selectFrom(member)
                .where( // 중간에 null이 들어가면 무시함. -> 동적 쿼리 작성 굿
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    void resultFetch() {
        List<Member> fetch = query
                .selectFrom(member)
                .fetch();

        Member fetchOne = query
                .selectFrom(member)
                .fetchOne();

        // limit 1 fetchOne
        Member fetchFirst = query
                .selectFrom(member)
                .fetchFirst();

        // deprecated!
        QueryResults<Member> results = query
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();
    }

    /**
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 올림차순
     * 단 2에서 회원 이름이 없으면 마지막에 출력
     */
    @Test
    void sort() {

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = query
                .selectFrom(QMember.member)
                .where(member.age.eq(100))
                .orderBy(
                        QMember.member.age.desc(),
                        QMember.member.username.asc().nullsLast()
                )
                .fetch();

        Member member5 = members.get(0);
        Member member6 = members.get(1);
        Member memberNull = members.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging1() {
        List<Member> members = query
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 2번째 부터
                .limit(2) // 2건 조회
                .fetch();

        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    void paging2() {
        List<Member> members = query
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 2번째 부터
                .limit(2) // 2건 조회
                .fetch();

        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    void aggregation() {
        List<Tuple> result = query
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void group1() {
        // 팀의 이름과 각 팀의 평균 연령을 구해라.
        /**
         * select t.name, avg(m.age)
         * from member m
         * inner join team t
         * on (m.team_id = t.id)
         * group by t.name;
         */
        List<Tuple> result = query
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void group2() {
        // 팀의 이름과 각 팀의 평균 연령을 구해라. (teamB만)
        /**
         * select t.name, avg(m.age)
         * from member m
         * inner join team t
         * on (m.team_id = t.id)
         * group by t.name
         */
        List<Tuple> result = query
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(team.name.eq("teamB"))
                .fetch();

        Tuple teamB = result.get(0);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void group3() {
        /**
         * select t.name, avg(m.age)
         * from member m
         * inner join team t
         * on (m.team_id = t.id)
         * group by t.name
         */
        List<Tuple> result = query
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.age.avg().gt(30))
                .fetch();

        Tuple teamB = result.get(0);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void join() {
        /**
         * 팀A에 소속된 모든 회원
         */
        List<Member> members = query
                .selectFrom(member)
                .join(member.team, team) // [조인 대상, 별칭으로 사용할 QClass]
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(members.size()).isEqualTo(2);
        assertThat(members)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = query
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }

    @Test
    void join_on_filtering() {
        /**
         * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
         */
        List<Tuple> result = query
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = query
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = query
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    @Test
    void subQuery() {
        /**
         * 나이가 가장 많은 회원 조회
         */

        QMember sub = new QMember("sub");

        Member findMember = query
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(sub.age.max())
                                .from(sub)
                ))
                .fetchOne();

        assertThat(findMember.getAge()).isEqualTo(40);
    }

    @Test
    void subQueryGoe() {
        /**
         * 나이가 평균 나이 이상인 회원
         * select *
         * from member
         * where member.age >= (select avg(sub.age)
         *                      from member sub)
         */

        QMember sub = new QMember("sub");

        List<Member> members = query
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(sub.age.avg())
                                .from(sub)
                ))
                .fetch();

        assertThat(members.size()).isEqualTo(2);
        assertThat(members)
                .extracting("username")
                .containsExactly("member3", "member4");
    }

    @Test
    void subQueryIn() {
        /**
         * 10살 이상인 회원들의 모든 정보 조회
         */

        QMember sub = new QMember("sub");

        List<Member> members = query
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(sub.age)
                                .from(sub)
                                .where(sub.age.gt(10))
                ))
                .fetch();

        assertThat(members.size()).isEqualTo(3);
        assertThat(members)
                .extracting("username")
                .containsExactly("member2", "member3", "member4");
    }

    @Test
    void subQuerySelect() {

        QMember sub = new QMember("sub");

        List<Tuple> result = query
                .select(member.username,
                        JPAExpressions
                                .select(sub.age.avg())
                                .from(sub)
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        System.out.println(tuple.get(JPAExpressions.select(sub.age.avg()).from(sub)));
    }

    @Test
    void baseCase() {
        /**
         * 10 -> 열살
         * 20 -> 스무살
         * 나머지 -> 기타
         */
        List<String> result = query
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void complexCase() {
        List<String> members = query
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String m : members) {
            System.out.println("m = " + m);
        }

    }
}
