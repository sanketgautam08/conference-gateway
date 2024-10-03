package com.sanketgautam.conference.gateway.repo;

import com.sanketgauatm.bog.dto.AvailableConference;
import com.sanketgauatm.bog.model.Conference;
import com.sanketgauatm.bog.model.Confirmation;
import com.sanketgauatm.bog.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Repository
public class ConferenceRepository {
    private final JdbcClient jdbcClient;
    private final Logger LOGGER = LoggerFactory.getLogger(ConferenceRepository.class);

    public ConferenceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Conference> findById(int id){
        return Optional.of(jdbcClient.sql("select * from conference where conference_id = ?")
                .param(id).query(Conference.class).single());
    }

    public int getRoomId(int conferenceId){
        return jdbcClient.sql("select room_id from conference where conference_id = ?").param(conferenceId).query(Integer.class).single();
    }

    public boolean cancelUserConference(String confirmationNumber, int userId) {
        int conferenceId = jdbcClient.sql("select conference_id from confirmation where confirmation_number = ? and user_id = ?")
                .params(confirmationNumber, userId).query(Integer.class).single();
            String removeFromConfirmationSql = """
                    delete from confirmation
                    where confirmation_number = ?
                    """;
            try{
                jdbcClient.sql(removeFromConfirmationSql).param(confirmationNumber).update();
                return deleteFromConferenceUsers(conferenceId, userId);
            }catch(Exception e){
                LOGGER.error("Error while removing from confirmation: {} ", e.getMessage());
                return false;
            }

    }

    private boolean deleteFromConferenceUsers(int conferenceId, int userId) {
        String sql = """
                delete from conference_users where conference_id = ? and user_id = ?
                """;
        try{
            jdbcClient.sql(sql).params(conferenceId,userId).update();
            LOGGER.info("Deleted from conference_user with id {}", conferenceId);
            return true;
        }catch(Exception e){
            LOGGER.error("Error while deleting from conference_user\n{}", e.getMessage());
            return false;
        }
    }

    public Optional<List<AvailableConference>> getAvailableConferences(LocalDate date) {
        String sql = """
                select c.conference_id, c.name, r.max_capacity-count(cu.conference_id) as available_seats from conference c
                inner join rooms r
                on c.room_id = r.room_id
                full join conference_users cu
                on cu.conference_id = c.conference_id
                where c.date_time = ?
                group by c.conference_id, r.max_capacity
                having count(cu.conference_id) <r.max_capacity
                """;
        return Optional.of(jdbcClient.sql(sql).param(date).query(AvailableConference.class).list());
    }

    public boolean validateConfirmation(Confirmation confirmation) {
        String sql = """
                select count(1) from confirmation
                where confirmation_number = ?
                and conference_id = ?
                and user_id = ?
                """;
        try{

            return jdbcClient.sql(sql)
                    .params(confirmation.getConfirmationNumber(),confirmation.getConference_id().getId(),confirmation.getUserId().getId())
                    .query(Integer.class).single() > 0;
        }catch(Exception e){
            LOGGER.error("Could not validate confirmation number\n {}", e.getMessage());
            return false;
        }
    }

    public boolean validateUserConfirmationNumber(String confirmationNumber, int userId) {
        String sql = """
                select count(1) from confirmation
                where confirmation_number = ?
                and user_id = ?
                """;
        try{

            return jdbcClient.sql(sql)
                    .params(confirmationNumber,userId)
                    .query(Integer.class).single() > 0;
        }catch(Exception e){
            LOGGER.error("Could not validate confirmation number\n {}", e.getMessage());
            return false;
        }
    }

    public Integer countRegisteredUsers(int id) {
        return jdbcClient.sql("select count(1) from conference_users where conference_id = ?").param(id).query(Integer.class).single();
    }

    public String insertIntoConferenceUsers(int id, User user) {
        String sql = """
                insert into conference_users(conference_id, user_id)
                values(:conferenceId, :userId)
                """;
        try{
            jdbcClient.sql(sql).params(Map.of("conferenceId", id, "userId", user.getId())).update();
            LOGGER.info("Inserted user into conference_users");
            boolean insertedConfirmation = false;
            String randomstring = "";
            while(!insertedConfirmation){
                randomstring = generateRandomString();
                insertIntoConfirmation(randomstring, id, user.getId());
                insertedConfirmation = true;
            }
            return randomstring;
        }catch(Exception e){
            LOGGER.error("Error while inserting into conference_user\n{}", e.getMessage());
            return null;
        }
    }
    private void insertIntoConfirmation(String confirmationNumber, int conferenceId, int userId) {
        String sql = """
                insert into confirmation(confirmation_number, conference_id, user_id)
                values(?,?,?)
                """;
        jdbcClient.sql(sql).params(confirmationNumber,conferenceId, userId).update();
        LOGGER.info("Confirmation added to db.");
    }

    private String generateRandomString(){
        String alphaNumericStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        Random random = new Random();
        StringBuilder s = new StringBuilder(8);

        for(int i = 0; i < 8; i++) {
            int position = random.nextInt(alphaNumericStr.length());
            s.append(alphaNumericStr.charAt(position));
        }
        return s.toString();
    }

}
