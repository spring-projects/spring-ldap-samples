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

package org.springframework.ldap.samples.odm.repository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.samples.odm.domain.Person;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Abstract base class for PersonDao integration tests.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("/applicationContext.xml")
public class PersonRepositorySampleIntegrationTests {

	protected Person person;

	@Autowired
	private PersonRepository personRepository;

	@BeforeEach
	void preparePerson() {
		this.person = new Person();
		this.person.setCountry("Sweden");
		this.person.setCompany("company1");
		this.person.setFullName("Some Person");
		this.person.setLastName("Person");
		this.person.setDescription("Sweden, Company1, Some Person");
		this.person.setPhone("+46 555-123456");
	}

	/**
	 * Having a single test method test create, update and delete is not exactly the ideal
	 * way of testing, since they depend on each other. A better way would be to separate
	 * the tests and load a test fixture before each operation, in order to guarantee the
	 * expected state every time. See the ldaptemplate-person sample for the correct way
	 * to do this.
	 */
	@Test
	void testCreateUpdateDelete() {
		this.person.setFullName("Another Person");
		this.personRepository.save(this.person);
		Person person = this.personRepository.findByPrimaryKey("Sweden", "company1", "Another Person");
		assertThat(person).isEqualTo(this.person);

		this.person.setDescription("Another description");
		this.personRepository.save(this.person);
		person = this.personRepository.findByPrimaryKey("Sweden", "company1", "Another Person");
		assertThat(person.getDescription()).isEqualTo("Another description");

		this.personRepository.delete(this.person);
		assertThatExceptionOfType(NameNotFoundException.class)
			.isThrownBy(() -> this.personRepository.findByPrimaryKey("Sweden", "company1", "Another Person"));
	}

	@Test
	void testGetAllPersonNames() {
		List<String> result = this.personRepository.getAllPersonNames();
		assertThat(result).hasSize(2);
		String first = result.get(0);
		assertThat(first).isEqualTo("Some Person");
	}

	@Test
	void testFindAll() {
		List<Person> result = this.personRepository.findAll();
		assertThat(result).hasSize(2);
		Person first = result.get(0);
		assertThat(first.getFullName()).isEqualTo("Some Person");
	}

	@Test
	void testFindByPrimaryKey() {
		Person result = this.personRepository.findByPrimaryKey("Sweden", "company1", "Some Person");

		assertThat(result.getCountry()).isEqualTo("Sweden");
		assertThat(result.getCompany()).isEqualTo("company1");
		assertThat(result.getDescription()).isEqualTo("Sweden, Company1, Some Person");
		assertThat(result.getPhone()).isEqualTo("+46 555-123456");
		assertThat(result.getFullName()).isEqualTo("Some Person");
		assertThat(result.getLastName()).isEqualTo("Person");
	}

}
