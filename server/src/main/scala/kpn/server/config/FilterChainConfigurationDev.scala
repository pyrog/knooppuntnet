package kpn.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@Profile(Array("dev"))
class FilterChainConfigurationDev {

  @Bean
  def filterChain(http: HttpSecurity): SecurityFilterChain = {
    http
      .authorizeHttpRequests(authorizeRequests =>
        authorizeRequests.anyRequest().permitAll()
      )
      //.csrf(csrf => csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse))
      .csrf(csrf => csrf.disable)
    http.build()
  }
}
