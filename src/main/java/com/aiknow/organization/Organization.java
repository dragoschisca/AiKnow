package com.aiknow.organization;

import com.aiknow.common.BaseEntity;
import com.aiknow.subscription.SubscriptionPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan = SubscriptionPlan.FREE;
}
