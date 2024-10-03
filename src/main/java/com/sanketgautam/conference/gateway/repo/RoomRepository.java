package com.sanketgautam.conference.gateway.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class RoomRepository {
    private final JdbcClient jdbcClient;
    private final Logger LOGGER = LoggerFactory.getLogger(RoomRepository.class);

    public RoomRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Integer getRoomCapacity(Integer roomId) {
        String sql = "SELECT max_capacity FROM rooms WHERE room_id = :roomId";
        try{
            return jdbcClient.sql(sql).params(Map.of("roomId", roomId)).query(Integer.class).single();
        }catch(Exception e){
            LOGGER.error("Error while checking room_capacity\n{}", e.getMessage());
            return null;
        }
    }
}