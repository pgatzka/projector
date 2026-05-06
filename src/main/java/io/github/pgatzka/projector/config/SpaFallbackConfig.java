package io.github.pgatzka.projector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Forwards unmatched non-API routes to /index.html so the React Router SPA
 * can handle client-side routing. /api/** and /actuator/** keep their normal
 * 404 behavior because they're handled by controllers / actuator before this
 * fallback fires.
 */
@Configuration
public class SpaFallbackConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Single-segment paths (e.g. /projects, /settings)
        registry.addViewController("/{path:^(?!api|actuator|assets|static|v3|swagger-ui|favicon\\.ico|index\\.html).*$}")
                .setViewName("forward:/index.html");
        // Two-or-more-segment paths (e.g. /projects/abc, /issues/abc/edit)
        registry.addViewController("/{path:^(?!api|actuator|assets|static|v3|swagger-ui).*$}/**")
                .setViewName("forward:/index.html");
    }
}
