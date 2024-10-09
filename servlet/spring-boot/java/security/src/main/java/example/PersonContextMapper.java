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

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import example.ldap.DirContextOperationsMapper;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public final class PersonContextMapper implements UserDetailsContextMapper {

	private final LdapClient ldap;

	private final ObjectDirectoryMapper odm = new DefaultObjectDirectoryMapper();

	public PersonContextMapper(LdapClient ldap) {
		this.ldap = ldap;
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
			Collection<? extends GrantedAuthority> authorities) {
		Person person = this.odm.mapFromLdapDataEntry(ctx, Person.class);
		DirContextOperationsMapper<String> toAuthority = (c) -> {
			List<String> members = List.of(c.getStringAttributes("uniqueMember"));
			return (members.contains(ctx.getNameInNamespace())) ? "ROLE_ADMIN" : "ROLE_USER";
		};
		String authority = this.ldap.search().name("cn=managers,ou=groups").toObject(toAuthority);
		return new UserDetailsPerson(person, authority);
	}

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		throw new UnsupportedOperationException("not supported");
	}

	@JsonSerialize(as = Person.class)
	public static class UserDetailsPerson extends Person implements UserDetails {

		Collection<GrantedAuthority> authorities;

		public UserDetailsPerson(Person person, String authority) {
			super(person);
			this.authorities = List.of(new SimpleGrantedAuthority(authority));
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return this.authorities;
		}

		@Override
		public String getPassword() {
			return null;
		}

	}

}
