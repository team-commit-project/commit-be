package com.receiptmate.auth.repository;

import com.receiptmate.auth.entity.LoginHistoryEntity;

import java.util.List;

public interface LoginHistoryRepositoryCustom {

    List<LoginHistoryEntity> findRecentTwoByUserId(Long userId);

}
