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

import javax.naming.Name;

import example.security.NullMethodAuthorizationDeniedHandler;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;

@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" }, base = "ou=people")
public class Person {

	@Id
	private Name dn;

	@DnAttribute(value = "uid", index = 1)
	@Attribute(name = "uid")
	private String username;

	@Attribute(name = "cn")
	private String name;

	@Attribute(name = "sn")
	private String lastName;

	public Person() {

	}

	public Person(String username) {
		this.username = username;
	}

	public Person(Person person) {
		this.dn = person.getDn();
		this.username = person.getUsername();
		this.name = person.getName();
		this.lastName = person.getLastName();
	}

	public Name getDn() {
		return this.dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@HandleAuthorizationDenied(handlerClass = NullMethodAuthorizationDeniedHandler.class)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
