package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "team_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
