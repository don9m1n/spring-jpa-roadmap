package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    @PersistenceUnit
    EntityManagerFactory emf;

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

    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();

        // Fetch Join으로 회원 필드와 팀 필드를 한 번에 끌어옴
        Member findMember = query
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();


        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();

    }

    /**
     * 나이가 가장 많은 회원 조회
     * Querydsl SubQuery => JPAExpressions
     */
    @Test
    public void subQuery() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max()).from(memberSub)
                )).fetch();

        assertThat(result)
                .extracting("age") // extracting: 추출
                .containsExactly(40);
    }

    /**
     * 나이가 평균 나이 이상인 회원
     */
    @Test
    public void subQueryGoe() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg()).from(memberSub)
                )).fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(30, 40);
    }
    
    @Test
    public void selectSubQuery() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = query.select(member.username, select(memberSub.age.avg()).from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " + tuple.get(select(memberSub.age.avg()).from(memberSub)));
        }
    }

    @Test
    public void simpleCase() throws Exception {
        List<String> result = query.select(member.age
                .when(10).then("열살")
                .when(20).then("스무살")
                .otherwise("기타"))
                .from(member)
                .fetch();

        for (String r : result) {
            System.out.println(r);
        }
    }
    
    @Test
    public void complexCase() throws Exception {
        List<String> result = query.select(new CaseBuilder()
                .when(member.age.between(10, 20)).then("0~20살")
                .when(member.age.between(21, 30)).then("21~30살")
                .otherwise("기타"))
                .from(member)
                .fetch();

        for (String r : result) {
            System.out.println(r);
        }
    }
    
    @Test
    public void constant() throws Exception {
        Tuple result = query.select(member.username, Expressions.constant("EUNBIN"))
                .from(member)
                .fetchFirst();
        System.out.println("result = " + result);
    }

    @Test
    public void concat() throws Exception {
        String member1 = query.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("member1 = " + member1);
    }

    @Test
    public void tupleProjection() throws Exception {
        List<Tuple> result = query
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }
    
    @Test
    @DisplayName("순수 JPA에서 DTO 조회")
    public void findDtoByJPQL() throws Exception {
        List<MemberDto> result = em.createQuery(
                "select new study.querydsl.dto.MemberDto(m.username, m.age) " +
                        "from Member m", MemberDto.class)
                .getResultList();
    }

    @Test
    @DisplayName("프로퍼티에 접근해서 DTO 값 채우기")
    public void findDtoByBean() throws Exception {
        // setter가 필수적으로 필요한 방법!!
        List<MemberDto> result = query
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto member : result) {
            System.out.println("이름 : " + member.getUsername());
            System.out.println("나이 : " + member.getAge());
        }
    }

    @Test
    @DisplayName("필드에 접근해서 DTO 값 채우기")
    public void findDtoByFields() throws Exception {
        List<MemberDto> result = query
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto member : result) {
            System.out.println("이름 : " + member.getUsername());
            System.out.println("나이 : " + member.getAge());
        }
    }

    @Test
    @DisplayName("생성자에 접근해서 DTO 값 채우기")
    public void findDtoByConstructor() throws Exception {
        List<MemberDto> result = query
                .select(Projections.constructor(MemberDto.class,
                        member.username, // 파라미터 순서 잘 지켜서 넣어야 함!
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto member : result) {
            System.out.println("이름 : " + member.getUsername());
            System.out.println("나이 : " + member.getAge());
        }
    }
    
    @Test
    public void dtoProjection() throws Exception {
        List<MemberDto> members = query.select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto member : members) {
            System.out.println(member.getUsername());
            System.out.println(member.getAge());
        }
    }
    
    @Test
    public void dynamicQuery_BooleanBuilder() throws Exception {
        String username = "member1";
        Integer age = 10;

        List<Member> result = searchMember1(username, age);
        assertThat(result.size()).isEqualTo(4);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            builder.or(member.age.gt(ageCond));
        }

        return query
                .selectFrom(member)
                .where(builder)
                .fetch();


    }
}