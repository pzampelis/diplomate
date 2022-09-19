package com.petros.diplomate.service;

import com.petros.diplomate.model.*;
import com.petros.diplomate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SecureTokenService secureTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TopicService topicService;


    @Override
    public User loadUserByUsername(String userEmail) throws IllegalStateException {
        return userRepository.findByEmail(userEmail).orElseThrow(
                () -> new IllegalStateException("User with email " + userEmail + " not found")
        );
    }

    public User getUserById(Long userId) throws IllegalStateException {
        return userRepository.findById(userId).orElseThrow(
                () -> new IllegalStateException("User with id " + userId + " not found")
        );
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public String signUpUser(User user) throws IllegalStateException {
        boolean userExists = userRepository.findByEmail(user.getEmail()).isPresent();

        if (userExists) {
            User existedUser = loadUserByUsername(user.getEmail());
            SecureToken retrievedToken = secureTokenService.getSecureTokenByUserId(existedUser.getId());
            boolean tokenStillActive = LocalDateTime.now().isBefore(retrievedToken.getExpiresAt());

            if (existedUser.isEnabled())
                throw new IllegalStateException("Email already taken");
            else if (tokenStillActive)
                throw new IllegalStateException("Email has already been given but has not been verified yet");
            else {
                SecureToken secureToken = secureTokenService.getSecureTokenByUserId(existedUser.getId());
                userRepository.deleteById(existedUser.getId());
                secureTokenService.deleteSecureTokenById(secureToken.getId());
            }

        }

        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return secureTokenService.initializeSecureToken(user);
    }

    public void enableUser(String userEmail) {
        User user = this.loadUserByUsername(userEmail);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void changePassword(User user, String oldPassword, String newPassword, String reenteredNewPassword)
            throws IllegalStateException {

        if (!newPassword.equals(reenteredNewPassword))
            throw new IllegalStateException("Passwords do not match");

        if (bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            String newEncodedPassword = bCryptPasswordEncoder.encode(newPassword);
            user.setPassword(newEncodedPassword);
            userRepository.save(user);
        }
        else
            throw new IllegalStateException("Wrong password");
    }

    public void updatePassword(String email, String password, String reenteredPassword) throws IllegalStateException {
        if (!password.equals(reenteredPassword))
            throw new IllegalStateException("Passwords do not match");

        User user = loadUserByUsername(email);

        String newEncodedPassword = bCryptPasswordEncoder.encode(password);
        user.setPassword(newEncodedPassword);

        userRepository.save(user);
    }

    public void resetPassword(String email) throws IllegalStateException {
        emailService.checkEmailValidity(email);
        User user = loadUserByUsername(email);

        if (!user.getEnabled())
            throw new IllegalStateException("Account is not activated");

        String token = secureTokenService.updateSecureToken(user);
        String link = "http://localhost:8080/api/reset-password?token=" + token;
        String subject = "Reset Your Password";
        emailService.send(email, emailService.buildLinkEmail(user.getFirstName(), subject, link), subject);
    }

    public void confirmSecureToken(String token) throws IllegalStateException {
        SecureToken secureToken = secureTokenService.getSecureTokenByToken(token);

        LocalDateTime expiresAt = secureToken.getExpiresAt();

        if (expiresAt.isBefore(LocalDateTime.now()))
            throw new IllegalStateException("Token has been expired");

        if (!secureToken.getUser().getEnabled())
            this.enableUser(secureToken.getUser().getEmail());

        if (secureToken.isRedeemed())
            throw new IllegalStateException("Token has already been used");

        secureToken.setRedeemed(true);
    }

    public List<User> getAllProfessors() {
        return userRepository.findAllByRoleAndEnabledTrue("PROFESSOR_ROLE");
    }

    public List<Student> getStudentsWithoutTopic() {
        List<Student> studentsWithoutTopic = new ArrayList<>();

        for (User student : userRepository.findAllByRoleAndEnabledTrue("STUDENT_ROLE"))
            if (topicService.getTopicByStudentId(student.getId()).isEmpty() ||
                    topicService.getTopicByStudentId(student.getId()).get().getAcceptanceDeadline() != null)
                studentsWithoutTopic.add((Student) student);

        return studentsWithoutTopic;
    }

    public void updateProfessorInfo(Professor updatedProfessor) {
        Professor existedProfessor = (Professor) this.loadUserByUsername(updatedProfessor.getEmail());

        if (!((existedProfessor.getFirstName()).equals(updatedProfessor.getFirstName())) ||
                !((existedProfessor.getLastName()).equals(updatedProfessor.getLastName()))) {
            existedProfessor.setFirstName(updatedProfessor.getFirstName());
            existedProfessor.setLastName(updatedProfessor.getLastName());
            existedProfessor.setFullName(updatedProfessor.getFirstName() + " " + updatedProfessor.getLastName());
        }
        if (!((existedProfessor.getProfileImagePath()).equals(updatedProfessor.getProfileImagePath())))
            existedProfessor.setProfileImagePath(updatedProfessor.getProfileImagePath());
        if (existedProfessor.getPhoneNumber() != updatedProfessor.getPhoneNumber())
            existedProfessor.setPhoneNumber(updatedProfessor.getPhoneNumber());
        if (!((existedProfessor.getOfficeInfo()).equals(updatedProfessor.getOfficeInfo())))
            existedProfessor.setOfficeInfo(updatedProfessor.getOfficeInfo());
        if (!((existedProfessor.getWebpageLink()).equals(updatedProfessor.getWebpageLink())))
            existedProfessor.setWebpageLink(updatedProfessor.getWebpageLink());
        if (!((existedProfessor.getGithubLink()).equals(updatedProfessor.getGithubLink())))
            existedProfessor.setGithubLink(updatedProfessor.getGithubLink());
        if (!((existedProfessor.getAboutUser()).equals(updatedProfessor.getAboutUser())))
            existedProfessor.setAboutUser(updatedProfessor.getAboutUser());

        userRepository.save(existedProfessor);
    }

    public void updateStudentInfo(Student updatedStudent, List<String> updatedSelectedTags) {
        Student existedStudent = (Student) this.loadUserByUsername(updatedStudent.getEmail());

        if (!((existedStudent.getFirstName()).equals(updatedStudent.getFirstName())) ||
                !((existedStudent.getLastName()).equals(updatedStudent.getLastName()))) {
            existedStudent.setFirstName(updatedStudent.getFirstName());
            existedStudent.setLastName(updatedStudent.getLastName());
            existedStudent.setFullName(updatedStudent.getFirstName() + " " + updatedStudent.getLastName());
        }
        if (!((existedStudent.getProfileImagePath()).equals(updatedStudent.getProfileImagePath())))
            existedStudent.setProfileImagePath(updatedStudent.getProfileImagePath());
        if (!((existedStudent.getGithubLink()).equals(updatedStudent.getGithubLink())))
            existedStudent.setGithubLink(updatedStudent.getGithubLink());
        if (!((existedStudent.getAboutUser()).equals(updatedStudent.getAboutUser())))
            existedStudent.setAboutUser(updatedStudent.getAboutUser());
        if (!((existedStudent.getGradesPDFPath()).equals(updatedStudent.getGradesPDFPath())))
            existedStudent.setGradesPDFPath(updatedStudent.getGradesPDFPath());

        if (updatedSelectedTags != null) {
            if (updatedSelectedTags.size() > 5)
                throw new IllegalStateException("You can not select more than five(5) tags");
            for (SelectedStudentTag selectedStudentTag : existedStudent.getSelectedStudentTags())
                if (updatedSelectedTags.contains(selectedStudentTag.getTag().getText()))
                    selectedStudentTag.setSelected(true);
                else
                    selectedStudentTag.setSelected(false);
        }

        userRepository.save(existedStudent);
    }

}
