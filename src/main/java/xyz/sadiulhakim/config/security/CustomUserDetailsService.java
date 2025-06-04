package xyz.sadiulhakim.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.sadiulhakim.user.User;
import xyz.sadiulhakim.user.UserService;

@Service
@RequiredArgsConstructor
class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByEmail(username);
        if (user == null)
            return null;

        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getRole(),
                user.getLastname(), user.getPicture());
    }
}
