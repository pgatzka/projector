package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.audit.AccountPrincipal;
import io.github.pgatzka.projector.data.service.AccountDataService;
import io.github.pgatzka.projector.rest.dto.LoginRequest;
import io.github.pgatzka.projector.rest.dto.MeResponse;
import io.github.pgatzka.projector.rest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final AccountDataService accountDataService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthService authService, AccountDataService accountDataService) {
        this.authService = authService;
        this.accountDataService = accountDataService;
    }

    @PostMapping("/login")
    public MeResponse login(@Valid @RequestBody LoginRequest request,
                            HttpServletRequest httpRequest,
                            HttpServletResponse httpResponse) {
        AccountPrincipal principal = authService.authenticate(request.email(), request.password());
        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal, null, AuthorityUtils.createAuthorityList("ROLE_USER")
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return accountDataService.findByEmail(principal.email())
            .map(MeResponse::of)
            .orElseThrow();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AccountPrincipal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return accountDataService.findByEmail(principal.email())
            .map(MeResponse::of)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
