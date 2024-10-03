package com.sanketgautam.conference.gateway.controller;

import com.sanketgauatm.bog.dto.AvailableConference;
import com.sanketgauatm.bog.model.Conference;
import com.sanketgauatm.bog.model.User;
import com.sanketgautam.conference.gateway.repo.ConferenceRepository;
import com.sanketgautam.conference.gateway.repo.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/conference")
public class ConferenceController {

    private final ConferenceRepository conferenceRepository;
    private final RoomRepository roomRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(ConferenceController.class);

    public ConferenceController(ConferenceRepository conferenceRepository, RoomRepository roomRepository) {
        this.conferenceRepository = conferenceRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableConference>> getAllConferencesAvialable(@RequestBody String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Optional<List<AvailableConference>> ac = conferenceRepository.getAvailableConferences(LocalDate.parse(date, formatter));
        return ac.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping("/cancel-user-conference/{confirmationNumber}")
    public ResponseEntity<String> cancelUserConference(@PathVariable("confirmationNumber") String confirmationNumber, @RequestBody User user) {
        boolean isConfirmationValid = conferenceRepository.validateUserConfirmationNumber(confirmationNumber, user.getId());
        if(isConfirmationValid){
            boolean isRemoved = conferenceRepository.cancelUserConference(confirmationNumber,user.getId());
            if (isRemoved) {
                return ResponseEntity.ok("Conference canceled");
            }
        }
        return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));

    }

    @PostMapping("/add-user/{conferenceId}")
    public ResponseEntity<String> registerUserToConference(@PathVariable int conferenceId, @RequestBody User user) {
        Optional<Conference> conference = conferenceRepository.findById(conferenceId);
        if(conference.isEmpty()){
            LOGGER.error("Couldn't find conference");
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
        }
        int roomId = conferenceRepository.getRoomId(conferenceId);
        Integer roomCapacity =  roomRepository.getRoomCapacity(roomId);
        Integer registeredUsers = conferenceRepository.countRegisteredUsers(conferenceId);

        if(user.getId() != null && (roomCapacity > registeredUsers)) {
            String uniqueConferenceCode = conferenceRepository.insertIntoConferenceUsers(conferenceId,user);
            LOGGER.info("user added to conference");
            return ResponseEntity.ok(uniqueConferenceCode);
        }else{
            LOGGER.error("Couldn't add user to conference");
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
        }
    }

}
