package study.datajpa.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.team.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
