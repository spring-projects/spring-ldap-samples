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

import java.io.IOException;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for "/".
 *
 * @author Josh Cummings
 */
@RestController
@RequestMapping("/people")
@AuthorizeReturnObject
public class PersonController {

	private final PersonRepository persons;

	private final ObjectMapper mapper;

	private final LdapClient ldap;

	public PersonController(PersonRepository persons, ObjectMapper mapper, LdapClient ldap) {
		this.persons = persons;
		this.mapper = mapper;
		this.ldap = ldap;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public Iterable<Person> list() {
		Iterable<Person> people = this.persons.findAll();
		return people;
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public Person me(@AuthenticationPrincipal Person person) {
		return person;
	}

	@GetMapping("/{uid}")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public Person get(@PathVariable("uid") Name uid) {
		return this.persons.findByDn(uid);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Person create(@RequestBody Person person) {
		return this.persons.save(person);
	}

	@PutMapping("/{uid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Person update(@PathVariable("uid") Name uid, @RequestBody String person) throws IOException {
		Person toUpdate = this.persons.findByDn(uid);
		this.mapper.readerForUpdating(toUpdate).readValue(person, Person.class);
		toUpdate.setDn(uid);
		return this.persons.save(toUpdate);
	}

	@PutMapping("/{uid}/password")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public void updatePassword(@PathVariable("uid") Name uid, @RequestBody Attributes attributes) {
		Attribute password = attributes.get("userPassword");
		this.ldap.modify(uid)
			.attributes(new ModificationItem(DirContextOperations.REPLACE_ATTRIBUTE, password))
			.execute();
	}

	@DeleteMapping("/{uid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public void delete(@PathVariable("uid") Name uid) {
		this.persons.deleteById(uid);
	}

}
