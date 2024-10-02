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

package org.springframework.ldap.samples.useradmin.domain;

import java.util.Objects;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapUtils;

/**
 * The user entry descriptor.
 *
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" }, base = "ou=Departments")
public final class User {

	@Id
	private Name id;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 3)
	private String fullName;

	@Attribute(name = "employeeNumber")
	private int employeeNumber;

	@Attribute(name = "givenName")
	private String firstName;

	@Attribute(name = "sn")
	private String lastName;

	@Attribute(name = "title")
	private String title;

	@Attribute(name = "mail")
	private String email;

	@Attribute(name = "telephoneNumber")
	private String phone;

	@DnAttribute(value = "ou", index = 2)
	@Transient
	private String unit;

	@DnAttribute(value = "ou", index = 1)
	@Transient
	private String department;

	public String getUnit() {
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDepartment() {
		return this.department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Name getId() {
		return this.id;
	}

	public void setId(Name id) {
		this.id = id;
	}

	public void setId(String id) {
		this.id = LdapUtils.newLdapName(id);
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getEmployeeNumber() {
		return this.employeeNumber;
	}

	public void setEmployeeNumber(int employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		User user = (User) o;

		return Objects.equals(this.id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}

}
