package com.petros.diplomate.service;

import com.petros.diplomate.model.*;
import com.petros.diplomate.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ExaminationScoreService examinationScoreService;


    public Session getSessionById(Long sessionId) throws IllegalStateException {
        return sessionRepository.findById(sessionId).orElseThrow(
                () -> new IllegalStateException("Session with ID " + sessionId + " not found")
        );
    }

    public List<Session> getAllSessionsByType(String sessionType) {
        return sessionRepository.findAllByType(sessionType);
    }

    public void deleteSessionById(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    public List<Session> getUpcomingSessionsByTypeAndProfessor(String sessionType, Professor professor) {
        List<Session> matchingSessions = new ArrayList<>();

        for (Session session : this.getAllSessionsByType(sessionType))
            if (session.getProfessor().equals(professor) && !this.hasSessionPassed(session))
                matchingSessions.add(session);
            else if (session.getProfessor().equals(professor) && sessionType.equals("MEETING_TYPE") &&
                    this.hasSessionPassed(session))
                this.deletePassedMeeting((MeetingSession) session);

        matchingSessions.sort(Comparator.comparing(Session::getDate).thenComparing(Session::getTime));

        return matchingSessions;
    }

    public List<Session> getSessionsByProfessor(Professor professor) {
        List<Session> professorSessions = new ArrayList<>();

        for (Session session : this.getAllSessionsByType("INTERVIEW_TYPE"))
            if (session.getProfessor().equals(professor) && !this.hasSessionPassed(session))
                professorSessions.add(session);
        for (Session session : this.getAllSessionsByType("MEETING_TYPE"))
            if (session.getProfessor().equals(professor) && !this.hasSessionPassed(session))
                professorSessions.add(session);
        for (Session session : this.getAllSessionsByType("EXAMINATION_TYPE"))
            if ((session.getProfessor().equals(professor) || ((ExaminationSession) session).getInvitedProfessors().contains(professor))
                    && !this.hasSessionPassed(session))
                professorSessions.add(session);

        return professorSessions;
    }

    public void checkForUnratedCompletedExaminations(Professor professor) {
        for (Session session : this.getAllSessionsByType("EXAMINATION_TYPE"))
            if ((session.getProfessor().equals(professor) || ((ExaminationSession) session).getInvitedProfessors().contains(professor))
                    && this.hasSessionPassed(session))
                if ((examinationScoreService.getExaminationScoreByProfessorIdAndTopicId(
                        professor.getId(), ((ExaminationSession) session).getTopic().getId())).isEmpty())

                    emailService.send(session.getProfessor().getEmail(),
                            emailService.buildScoreExaminationReminderEmail(session.getProfessor().getFirstName(),
                                    ((ExaminationSession) session).getTopic().getName()), "Examination Scoring Reminder");
    }

    public boolean hasSessionPassed(Session session) {
        return (session.getDate().isEqual(LocalDate.now()) && session.getTime().isBefore(LocalTime.now())) ||
               session.getDate().isBefore(LocalDate.now());
    }

    public void deletePassedMeeting(MeetingSession meeting) {
        meeting.setProfessor(null);
        meeting.getInvitedStudents().clear();
        sessionRepository.deleteById(meeting.getId());
    }

    public void cancelSession(Session session) {
        switch (session.getType()) {
            case "INTERVIEW_TYPE": {
                InterviewSession interviewSession = (InterviewSession) session;
                Student student = interviewSession.getStudent();
                String subject = "Interview Canceled";
                emailService.send(student.getEmail(),
                        emailService.buildCancelSessionEmail(student.getFirstName(), session, subject),
                        subject);

                sessionRepository.deleteById(session.getId());
                break;
            }
            case "MEETING_TYPE": {
                MeetingSession meetingSession = (MeetingSession) session;
                List<Student> students = meetingSession.getInvitedStudents();
                String subject = "Meeting Canceled";
                for (Student student : students)
                    emailService.send(student.getEmail(),
                            emailService.buildCancelSessionEmail(student.getFirstName(), session, subject),
                            subject);

                this.deletePassedMeeting((MeetingSession) session);
                break;
            }
            case "EXAMINATION_TYPE": {
                ExaminationSession examinationSession = (ExaminationSession) session;
                String subject = "Examination Canceled";
                Student student = examinationSession.getStudent();
                emailService.send(student.getEmail(),
                        emailService.buildCancelSessionEmail(student.getFirstName(), session, subject),
                        subject);

                List<Professor> professors = ((ExaminationSession) session).getInvitedProfessors();
                for (Professor professor : professors)
                    emailService.send(professor.getEmail(),
                            emailService.buildCancelSessionEmail(professor.getFirstName(), session, subject),
                            subject);

                ((ExaminationSession) session).setTopic(null);
                ((ExaminationSession) session).getInvitedProfessors().clear();

                sessionRepository.deleteById(session.getId());
                break;
            }
        }
    }

    public void checkUserAvailability(User user, LocalDate date, LocalTime time, Long reschedulingSessionId)
            throws IllegalStateException {

        ArrayList<Session> allSessions = new ArrayList<>();

        if (user.getRole().equals("PROFESSOR_ROLE")) {
            allSessions.addAll(this.getSessionsByProfessor((Professor) user));
            allSessions.addAll(this.getExaminationSessionsProfessorIsInvitedTo((Professor) user));
        }
        else if (user.getRole().equals("STUDENT_ROLE")) {
            allSessions.addAll(this.getUpcomingInterviewsByStudent((Student) user));
            allSessions.addAll(this.getUpcomingMeetingsByStudent((Student) user));

            ExaminationSession exam = this.getExaminationSessionByStudent((Student) user);
            if (exam != null)
                allSessions.add(exam);
        }

        // in case of rescheduling, ignore the session being rescheduled
        if (reschedulingSessionId != null)
            allSessions.remove(this.getSessionById(reschedulingSessionId));

        for (Session session : allSessions)
            if (this.areDateAndTimeTaken(session.getDate(), session.getTime(), date, time))
                throw new IllegalStateException(user.getFullName() + " has something already scheduled for " +
                                                session.getDate() + ", from " + session.getTime() + " to " +
                                                session.getTime().plusMinutes(60) + ".");
    }

    public boolean areDateAndTimeTaken(LocalDate scheduledDate, LocalTime scheduledTime , LocalDate givenDate,
                                        LocalTime givenTime) throws IllegalStateException {

        if (givenDate.equals(scheduledDate) && (givenTime.equals(scheduledTime) ||
            (givenTime.isBefore(scheduledTime) && (givenTime.plusMinutes(60)).isAfter(scheduledTime)) ||
            (givenTime.isBefore(scheduledTime.plusMinutes(60)) && (givenTime.plusMinutes(60)).isAfter(scheduledTime.plusMinutes(60)))))
            return true;

        return false;
    }

    public List<ExaminationSession> getUpcomingOthersExaminations(List<ExaminationSession> examinationsInvitedTo) {
        ArrayList<ExaminationSession> upcomingExaminations = new ArrayList<>();

        for (ExaminationSession exam : examinationsInvitedTo)
            if (!this.hasSessionPassed(exam))
                upcomingExaminations.add(exam);

        return upcomingExaminations;
    }

    public List<Session> getAllInterviewSessions() {
        return sessionRepository.findAllByType("INTERVIEW_TYPE");
    }

    public InterviewSession getInterviewSessionByProfessorAndStudent(Professor professor, Student student) {
        for (Session session : this.getAllInterviewSessions())
            if (session.getProfessor().equals(professor) && ((InterviewSession) session).getStudent().equals(student))
                return (InterviewSession) session;

        return null;
    }

    public List<InterviewSession> getUnratedPassedInterviewSessionsByTopic(Topic topic) {
        List<InterviewSession> matchingSessions = new ArrayList<>();

        for (Session session : this.getAllInterviewSessions())
            if (((InterviewSession) session).getTopic().equals(topic) && this.hasSessionPassed(session) &&
                    ((InterviewSession) session).getScore() == null)

                matchingSessions.add(((InterviewSession) session));

        return matchingSessions;
    }

    public List<InterviewSession> getRatedPassedInterviewSessionsByTopic(Topic topic) {
        List<InterviewSession> matchingSessions = new ArrayList<>();

        for (Session session : this.getAllInterviewSessions())
            if (((InterviewSession) session).getTopic().equals(topic) && this.hasSessionPassed(session) &&
                    ((InterviewSession) session).getScore() != null)

                matchingSessions.add(((InterviewSession) session));

        matchingSessions.sort(Comparator.comparing(InterviewSession::getScore).reversed()); // sort by score

        return matchingSessions;
    }

    public void scheduleInterviewSession(InterviewSession session) {
        this.isInterviewAlreadyScheduled(session);
        this.checkDateAndTimeValidity(session.getDate(), session.getTime());
        this.checkUserAvailability(session.getProfessor(), session.getDate(), session.getTime(), null);
        this.checkUserAvailability(session.getStudent(), session.getDate(), session.getTime(), null);

        emailService.send(session.getStudent().getEmail(),
                emailService.buildInterviewSessionEmail(session.getStudent().getFirstName(), session),
                "Interview Scheduled");

        sessionRepository.save(session);
    }

    public void isInterviewAlreadyScheduled(InterviewSession interviewSession) throws IllegalStateException {
        for (Session session : this.getAllInterviewSessions())
            if (interviewSession.getStudent().equals(((InterviewSession) session).getStudent()) &&
                    interviewSession.getProfessor().equals(session.getProfessor()))
                throw new IllegalStateException("This interview has already been scheduled for " + session.getDate() +
                        " at " + session.getTime());
    }

    public void rescheduleInterview(InterviewSession session, LocalDate newDate, LocalTime newTime) {
        this.checkUserAvailability(session.getProfessor(), newDate, newTime, session.getId());
        this.checkUserAvailability(session.getStudent(), newDate, newTime, session.getId());

        emailService.send(session.getStudent().getEmail(),
                emailService.buildRescheduleInterviewEmail(session.getStudent().getFirstName(), session, newDate, newTime),
                "Interview Rescheduled");

        session.setDate(newDate);
        session.setTime(newTime);

        sessionRepository.save(session);
    }

    public void rateInterview(InterviewSession interview, Double score) {
        interview.setScore(score);
        sessionRepository.save(interview);
    }

    public void removeInterviewSessionsByTopic(Topic topic) {
        for (Session session : this.getAllInterviewSessions())
            if (((InterviewSession) session).getTopic().equals(topic))
                sessionRepository.deleteById(session.getId());
    }

    public List<InterviewSession> getUpcomingInterviewsByTopic(Topic topic) {
        List<InterviewSession> upcomingInterviews = new ArrayList<>();

        for (Session session : this.getAllInterviewSessions())
            if (((InterviewSession) session).getTopic().equals(topic) && !this.hasSessionPassed(session))
                upcomingInterviews.add(((InterviewSession) session));

        upcomingInterviews.sort(Comparator.comparing(Session::getDate).thenComparing(Session::getTime));

        return upcomingInterviews;
    }

    public List<InterviewSession> getUpcomingInterviewsByStudent(Student student) {
        List<InterviewSession> upcomingStudentInterviews = new ArrayList<>();

        for (Session interview : this.getAllInterviewSessions())
            if (((InterviewSession) interview).getStudent().equals(student) && !this.hasSessionPassed(interview))
                upcomingStudentInterviews.add((InterviewSession) interview);

        upcomingStudentInterviews.sort(Comparator.comparing(Session::getDate).thenComparing(Session::getTime));

        return upcomingStudentInterviews;
    }

    public List<Session> getAllMeetingSessions() {
        return sessionRepository.findAllByType("MEETING_TYPE");
    }

    public void scheduleMeetingSession(MeetingSession session) {
        this.checkDateAndTimeValidity(session.getDate(), session.getTime());
        this.checkUserAvailability(session.getProfessor(), session.getDate(), session.getTime(), null);

        for (Student student : session.getInvitedStudents())
            emailService.send(student.getEmail(), emailService.buildMeetingSessionEmail(student.getFirstName(), session),
                    "Meeting Scheduled");

        sessionRepository.save(session);
    }

    public void rescheduleMeeting(MeetingSession session, LocalDate newDate, LocalTime newTime) {
        this.checkUserAvailability(session.getProfessor(), newDate, newTime, session.getId());

        for (Student student : session.getInvitedStudents())
            emailService.send(student.getEmail(),
                    emailService.buildRescheduleMeetingEmail(student.getFirstName(), session, newDate, newTime),
                    "Meeting Rescheduled");

        session.setDate(newDate);
        session.setTime(newTime);

        sessionRepository.save(session);
    }

    public List<MeetingSession> getUpcomingMeetingsByStudent(Student student) {
        List<MeetingSession> upcomingStudentMeetings = new ArrayList<>();

        for (Session meeting : this.getAllMeetingSessions())
            if (((MeetingSession) meeting).getInvitedStudents().contains(student) && !this.hasSessionPassed(meeting))
                upcomingStudentMeetings.add((MeetingSession) meeting);
            else if (this.hasSessionPassed(meeting))
                this.deletePassedMeeting((MeetingSession) meeting);

        upcomingStudentMeetings.sort(Comparator.comparing(Session::getDate));

        return upcomingStudentMeetings;
    }

    public List<Session> getAllExaminationSessions() {
        return sessionRepository.findAllByType("EXAMINATION_TYPE");
    }

    public ExaminationSession getExaminationSessionByStudent(Student student) {
        for (Session exam : this.getAllExaminationSessions())
            if (((ExaminationSession) exam).getStudent().equals(student))
                return (ExaminationSession) exam;

        return null;
    }

    public List<ExaminationSession> getExaminationSessionsProfessorIsInvitedTo(Professor professor) {
        List<ExaminationSession> examinationsProfessorsIsInvitedTo = new ArrayList<>();

        for (Session session : this.getAllExaminationSessions())
            if (((ExaminationSession) session).getInvitedProfessors().contains(professor))
                examinationsProfessorsIsInvitedTo.add((ExaminationSession) session);

        return examinationsProfessorsIsInvitedTo;
    }

    public boolean isProfessorsSelectionValid(Long supervisorId, Long professorId1, Long professorId2) {
        if (supervisorId.equals(professorId1) || supervisorId.equals(professorId2) || professorId1.equals(professorId2))
            return false;

        return true;
    }

    public void isExaminationAlreadyScheduled(ExaminationSession examinationSession) throws IllegalStateException {
        for (Session session : this.getAllExaminationSessions())
            if (examinationSession.getStudent().getId().equals(((ExaminationSession) session).getStudent().getId()) &&
                    examinationSession.getProfessor().getId().equals(session.getProfessor().getId()))
                throw new IllegalStateException("This examination has already been scheduled for " + session.getDate() +
                        " at " + session.getTime());
    }

    public void scheduleExaminationSession(ExaminationSession session) {
        this.isExaminationAlreadyScheduled(session);
        this.checkDateAndTimeValidity(session.getDate(), session.getTime());
        this.checkUserAvailability(session.getProfessor(), session.getDate(), session.getTime(), null);

        for (Professor professor : session.getInvitedProfessors())
            this.checkUserAvailability(professor, session.getDate(), session.getTime(), null);

        for (Professor professor : session.getInvitedProfessors())
            emailService.send(professor.getEmail(),
                    emailService.buildExaminationSessionEmail(professor.getFirstName(), session), "Examination Scheduled");

        emailService.send(session.getStudent().getEmail(),
                emailService.buildExaminationSessionEmail(session.getStudent().getFirstName(), session),
                "Examination Scheduled");

        sessionRepository.save(session);
    }

    public void rescheduleExamination(ExaminationSession session, LocalDate newDate, LocalTime newTime) {
        this.checkUserAvailability(session.getProfessor(), newDate, newTime, session.getId());

        for (Professor professor : session.getInvitedProfessors())
            this.checkUserAvailability(professor, newDate, newTime, session.getId());

        for (Professor professor : session.getInvitedProfessors())
            emailService.send(professor.getEmail(),
                    emailService.buildRescheduleExaminationEmail(professor.getFirstName(), session, newDate, newTime),
                    "Examination Rescheduled");

        emailService.send(session.getStudent().getEmail(),
                emailService.buildRescheduleExaminationEmail(session.getStudent().getFirstName(), session, newDate, newTime),
                "Examination Rescheduled");

        session.setDate(newDate);
        session.setTime(newTime);

        sessionRepository.save(session);
    }

    public List<ExaminationSession> getUnratedExaminationsByProfessor(Professor professor) {
        List<ExaminationSession> unratedExaminations = new ArrayList<>();

        for (ExaminationSession exam : this.getPassedExaminationSessionsByProfessor(professor))
            if ((examinationScoreService.getExaminationScoreByProfessorIdAndTopicId(professor.getId(), exam.getTopic().getId())).isEmpty())
                unratedExaminations.add(exam);

        return unratedExaminations;
    }

    public List<ExaminationSession> getPassedExaminationSessionsByProfessor(Professor professor) {
        List<ExaminationSession> passedExaminations = new ArrayList<>();

        for (ExaminationSession session : this.getExaminationSessionsByProfessor(professor))
            if (this.hasSessionPassed(session))
                passedExaminations.add(session);

        return passedExaminations;
    }

    public List<ExaminationSession> getExaminationSessionsByProfessor(Professor professor) {
        List<ExaminationSession> examinationSessions = new ArrayList<>();

        for (Session session : this.getAllExaminationSessions())
            if (((ExaminationSession) session).getInvitedProfessors().contains(professor) ||
                    session.getProfessor().equals(professor))

                examinationSessions.add((ExaminationSession) session);

        return examinationSessions;
    }

    public void checkDateAndTimeValidity(LocalDate date, LocalTime time) throws IllegalStateException {
        if (date.isEqual(LocalDate.now()))
            if (time.minusMinutes(60).isBefore(LocalTime.now()))
                throw new IllegalStateException("If date is today, time should be at least one (1) hour from now");
    }

}
