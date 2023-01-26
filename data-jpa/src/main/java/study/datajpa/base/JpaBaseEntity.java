package study.datajpa.base;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class JpaBaseEntity {

    @Column(updatable = false)
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist // em.persist() 발생 전에 실행
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now; // 필드명이 겹치거나 강조할 때가 아니면 this는 생략해도 상관없다.
        this.updatedDate = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
