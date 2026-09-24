package com.aiknow.user;

import com.aiknow.audit.AuditEventPublisher;
import com.aiknow.audit.AuditEventType;
import com.aiknow.common.ApiResponse;
import com.aiknow.organization.Organization;
import com.aiknow.organization.OrganizationMember;
import com.aiknow.organization.OrganizationMemberRepository;
import com.aiknow.organization.OrganizationMemberRole;
import com.aiknow.organization.OrganizationRepository;
import com.aiknow.security.JwtTokenProvider;
import com.aiknow.security.UserDetailsImpl;
import com.aiknow.user.dto.AuthRequest;
import com.aiknow.user.dto.AuthResponse;
import com.aiknow.user.dto.RefreshTokenRequest;
import com.aiknow.user.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final AuditEventPublisher auditEventPublisher;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());
        User user = userService.createUser(request.email(), request.password(), request.fullName(), UserRole.OWNER);

        Organization organization = new Organization();
        organization.setName(request.organizationName());
        organization.setSlug(generateSlug(request.organizationName()));
        organization = organizationRepository.save(organization);

        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(organization.getId());
        member.setUserId(user.getId());
        member.setRole(OrganizationMemberRole.OWNER);
        member.setJoinedAt(LocalDateTime.now());
        organizationMemberRepository.save(member);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();

        auditEventPublisher.publish(AuditEventType.USER_REGISTERED, organization.getId(), null, user.getId(),
                "USER", user.getId(), Map.of("email", user.getEmail()));

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        log.info("Logging in user with email: {}", request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userService.findById(userDetails.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();

        auditEventPublisher.publish(AuditEventType.USER_LOGIN, null, null, user.getId(), "USER", user.getId(), null);

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refreshing token");
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.refreshToken());
        User user = refreshToken.getUser();
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logging out user");
        refreshTokenService.revokeRefreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
