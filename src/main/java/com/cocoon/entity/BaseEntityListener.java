package com.cocoon.entity;

import com.cocoon.entity.common.UserPrincipal;
import com.cocoon.exception.CocoonException;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Component
public class BaseEntityListener extends AuditingEntityListener {


    @PrePersist
    public void onPrePersist(BaseEntity baseEntity) throws CocoonException {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        baseEntity.setCreatedBy(userPrincipal.getId());
        baseEntity.setCreatedTime(LocalDateTime.now());
        onPreUpdate(baseEntity);
    }

    @PreUpdate
    public void onPreUpdate(BaseEntity baseEntity) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        baseEntity.setUpdatedBy(userPrincipal.getId());
        baseEntity.setUpdatedTime(LocalDateTime.now());
    }
}
