package com.dhatvibs.modules.repository.auth;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.auth.CbUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CbUserRepository
        extends JpaRepository<CbUser, UUID> {

    Optional<CbUser> findByPhone(String phone);

    Optional<CbUser> findByExternalUserIdAndAppId(
            String externalUserId, String appId);
}