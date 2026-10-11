package com.agentgo.auth.repository

import com.agentgo.auth.entity.UserProfileEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.QueryByExampleExecutor

/**
 * Persistence access for [UserProfileEntity] rows.
 *
 * Lookups are keyed by the OIDC subject so later agent code can load the signed-in
 * user's personal context without accepting an owner id from a client payload.
 */
interface UserProfileRepository :
    JpaRepository<UserProfileEntity, UUID>,
    JpaSpecificationExecutor<UserProfileEntity>,
    QueryByExampleExecutor<UserProfileEntity> {
    /**
     * Finds the profile owned by an OIDC subject.
     *
     * @param ownerUserId OIDC subject
     * @return matching profile, or `null` when the user has not been synchronized yet
     */
    fun findByOwnerUserId(ownerUserId: String): UserProfileEntity?

    /**
     * Finds the profile mirrored for an Authentik username.
     *
     * @param username Authentik username
     * @return matching profile, or `null` when absent
     */
    fun findByUsername(username: String): UserProfileEntity?

    /**
     * Finds the profile linked to an Authentik user UUID.
     *
     * @param authentikUserUuid Authentik core user UUID
     * @return matching profile, or `null` when absent
     */
    fun findByAuthentikUserUuid(authentikUserUuid: UUID): UserProfileEntity?
}
