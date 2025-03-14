package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.Action;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {

    @Query("SELECT COUNT(DISTINCT a.userId) FROM Action a " +
            "WHERE a.action = 'VIEW' AND a.eventId = :eventId " +
            "AND a.userId NOT IN (" +
            "   SELECT a2.userId FROM Action a2 " +
            "   WHERE a2.eventId = :eventId AND (a2.action = 'LIKE' OR a2.action = 'REGISTER')" +
            ")")
    long countUserIdsWithViewOnly(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(DISTINCT a.userId) FROM Action a " +
            "WHERE a.action = 'LIKE' AND a.eventId = :eventId " +
            "AND a.userId NOT IN (" +
            "   SELECT a2.userId FROM Action a2 " +
            "   WHERE a2.eventId = :eventId AND (a2.action = 'REGISTER' OR a2.action = 'VIEW')" +
            ")")
    long countUserIdsWithLikeOnly(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(DISTINCT a.userId) FROM Action a " +
            "WHERE a.action = 'REGISTER' AND a.eventId = :eventId " +
            "AND a.userId NOT IN (" +
            "   SELECT a2.userId FROM Action a2 " +
            "   WHERE a2.eventId = :eventId AND (a2.action = 'LIKE' OR a2.action = 'VIEW')" +
            ")")
    long countUserIdsWithRegisterOnly(@Param("eventId") Long eventId);

    @Query("SELECT DISTINCT a.eventId FROM Action a WHERE a.userId = :uid")
    List<Long> findEventIdsByUserId(@Param("uid") Long uid);

    @Query("SELECT DISTINCT a.eventId FROM Action a WHERE a.userId = :uid ORDER BY a.timestamp ASC")
    List<Long> findEventIdsByUserIdSortByTstamp(@Param("uid") Long uid, Pageable pageable);

    @Query("SELECT DISTINCT a.eventId FROM Action a WHERE a.userId <> :uid")
    List<Long> findNotInteractedEventIdsByUserId(@Param("uid") Long uid);


}