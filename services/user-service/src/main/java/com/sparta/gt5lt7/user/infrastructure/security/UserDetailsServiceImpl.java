package com.sparta.gt5lt7.user.infrastructure.security;

import com.sparta.gt5lt7.user.infrastructure.security.UserDetailsImpl;
import com.sparta.gt5lt7.user.domain.entity.User;
import com.sparta.gt5lt7.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );
    }
}