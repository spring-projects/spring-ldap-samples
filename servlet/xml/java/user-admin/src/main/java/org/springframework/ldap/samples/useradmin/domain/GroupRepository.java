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

import java.util.Collection;

import javax.naming.Name;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.data.ldap.repository.Query;

/**
 * Spring Data-generated repository for Group administration. The methods defined in
 * LdapRepository and its superinterfaces directly map mot
 * {@link org.springframework.data.ldap.repository.support.SimpleLdapRepository}.
 *
 * The methods defined in {@link GroupRepositoryExtension} are implemented in the
 * generated instance by 'weaving in' a reference to a bean in the applicationContext
 * implementing the interface.
 *
 * In the {@link #findByName(String)} method, the filter will be automatically be
 * generated based on naming convention; the 'ByName' constraint will be fulfilled using a
 * filter based on the attribute mapping of the name attribute in the target entity class.
 *
 * The {@link #findByMember(javax.naming.Name)} acts on the Query annotation, building an
 * {@link org.springframework.ldap.query.LdapQuery} from the annotation attributes.
 *
 * @author Mattias Hellborg Arthursson
 */
public interface GroupRepository extends LdapRepository<Group>, GroupRepositoryExtension {

	/**
	 * The role to grant to a user.
	 */
	String USER_GROUP = "ROLE_USER";

	Group findByName(String groupName);

	@Query("(member={0})")
	Collection<Group> findByMember(Name member);

}
