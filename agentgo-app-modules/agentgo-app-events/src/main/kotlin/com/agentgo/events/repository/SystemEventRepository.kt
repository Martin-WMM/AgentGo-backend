package com.agentgo.events.repository

import com.agentgo.events.entity.SystemEventEntity
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.query.QueryByExampleExecutor

/**
 * Persistence access for global [SystemEventEntity] rows.
 *
 * Callers enforce the three-month and 1,000-row retention window. This repository
 * only exposes the queries that window needs.
 */
interface SystemEventRepository :
    JpaRepository<SystemEventEntity, Long>,
    JpaSpecificationExecutor<SystemEventEntity>,
    QueryByExampleExecutor<SystemEventEntity> {

    /**
     * Deletes every event that happened before [cutoff].
     *
     * The persistence context is cleared so later reads do not reuse removed rows.
     *
     * @param cutoff exclusive upper bound of the discarded window
     * @return number of deleted rows
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SystemEventEntity event where event.occurredAt < :cutoff")
    fun deleteOlderThan(@Param("cutoff") cutoff: Instant): Int

    /**
     * Counts events that sort after [occurredAt] and [id].
     *
     * Newer means a later [occurredAt], or the same instant with a greater identifier.
     * The row identified by [id] is not included.
     *
     * @param occurredAt event instant to compare
     * @param id event identifier used as the tie-breaker
     * @return number of strictly newer events
     */
    @Query(
        """
        select count(event) from SystemEventEntity event
        where event.occurredAt > :occurredAt
           or (event.occurredAt = :occurredAt and event.id > :id)
        """,
    )
    fun countNewerThan(
        @Param("occurredAt") occurredAt: Instant,
        @Param("id") id: Long,
    ): Long
}
