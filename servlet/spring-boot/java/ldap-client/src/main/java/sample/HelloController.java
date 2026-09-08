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

import java.util.Arrays;
import java.util.List;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapClient;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	private final LdapClient ldapClient;

	public HelloController(LdapClient ldapClient) {
		this.ldapClient = ldapClient;
	}

	@GetMapping
	public String hello(Authentication authentication) {
		return "Hello, " + authentication.getName();
	}

	@GetMapping("/cn")
	public List<String> cn(Authentication authentication) {
		AttributesMapper<String> mapper = (attrs) -> attrs.get("cn").get().toString();
		return this.ldapClient.search()
			.query((query) -> query.base("ou=people").where("uid").is(authentication.getName()))
			.map(mapper)
			.list();
	}

	@GetMapping("/description")
	public String description(Authentication authentication) {
		return lookup(authentication).getStringAttribute("description");
	}

	@PostMapping("/description")
	public void updateDescription(Authentication authentication, @RequestBody String description) {
		DirContextAdapter entry = lookup(authentication);
		entry.setAttributeValue("description", description);
		this.ldapClient.modify(entry.getDn()).attributes(entry.getModificationItems()).execute();
	}

	/**
	 * {@code facsimileTelephoneNumber} has no equality matching rule in its schema
	 * (see RFC4519), so removing just one of several values is not something every LDAP
	 * server accepts. {@link LdapClientConfig} registers a {@code dirContextPostProcessor}
	 * that forces this attribute to always be replaced in its entirety instead, via
	 * {@code DirContextAdapter#setMayRemoveByValue}.
	 */
	@GetMapping("/fax")
	public List<String> fax(Authentication authentication) {
		return Arrays.asList(lookup(authentication).getStringAttributes("facsimileTelephoneNumber"));
	}

	@PostMapping("/fax")
	public void updateFax(Authentication authentication, @RequestBody String numbers) {
		DirContextAdapter entry = lookup(authentication);
		entry.setAttributeValues("facsimileTelephoneNumber", numbers.split(","));
		this.ldapClient.modify(entry.getDn()).attributes(entry.getModificationItems()).execute();
	}

	private DirContextAdapter lookup(Authentication authentication) {
		return this.ldapClient.search()
			.query((query) -> query.base("ou=people").where("uid").is(authentication.getName()))
			.single();
	}

}
