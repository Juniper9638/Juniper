package juniper.local.juniper.filter;

import juniper.local.juniper.security.JwtAuthToken;
import juniper.local.juniper.security.JwtAuthTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "x-auth-token";
    private JwtAuthTokenProvider tokenProvider;

    public JwtFilter(JwtAuthTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = resolveToken(httpServletRequest);

        if (token.isPresent()) {
            JwtAuthToken jwtAuthToken = tokenProvider.convertAuthToken(token.get());

            if (jwtAuthToken.validate()) {
                Authentication authentication = tokenProvider.getAuthentication(jwtAuthToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        String authToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authToken))
            return Optional.of(authToken);
        else
            return Optional.empty();
    }
}
