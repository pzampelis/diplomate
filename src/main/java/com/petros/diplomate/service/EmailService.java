package com.petros.diplomate.service;

import com.petros.diplomate.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private EmailValidatorService emailValidatorService;


    public void checkEmailValidity(String email) throws IllegalStateException {
        if (!emailValidatorService.test(email))
            throw new IllegalStateException("Email not found");
    }

    @Async
    public void send(String to, String email, String subject) throws IllegalStateException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            mimeMessageHelper.setText(email, true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject("DiploMate - " + subject);
            mimeMessageHelper.setFrom("diplomate-app@mail.com");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("Failed to send email", e);
            throw new IllegalStateException("Failed to send email");
        }
    }

    public String buildLinkEmail(String firstName, String subject, String link) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>You can " + subject.toLowerCase() + " by clicking the link bellow:</p>" +
               "    <a href=" + link + ">click here</a>" +
               "    <p>The link will expire in 20 minutes.</p>" +
               "</div>";
    }

    public String buildTopicUpdateEmail(String firstName, String subject, String topicName) {
        String emailBody = "<div>" +
                           "    <p>Hello " + firstName + ".</p>";

        if (subject.equals("Rejected for Topic"))
            emailBody +=  "    <p>We are sorry to inform you that you were not the one who got selected for the topic " +
                          "       with name " + topicName + ".</p>";
        else
            emailBody +=  "    <p>We are happy to inform you that you were the one who got selected for the topic " +
                          "       with name " + topicName + ".</p>" +
                          "    <p>Congratulations!</p>";

        emailBody += "</div>";

        return emailBody;
    }

    public String buildInterviewSessionEmail(String firstName, InterviewSession session) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>Professor " + session.getProfessor().getFullName() + " has scheduled an interview" +
               "       with you about the topic " + session.getTopic().getName() + ".</p>" +
               "    <p>The interview will take place on " + session.getDate() + " at " + session.getTime() + ".</p>" +
               "</div>";
    }

    public String buildMeetingSessionEmail(String firstName, MeetingSession session) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>Professor " + session.getProfessor().getFullName() + " has scheduled a meeting on "
                    + session.getDate() + " at " + session.getTime() + ".</p>" +
               "</div>";
    }

    public String buildExaminationSessionEmail(String firstName, ExaminationSession session) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>You have been invited by professor " + session.getProfessor().getFirstName() +
               " to the examination of the topic " + session.getTopic().getName() + ".</p>" +
               "    <p>The examination will take place on " + session.getDate() + " at " + session.getTime() + ".</p>" +
               "</div>";
    }

    public String buildRescheduleInterviewEmail(String firstName, InterviewSession session, LocalDate newDate, LocalTime newTime) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>Your previously scheduled interview with professor " + session.getProfessor().getFullName() +
               " about the topic " + session.getTopic().getName() + ", on " + session.getDate() + " at "
               + session.getTime() + ", has been rescheduled for " + newDate + " at " + newTime + ".</p>" +
               "</div>";
    }

    public String buildRescheduleMeetingEmail(String firstName, MeetingSession session, LocalDate newDate, LocalTime newTime) {
        return "<div>" +
                "    <p>Hello " + firstName + ".</p>" +
                "    <p>Your previously scheduled meeting with professor " + session.getProfessor().getFullName() +
                ", on " + session.getDate() + " at " + session.getTime() + ", has been rescheduled for " + newDate +
                " at " + newTime + ".</p>" +
                "</div>";
    }

    public String buildRescheduleExaminationEmail(String firstName, ExaminationSession session, LocalDate newDate, LocalTime newTime) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>The previously scheduled examination by professor " + session.getProfessor().getFirstName() +
               " for the topic " + session.getTopic().getName() + ", on " + session.getDate() + " at "
                + session.getTime() + ", has been rescheduled for " + newDate + " at " + newTime + ".</p>" +
                "</div>";
    }

    public String buildCancelSessionEmail(String firstName, Session session, String subject) {
        String emailBody = "<div>" +
                           "    <p>Hello " + firstName + ".</p>";

        if (subject.equals("Interview Canceled"))
            emailBody +=   "    <p>We would like to inform you that the interview scheduled for " + session.getDate()
                        +  " at " + session.getTime() + " about the topic " + ((InterviewSession) session).getTopic().getName()
                        +  " has been cancelled.</p>";
        else if (subject.equals("Meeting Canceled"))
            emailBody +=   "    <p>We would like to inform you that the meeting scheduled for " + session.getDate()
                        +  " at " + session.getTime() + " with the professor " + session.getProfessor().getFullName()
                        +  " has been cancelled.</p>";
        else
            emailBody +=   "    <p>We would like to inform you that the examination scheduled for " + session.getDate()
                        +  " at " + session.getTime() + " about the topic " + ((ExaminationSession) session).getTopic().getName()
                        +  " has been cancelled.</p>";

        emailBody += "</div>";

        return emailBody;
    }

    public String buildCancelInterviewByStudentEmail(InterviewSession session) {
        return "<div>" +
               "    <p>Hello " + session.getProfessor().getFirstName() + ".</p>" +
               "    <p>We would like to inform you that the interview scheduled for " + session.getDate() + " at " +
               session.getTime() + " about the topic " + session.getTopic().getName() + " has been cancelled " +
               "as the student " + session.getStudent().getFullName() + " got accepted for another topic.</p>" +
               "</div>";
    }

    public String buildFileUploadedEmail(String receiverName, String uploaderName, String topicName) {
        return "<div>" +
               "    <p>Hello " + receiverName + ".</p>" +
               "    <p>We would like to inform you that " + uploaderName + ", has uploaded a new file on topic " +
                topicName + ".</p>" +
               "    <p>Be sure to check it out.</p>" +
               "</div>";
    }

    public String buildTopicScoredEmail(String firstName, String topicName) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>We would like to inform you that your topic with name " + topicName + ", has been rated.</p>" +
               "    <p>Please go to the application to see your topics final score.</p>" +
               "</div>";
    }

    public String buildTopicDeletedEmail(String firstName, String topicName) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>We would like to inform you that the topic with name " + topicName + ", has been deleted.</p>" +
               "</div>";
    }

    public String buildNewTopicNotificationEmail(String firstName, Topic topic) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>We would like to inform you that professor " + topic.getProfessor().getFullName() +
               " added a new topic with name " + topic.getName() + ", that we think you will find interesting.</p>" +
               "    <p>Go check it out.</p>" +
               "</div>";
    }

    public String buildNotifyProfessorAboutOpenTopicWasTakenEmail(Topic topic) {
        return "<div>" +
               "    <p>Hello " + topic.getProfessor().getFirstName() + ".</p>" +
               "    <p>We would like to inform you that your open topic with name " + topic.getName() +
               " was taken by " + topic.getStudent().getFullName() + ".</p>" +
               "    <p>We hope you a pleasant cooperation.</p>" +
               "</div>";
    }

    public String buildScoreExaminationReminderEmail(String firstName, String topicName) {
        return "<div>" +
               "    <p>Hello " + firstName + ".</p>" +
               "    <p>We would like to remind you that you have not yet scored the completed examination of the topic with name "
               + topicName + ".</p>" +
               "    <p>Please head to the Unrated examinations section in your calendar to do so.</p>" +
               "</div>";
    }

}
