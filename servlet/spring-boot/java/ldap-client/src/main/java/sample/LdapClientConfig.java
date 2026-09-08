/*
 * Copyright 2005-2024 the original author or authors.
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

package sample;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapClient;

/**
 * Configures an {@link LdapClient} bean, built directly from the {@link ContextSource}
 * that Spring Boot auto-configures. This sample uses only {@link LdapClient} -- never
 * {@code LdapTemplate} -- to show what an {@code LdapClient}-only application looks
 * like.
 */
@Configuration
public class LdapClientConfig {

	private static final Logger log = LoggerFactory.getLogger(LdapClientConfig.class);

	/**
	 * Attributes with no equality matching rule defined in their schema (see
	 * <a href="https://datatracker.ietf.org/doc/html/rfc4519#section-2.10">RFC4519</a>)
	 * must always be replaced in their entirety -- some LDAP servers reject an attempt
	 * to remove individual values from them.
	 */
	private static final Set<String> NO_EQUALITY_MATCHING_RULE = Set.of("facsimileTelephoneNumber");

	@Bean
	LdapClient ldapClient(ContextSource contextSource) {
		return LdapClient.withContextSource(contextSource)
			.dirContextPostProcessor((adapter) -> adapter.setMayRemoveByValue((original, updated) -> {
				boolean mayRemoveByValue = !NO_EQUALITY_MATCHING_RULE.contains(updated.getID());
				log.info("Attribute '{}' may remove individual values: {}", updated.getID(), mayRemoveByValue);
				return mayRemoveByValue;
			}))
			.build();
	}

}
