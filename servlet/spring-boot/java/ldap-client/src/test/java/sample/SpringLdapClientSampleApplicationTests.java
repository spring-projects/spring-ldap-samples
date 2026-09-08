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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringLdapClientSampleApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	void indexWhenCorrectUsernameAndPasswordThenAuthenticates() throws Exception {
		HttpHeaders http = new HttpHeaders();
		http.setBasicAuth("bob", "bobspassword");
		this.mvc.perform(get("/").headers(http)).andExpect(status().isOk()).andExpect(content().string("Hello, bob"));
	}

	@Test
	void cnWhenCorrectUsernameAndPasswordThenShowsCommonName() throws Exception {
		HttpHeaders http = new HttpHeaders();
		http.setBasicAuth("bob", "bobspassword");
		this.mvc.perform(get("/cn").headers(http))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.[0]").value("Bob Hamilton"));
	}

	@Test
	void updateDescriptionThenGetDescriptionReflectsChange() throws Exception {
		HttpHeaders http = new HttpHeaders();
		http.setBasicAuth("bob", "bobspassword");
		this.mvc.perform(post("/description").headers(http).with(csrf()).content("Updated by LdapClient"))
			.andExpect(status().isOk());
		this.mvc.perform(get("/description").headers(http))
			.andExpect(status().isOk())
			.andExpect(content().string("Updated by LdapClient"));
	}

	@Test
	void faxWhenCorrectUsernameAndPasswordThenShowsBothNumbers() throws Exception {
		HttpHeaders http = new HttpHeaders();
		http.setBasicAuth("bob", "bobspassword");
		this.mvc.perform(get("/fax").headers(http))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$", hasItems("+1 555 1234567", "+1 555 7654321")));
	}

	@Test
	void updatingOneOfTwoFaxNumbersThenGetFaxReflectsChange() throws Exception {
		HttpHeaders http = new HttpHeaders();
		http.setBasicAuth("bob", "bobspassword");
		// removes one of the two original numbers and adds a new one -- exactly the
		// partial multi-value change that would otherwise attempt a by-value removal
		this.mvc.perform(post("/fax").headers(http).with(csrf()).content("+1 555 1234567,+1 555 9999999"))
			.andExpect(status().isOk());
		this.mvc.perform(get("/fax").headers(http))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$", hasItems("+1 555 1234567", "+1 555 9999999")));
	}

}
