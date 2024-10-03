package com.sanketgautam.conference.gateway.repo;

import com.sanketgauatm.bog.model.Feedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class FeedbackRepository {
    private final JdbcClient jdbcClient;
    private final Logger LOGGER = LoggerFactory.getLogger(FeedbackRepository.class);

    public FeedbackRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean saveFeedback(Feedback feedback) {
        String sql = """
                INSERT INTO feedback(feedback_id,conference_id,user_id,feedback_text,rating,user_name)
                values (:feedbackId,:conference,:user,:feedbackText,:rating,:userName)
                """;
        var feedBackId = jdbcClient.sql("select nextval('feedback_seq')").query(Integer.class).single();
        Map<String, Object> params = Map.of(
                "feedbackId", feedBackId,
                "conference", feedback.getFeedbackConference().getId(),
                "user", feedback.getFeedbackUser().getId(),
                "feedbackText", feedback.getFeedbackText(),
                "rating", feedback.getRating(),
                "userName", feedback.getUserName()
        );
        try {
            jdbcClient.sql(sql).params(params).update();
            LOGGER.info("Feedback saved successfully");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to save feedback", e);
            return false;
        }
    }

}
