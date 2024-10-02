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

package org.springframework.ldap.samples.useradmin.domain.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameClassPair;
import javax.naming.ldap.LdapName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.core.NameClassPairMapper;
import org.springframework.ldap.samples.useradmin.domain.DepartmentRepository;
import org.springframework.ldap.samples.useradmin.domain.UserRepository;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;

/**
 * An inline implementation of {@link DepartmentRepository}, drawing a comparison between
 * this and what Spring Data LDAP does for you {@link UserRepository}.
 *
 * @author Mattias Hellborg Arthursson
 */
public class DepartmentRepositoryImpl implements DepartmentRepository {

	private static final LdapName DEPARTMENTS_OU = LdapUtils.newLdapName("ou=Departments");

	private final LdapClient ldap;

	@Autowired
	public DepartmentRepositoryImpl(LdapClient ldap) {
		this.ldap = ldap;
	}

	@Override
	public Map<String, List<String>> getDepartmentMap() {
		return new HashMap<>() {
			{
				List<String> allDepartments = getAllDepartments();
				for (String oneDepartment : allDepartments) {
					put(oneDepartment, getAllUnitsForDepartment(oneDepartment));
				}
			}
		};
	}

	private List<String> getAllDepartments() {
		return this.ldap.list(DEPARTMENTS_OU).toList(new OuValueNameClassPairMapper());
	}

	private List<String> getAllUnitsForDepartment(String department) {
		return this.ldap.list(LdapNameBuilder.newInstance(DEPARTMENTS_OU).add("ou", department).build())
			.toList(new OuValueNameClassPairMapper());
	}

	private static class OuValueNameClassPairMapper implements NameClassPairMapper<String> {

		@Override
		public String mapFromNameClassPair(NameClassPair nameClassPair) {
			LdapName name = LdapUtils.newLdapName(nameClassPair.getName());
			return LdapUtils.getStringValue(name, "ou");
		}

	}

}
