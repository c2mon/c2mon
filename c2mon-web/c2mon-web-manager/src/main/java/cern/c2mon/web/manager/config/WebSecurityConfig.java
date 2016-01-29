package cern.c2mon.web.manager.config;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/static/**").antMatchers("/css/**").antMatchers("/js/**").antMatchers("/img/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers("/").permitAll()
        .antMatchers("/login").permitAll()
        .antMatchers("/configloader/progress").hasRole("ADMIN")
        .antMatchers("/process/**").hasRole("ADMIN")
        .antMatchers("/commandviewer/**").hasRole("ADMIN")
        .anyRequest().anonymous()
        .and()
        .formLogin()
        .loginPage("/login")
        .failureUrl("/login?error=true")
        .permitAll()
        .and()
        .csrf().disable();
  }

//  @Bean
//  public EmbeddedServletContainerCustomizer containerCustomizer() {
//    return new EmbeddedServletContainerCustomizer() {
//      @Override
//      public void customize(ConfigurableEmbeddedServletContainer container) {
//        container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error/404"));
//      }
//    };
//  }
}
