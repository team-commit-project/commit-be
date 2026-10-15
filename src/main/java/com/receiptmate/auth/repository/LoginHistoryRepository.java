package com.receiptmate.auth.repository;

import com.receiptmate.auth.entity.LoginHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistoryEntity, Long>, LoginHistoryRepositoryCustom {

}
