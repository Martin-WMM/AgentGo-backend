package com.agentgo.settings.repository

import com.agentgo.settings.entity.UserSettingsEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.QueryByExampleExecutor

/**
 * Persistence access for [UserSettingsEntity] rows.
 *
 * Each authenticated user owns at most one settings row.
 */
interface UserSettingsRepository :
    JpaRepository<UserSettingsEntity, UUID>,
    JpaSpecificationExecutor<UserSettingsEntity>,
    QueryByExampleExecutor<UserSettingsEntity> {
    /**
     * Finds the settings row owned by [ownerUserId].
     *
     * @param ownerUserId authenticated owner
     * @return matching entity, or `null` when the user has not saved settings
     */
    fun findByOwnerUserId(ownerUserId: String): UserSettingsEntity?
}
