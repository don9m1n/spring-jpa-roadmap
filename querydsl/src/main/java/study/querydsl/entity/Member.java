package study.querydsl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = PROTECTED) // protected 접근 제어자를 가진 기본 생성자 생성
@ToString(of = {"id", "username", "age"}) // 연관관계가 걸린 필드는 제외하고 설정 -> 무한 참조가 일어날 수 있기 때문에 위험
public class Member {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id") // 단순하게 컬럼명을 지정해주는 어노테이션
    private Team team;

    public Member(String username) {
        this(username, 0);
    }

    public Member(String username, int age) {
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
