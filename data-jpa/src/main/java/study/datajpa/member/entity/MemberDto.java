package study.datajpa.member.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class MemberDto {

    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        if (member.getTeam() != null) {
            this.teamName = member.getTeam().getName();
        }
    }
}
