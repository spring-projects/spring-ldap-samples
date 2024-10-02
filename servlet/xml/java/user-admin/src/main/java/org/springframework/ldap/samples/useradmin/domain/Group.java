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

import java.util.HashSet;
import java.util.Set;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * The group entry descriptor.
 *
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = { "groupOfNames", "top" }, base = "ou=Groups")
public final class Group {

	@Id
	private Name id;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 1)
	private String name;

	@Attribute(name = "description")
	private String description;

	@Attribute(name = "member")
	private final Set<Name> members = new HashSet<>();

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Name> getMembers() {
		return this.members;
	}

	public void addMember(Name newMember) {
		this.members.add(newMember);
	}

	public void removeMember(Name member) {
		this.members.remove(member);
	}

	public Name getId() {
		return this.id;
	}

	public void setId(Name id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
