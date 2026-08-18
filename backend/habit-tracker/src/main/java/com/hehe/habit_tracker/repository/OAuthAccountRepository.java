package com.hehe.habit_tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.common.AuthProvider;
import com.hehe.habit_tracker.entity.OAuthAccount;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
