package com.evggenn.edugo.user;

import com.evggenn.edugo.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepo.findByEmailWithRoles(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return new CustomUserDetails(user);
    }
}
