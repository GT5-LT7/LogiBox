package com.spatra.gt5lt7.user.infrastructure.security;

import com.spatra.gt5lt7.common.exception.BaseException;
import com.spatra.gt5lt7.common.security.UserDetailsImpl;
import com.spatra.gt5lt7.user.domain.entity.User;
import com.spatra.gt5lt7.user.domain.repository.UserRepository;
import com.spatra.gt5lt7.user.global.exception.UserErrorCode;
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
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().name()
        );
    }
}