package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory query;

    // 각 테스트 실행 전에 실행되는 메서드
    @BeforeEach
    public void before() {
        query = new JPAQueryFactory(em);
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
    @DisplayName("member1을 찾아라!(JPQL)")
    public void startJPQL() throws Exception {
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
        assertThat(findMember.getTeam().getName()).isEqualTo("teamA");
    }

    @Test
    @DisplayName("member1을 찾아라!(Querydsl)")
    public void startQuerydsl() throws Exception {

        Member findMember = query
                .selectFrom(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
        assertThat(findMember.getTeam().getName()).isEqualTo("teamA");
    }

    @Test
    public void searchV1() {
        Member findMember = query
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                        .and(member.age.eq(10))
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchV2() {
        Member findMember = query
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {

        List<Member> members = query
                .selectFrom(member)
                .fetch();

        Member member1 = query
                .selectFrom(QMember.member)
                .fetchFirst();

        // limit(1).fetchOne() == fetchFirst()
        Member member2 = query
                .selectFrom(QMember.member)
                .limit(1)
                .fetchOne();


        // fetchResults()는 deprecated..
        QueryResults<Member> results = query
                .selectFrom(member)
                .fetchResults();

        // fetchCount()도 deprecated..
        long count1 = query
                .selectFrom(member)
                .fetchCount();

        // fetchCount() 대신에 이렇게 쓰자!
        Long count2 = query
                .select(member.count())
                .from(member)
                .fetchOne();
    }

    @Test
    public void sort() {
        /**         * 회원 정렬 순서
         * 1. 나이 내림차순 (desc)
         * 2. 이름 오름차순 (asc)
         * 이름이 없는 경우 가장 마지막에 출력

         */
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();


        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = query
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("count쿼리가 필요하면 따로 작성해서 사용하자")
    public void paging2() {
        List<Member> result = query
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        Long count = query
                .select(member.count())
                .from(member)
                .fetchOne();

        assertThat(result.size()).isEqualTo(2);
        assertThat(count).isEqualTo(4L);
    }

    @Test
    public void aggregation() throws Exception {
        // Tuple은 결과 데이터의 타입이 여러 개인 경우에 사용되는 타입
        Tuple tuple = query
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetchOne();

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     */
    @Test
    public void group() throws Exception {

        List<Tuple> result = query
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(team.name.contains("B")) // 팀 이름에 B가 포함된 팀만 필터링
                .fetch();

        Tuple teamB = result.get(0);

        assertThat(result.size()).isEqualTo(1);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
        List<Member> result = query
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = query
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result.size()).isEqualTo(2);

    }

    /**
     * ex) 회원, 팀을 조인할 때, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: select m.*, t.* from Member m LEFT JOIN Team t on m.team_id = t.id and t.name = 'teamA';
     */
    @Test
    public void join_on_filtering() throws Exception {

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

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * ex) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: select m, t from Member m LEFT JOIN Team t on m.username = t.name
     * SQL: select m.*, t.* from Member m LEFT JOIN Team t on m.username = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = query
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
    
}
