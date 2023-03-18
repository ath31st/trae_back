/*
 * Copyright (c) 2023. Vladimir Olennikov.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.trae.backend.config;

import static ru.trae.backend.util.Role.ROLE_ADMINISTRATOR;
import static ru.trae.backend.util.Role.ROLE_EMPLOYEE;
import static ru.trae.backend.util.Role.ROLE_MANAGER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import ru.trae.backend.exceptionhandler.RestAccessDeniedHandler;
import ru.trae.backend.exceptionhandler.RestAuthenticationEntryPoint;
import ru.trae.backend.util.jwt.JwtFilter;

/**
 * Security configuration to provide authentication and authorization.
 *
 * @author Vladimir Olennikov
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;
  private final JwtFilter jwtFilter;
  private static final String[] AUTH_WHITELIST = {
      // -- Swagger UI v2
      "/v2/api-docs",
      "/swagger-resources",
      "/swagger-resources/**",
      "/configuration/ui",
      "/configuration/security",
      "/swagger-ui.html",
      "/webjars/**",
      // -- Swagger UI v3 (OpenAPI)
      "/v3/api-docs/**",
      "/swagger-ui/**"
  };

  /**
   * This method is used to create a bean for SecurityFilterChain.
   *
   * @param httpSecurity HttpSecurity object
   * @return SecurityFilterChain object
   * @throws Exception exception
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf().disable()
        //TODO delete CORS after deploying front on server
        .cors().configurationSource(request -> {
          CorsConfiguration configuration = new CorsConfiguration();
          configuration.setAllowedOrigins(List.of("*"));
          configuration.setAllowedMethods(List.of("*"));
          configuration.setAllowedHeaders(List.of("*"));
          return configuration;
        })
        .and()
        .exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint)
        .and()
        .exceptionHandling().accessDeniedHandler(restAccessDeniedHandler)
        .and()
        .authorizeRequests()
        .antMatchers("/api/auth/login", "/api/auth/token").permitAll()
        .antMatchers(AUTH_WHITELIST).permitAll()
        .antMatchers("/api/auth/logout", "/api/auth/refresh").authenticated()

        .antMatchers(
            "/api/manager/change-password",
            "/api/manager/update-data")
        .hasAnyAuthority(ROLE_MANAGER.name(), ROLE_ADMINISTRATOR.name())
        .antMatchers(
            "/api/manager/reset-password",
            "/api/manager/**",
            "/api/manager/change-role-status",
            "/api/manager/managers",
            "/api/manager/register",
            "/api/manager/reset-password",
            "/api/manager/roles")
        .hasAuthority(ROLE_ADMINISTRATOR.name())

        .antMatchers(
            "/api/employee/checkin/**",
            "/api/employee/checkout/**",
            "/api/employee/login/**")
        .hasAuthority(ROLE_EMPLOYEE.name())
        .antMatchers(
            "/api/employee/employees",
            "/api/employee/register")
        .hasAuthority(ROLE_ADMINISTRATOR.name())

        .antMatchers("/api/project/new")
        .hasAnyAuthority(ROLE_MANAGER.name(), ROLE_ADMINISTRATOR.name())

        .anyRequest().authenticated()
        .and()
        .httpBasic().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();
  }
}