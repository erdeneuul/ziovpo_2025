package ru.mfa.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MeResponse {
    private String username;
    private List<String> roles;

    public MeResponse(String username, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
}
