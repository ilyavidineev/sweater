package ru.racoon.sweater.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.racoon.sweater.domain.Role;
import ru.racoon.sweater.domain.User;
import ru.racoon.sweater.repository.UserRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MailSender mailSender;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    public boolean addUser(User user) {
        User userFromDb = userRepo.findByUsername(user.getUsername());

        if (userFromDb != null) {
            return false;
        }

        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);

        sendMessage(user);

        return true;
    }

    private void sendMessage(User user) {
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String message = String.format(
                    "Hello, %s, \n" +
                            "Welcome to Sweater. Please confirm your Email: http://localhost:8080/activate/%s",
                    //TODO: change activation link to real
                    user.getUsername(),
                    user.getActivationCode()
            );
            mailSender.send(user.getEmail(), "Activation code from Sweater", message);
        }
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);

        if (user == null) return false;
        user.setActivationCode(null);
        userRepo.save(user);
        return true;

    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public void saveUser(User user, String username, Map<String, String> form) {
        user.setUsername(username);
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());
        user.getRoles().clear();
        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
        userRepo.save(user);
    }

    public void updateProfile(User user, String password, String email) {

        String userEmail = user.getEmail();
        boolean isEmailChanged = (email != null && !email.equals(userEmail)) ||
                (userEmail != null && !userEmail.equals(email));
        if (isEmailChanged) {
            user.setEmail(email);
            if (Strings.isNotEmpty(email)) {
                user.setActivationCode(UUID.randomUUID().toString());
            }
        }

        if (Strings.isNotEmpty(password)) {
             user.setPassword(passwordEncoder.encode(password));
        }

        userRepo.save(user);

        if (isEmailChanged) {
            sendMessage(user);
        }
    }

    public void subscribe(User currentUser, User user) {
        user.getSubscribers().add(currentUser);
        userRepo.save(user);
    }

    public void unsubscribe(User currentUser, User user) {
        user.getSubscribers().remove(currentUser);
        userRepo.save(user);
    }
}
