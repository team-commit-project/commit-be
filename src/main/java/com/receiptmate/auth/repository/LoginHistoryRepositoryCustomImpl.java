package com.receiptmate.auth.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.receiptmate.auth.entity.LoginHistoryEntity;
import com.receiptmate.auth.entity.QLoginHistoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.receiptmate.auth.entity.QLoginHistoryEntity.*;

@Repository
@RequiredArgsConstructor
public class LoginHistoryRepositoryCustomImpl implements LoginHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<LoginHistoryEntity> findRecentTwoByUserId(Long userId) {
        return queryFactory
                .selectFrom(loginHistoryEntity)
                .where(loginHistoryEntity.userEntity.userId.eq(userId))
                .orderBy(
                        loginHistoryEntity.loginAt.desc(),
                        loginHistoryEntity.loginHistoryId.desc()
                )
                .limit(2)
                .fetch();
    }
}
