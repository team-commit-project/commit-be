package com.receiptmate.category.entity;

import com.receiptmate.user.entity.UserCompanyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserCompanyEntity user;

    private String categoryName;

    @Column(name = "is_active")
    private boolean active;

    private CategoryEntity(UserCompanyEntity user, String categoryName) {
        this.user = user;
        this.categoryName = categoryName;
        this.active = true;
    }

    public static CategoryEntity create(UserCompanyEntity user, String categoryName) {
        return new CategoryEntity(user, categoryName);
    }

    public void activate() {
        this.active = true;
    }
}
