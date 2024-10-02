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

import java.util.List;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.springframework.LdapDataEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.samples.useradmin.domain.Group;
import org.springframework.ldap.samples.useradmin.domain.GroupRepositoryExtension;
import org.springframework.ldap.support.LdapUtils;

/**
 * A set of methods that extend the {@link Group} Spring Data repository.
 *
 * @author Mattias Hellborg Arthursson
 */
public class GroupRepositoryImpl implements GroupRepositoryExtension, BaseLdapNameAware {

	private static final LdapName ADMIN_USER = LdapUtils.newLdapName("cn=System,ou=System,ou=IT,ou=Departments");

	private final LdapClient ldap;

	private final ObjectDirectoryMapper mapper;

	private LdapName baseLdapPath;

	@Autowired
	public GroupRepositoryImpl(LdapClient ldap, ObjectDirectoryMapper mapper) {
		this.ldap = ldap;
		this.mapper = mapper;
	}

	@Override
	public void setBaseLdapPath(LdapName baseLdapPath) {
		this.baseLdapPath = baseLdapPath;
	}

	@Override
	public List<String> getAllGroupNames() {
		LdapQuery query = LdapQueryBuilder.query().attributes("cn").where("objectclass").is("groupOfNames");

		return this.ldap.search()
			.query(query)
			.toList((AttributesMapper<String>) (attributes) -> (String) attributes.get("cn").get());
	}

	@Override
	public void create(Group group) {
		// A groupOfNames cannot be empty - add a system entry to all new groups.
		group.addMember(LdapUtils.prepend(ADMIN_USER, this.baseLdapPath));
		Name targetId = this.mapper.getCalculatedId(group);
		LdapDataEntry entry = new DirContextAdapter(targetId);
		this.mapper.mapToLdapDataEntry(group, entry);
		this.ldap.bind(targetId).object(entry).execute();
	}

}
