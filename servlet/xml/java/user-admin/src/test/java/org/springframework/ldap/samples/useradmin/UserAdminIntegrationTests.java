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

package org.springframework.ldap.samples.useradmin;

import java.util.List;
import java.util.Map;

import javax.naming.Name;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.samples.useradmin.domain.DepartmentRepository;
import org.springframework.ldap.samples.useradmin.domain.Group;
import org.springframework.ldap.samples.useradmin.domain.GroupRepository;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("/applicationContext.xml")
public class UserAdminIntegrationTests {

	@Autowired
	private GroupRepository groups;

	@Autowired
	private DepartmentRepository departments;

	@Test
	void createIncludesSystemMember() {
		Group group = new Group();
		group.setName("Empty");
		group.setDescription("No Members");
		this.groups.create(group);
		Name systemDn = LdapUtils.newLdapName("cn=System,ou=System,ou=IT,ou=Departments,dc=example,dc=com");
		Group created = this.groups.findByName("Empty");
		assertThat(created.getMembers()).isNotEmpty();
		assertThat(created.getMembers()).contains(systemDn);
	}

	@Test
	void getAllDepartmentsReturnsAllDepartments() {
		Map<String, List<String>> departments = this.departments.getDepartmentMap();
		assertThat(departments).hasSize(2);
		assertThat(departments.get("Accounting")).hasSize(1);
		assertThat(departments.get("IT")).hasSize(4);
	}

}
