package com.dtaquito_micro_services.rooms_service.player_list.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerListRepository extends JpaRepository<PlayerList, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PlayerList pl WHERE pl.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT pl FROM PlayerList pl WHERE pl.room.id = :roomId")
    Optional<PlayerList> findFirstByRoomId(@Param("roomId") Long roomId);

    boolean existsByRoomAndUserId(Rooms room, Long userId);

    @Query("SELECT pl FROM PlayerList pl WHERE pl.room.id = :roomId")
    List<PlayerList> findByRoomId(@Param("roomId") Long roomId);
}