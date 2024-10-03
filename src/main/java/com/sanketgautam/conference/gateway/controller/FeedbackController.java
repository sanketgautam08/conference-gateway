package com.sanketgautam.conference.gateway.controller;

import com.sanketgauatm.bog.model.Confirmation;
import com.sanketgauatm.bog.model.Feedback;
import com.sanketgautam.conference.gateway.repo.ConferenceRepository;
import com.sanketgautam.conference.gateway.repo.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {
    private final Logger LOGGER = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackRepository feedbackRepo;
    private final ConferenceRepository conferenceRepo;

    public FeedbackController(FeedbackRepository feedbackRepo, ConferenceRepository conferenceRepo) {
        this.feedbackRepo = feedbackRepo;
        this.conferenceRepo = conferenceRepo;
    }

    @PostMapping("/{confirmationId}")
    public ResponseEntity<String> postFeedback(@PathVariable String confirmationId, @RequestBody Feedback feedback) {
        Confirmation confirmation = new Confirmation(confirmationId, feedback.getFeedbackConference(), feedback.getFeedbackUser());
        boolean isConfirmationValid = conferenceRepo.validateConfirmation(confirmation);
        if(isConfirmationValid) {
            String userName = feedback.getFeedbackUser().getFirstName() + " " + feedback.getFeedbackUser().getLastName().charAt(0);
            feedback.setUserName(userName);
            boolean feedbackSaved = feedbackRepo.saveFeedback(feedback);
            if(feedbackSaved){
                LOGGER.info("Successfully posted feedback");
                return ResponseEntity.ok("Successfully posted feedback");
            }
        }
        LOGGER.error("Error while posting feedback");
        return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
    }

}
