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

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.ldap.server.UnboundIdContainer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Rob Winch
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	UnboundIdContainer container;

	@BeforeEach
	void setup() {
		this.container.start();
	}

	@AfterEach
	void teardown() {
		this.container.stop();
	}

	@Test
	@WithMockUser
	void authenticatedPersonsThenOk() throws Exception {
		// @formatter:off
		String json = this.mockMvc.perform(get("/people"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		List<Person> people = this.mapper.readValue(json, new TypeReference<>() { });
		assertThat(people).hasSize(4)
			.extracting(Person::getUsername).containsExactlyInAnyOrder("user", "may", "hal", "dante");
		assertThat(people)
			.extracting(Person::getName).containsOnlyNulls();
		// @formatter:on
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminAuthenticatedPersonsThenOk() throws Exception {
		// @formatter:off
		String json = this.mockMvc.perform(get("/people"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		List<Person> people = this.mapper.readValue(json, new TypeReference<>() { });
		assertThat(people).hasSize(4)
			.extracting(Person::getUsername).containsExactlyInAnyOrder("user", "may", "hal", "dante");
		assertThat(people)
			.extracting(Person::getName).containsExactlyInAnyOrder(
				"User User", "May Bea", "Hal 2000", "Dante Alvarez");
		// @formatter:on
	}

	@Test
	void basicAuthenticatedPersonsThenOk() throws Exception {
		// @formatter:off
		String json = this.mockMvc.perform(get("/people").with(httpBasic("dante", "secret")))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		List<Person> people = this.mapper.readValue(json, new TypeReference<>() { });
		assertThat(people).hasSize(4)
			.extracting(Person::getUsername).containsExactlyInAnyOrder("user", "may", "hal", "dante");
		assertThat(people)
			.extracting(Person::getName).containsOnlyNulls();
		// @formatter:on
	}

	@Test
	void basicAdminAuthenticatedPersonsThenOk() throws Exception {
		// @formatter:off
		String json = this.mockMvc.perform(get("/people").with(httpBasic("may", "later")))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		List<Person> people = this.mapper.readValue(json, new TypeReference<>() { });
		assertThat(people).hasSize(4)
			.extracting(Person::getUsername).containsExactlyInAnyOrder("user", "may", "hal", "dante");
		assertThat(people)
			.extracting(Person::getName).containsExactlyInAnyOrder(
				"User User", "May Bea", "Hal 2000", "Dante Alvarez");
		// @formatter:on
	}

	@Test
	void meThenCurrentPerson() throws Exception {
		String json = this.mockMvc.perform(get("/people/me").with(httpBasic("user", "password")))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Person person = this.mapper.readValue(json, Person.class);
		assertThat(person.getUsername()).isEqualTo("user");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void partiallyUpdatePersonThenUpdated() throws Exception {
		Person person = new Person("dante");
		person.setDn(LdapUtils.newLdapName("uid=dante,ou=people"));
		person.setUsername("ari");
		String json = this.mapper.writeValueAsString(person);
		String updated = this.mockMvc
			.perform(put("/people/" + person.getDn()).content(json).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		person = this.mapper.readValue(updated, Person.class);
		assertThat(person.getUsername()).isEqualTo("ari");
		assertThat(person.getDn().toString()).isEqualTo("uid=ari,ou=people");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deletePersonThenRemoved() throws Exception {
		String dn = "uid=hal,ou=people";
		this.mockMvc.perform(delete("/people/" + dn)).andExpect(status().isOk());
		this.mockMvc.perform(get("/people/" + dn)).andExpect(status().isNotFound());
	}

}
