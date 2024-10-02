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

import org.springframework.LdapDataEntry;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.samples.odm.domain.Person;

/**
 * An implementation of {@link PersonRepositoryExtension}.
 *
 * This extension uses ODM to calculate the DN so that it can be looked up.
 *
 * @author Josh Cummings
 */
public class PersonRepositoryExtensionImpl implements PersonRepositoryExtension {

	private final LdapClient ldap;

	private final ObjectDirectoryMapper odm;

	public PersonRepositoryExtensionImpl(LdapClient ldap, ObjectDirectoryMapper odm) {
		this.ldap = ldap;
		this.odm = odm;
	}

	@Override
	public Person findByPrimaryKey(String country, String company, String fullname) {
		Person person = new Person(country, company, fullname);
		return this.ldap.search().name(this.odm.getCalculatedId(person)).toObject((ContextMapper<Person>) (ctx) -> {
			LdapDataEntry entry = (LdapDataEntry) ctx;
			return this.odm.mapFromLdapDataEntry(entry, Person.class);
		});
	}

}
