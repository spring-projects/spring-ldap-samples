/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.server.UnboundIdContainer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	static RoleHierarchy roles() {
		return RoleHierarchyImpl.withDefaultRolePrefix().role("ADMIN").implies("USER").build();
	}

	@Bean
	SecurityFilterChain http(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated())
			.httpBasic(withDefaults())
			.csrf((csrf) -> csrf.disable());
		return http.build();
	}

	@Bean
	UnboundIdContainer ldapContainer() {
		UnboundIdContainer container = new UnboundIdContainer("dc=springframework,dc=org", "classpath:users.ldif");
		container.setPort(0);
		return container;
	}

	@Bean
	ContextSource contextSource(UnboundIdContainer container) {
		int port = container.getPort();
		return new DefaultSpringSecurityContextSource("ldap://localhost:" + port + "/dc=springframework,dc=org");
	}

	@Bean
	BindAuthenticator authenticator(BaseLdapPathContextSource contextSource) {
		BindAuthenticator authenticator = new BindAuthenticator(contextSource);
		authenticator.setUserDnPatterns(new String[] { "uid={0},ou=people" });
		return authenticator;
	}

	@Bean
	LdapAuthenticationProvider authenticationProvider(LdapAuthenticator authenticator, LdapClient ldap) {
		LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator);
		provider.setUserDetailsContextMapper(new PersonContextMapper(ldap));
		return provider;
	}

	@Bean
	LdapClient ldapClient(ContextSource contextSource) {
		return LdapClient.create(contextSource);
	}

}
