package kpn.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutFilter

@Configuration
@Profile(Array("!dev"))
class FilterChainConfiguration(
  authenticationReturnUrlFilter: AuthenticationReturnUrlFilter,
  authenticationCookieFilter: AuthenticationCookieFilter,
  authenticationSuccessHandler: CustomAuthenticationSuccessHandler,
  authenticationFailureHandler: CustomAuthenticationFailureHandler,
  authorizationRequestResolver: CustomAuthorizationRequestResolver,
  userService: UserService,
  testEnabled: Boolean
) {

  @Bean
  def filterChain(http: HttpSecurity): SecurityFilterChain = {
    http
      .addFilterAfter(authenticationReturnUrlFilter, classOf[LogoutFilter])
      .addFilterAfter(authenticationCookieFilter, classOf[AuthenticationReturnUrlFilter])
      .addFilterAfter(new RequestContextFilter(testEnabled), classOf[AuthenticationCookieFilter])
      .authorizeHttpRequests(authorizeRequests =>
        authorizeRequests.anyRequest().permitAll()
      )
      //.csrf(csrf => csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse))
      .csrf(csrf => csrf.disable)
      .oauth2Login(oauth2Login =>
        oauth2Login
          .loginProcessingUrl("/api/oauth2/code/*")
          .successHandler(authenticationSuccessHandler)
          .failureHandler(authenticationFailureHandler)
          .userInfoEndpoint(userInfoEndpoint =>
            userInfoEndpoint.userService(userService)
          )
          .authorizationEndpoint(authorizationEndpoint =>
            authorizationEndpoint.authorizationRequestResolver(authorizationRequestResolver)
          )
      )

    http.build()
  }
}
