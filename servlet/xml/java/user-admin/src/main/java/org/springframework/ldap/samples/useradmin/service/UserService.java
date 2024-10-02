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

package org.springframework.ldap.samples.useradmin.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.samples.useradmin.domain.DirectoryType;
import org.springframework.ldap.samples.useradmin.domain.Group;
import org.springframework.ldap.samples.useradmin.domain.GroupRepository;
import org.springframework.ldap.samples.useradmin.domain.User;
import org.springframework.ldap.samples.useradmin.domain.UserRepository;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;

/**
 * The service layer for user administration.
 *
 * @author Mattias Hellborg Arthursson
 */
public class UserService implements BaseLdapNameAware {

	private final UserRepository users;

	private final GroupRepository groups;

	private LdapName baseLdapPath;

	private DirectoryType directoryType;

	@Autowired
	public UserService(UserRepository users, GroupRepository groups) {
		this.users = users;
		this.groups = groups;
	}

	public Group getUserGroup() {
		return this.groups.findByName(GroupRepository.USER_GROUP);
	}

	public void setDirectoryType(DirectoryType directoryType) {
		this.directoryType = directoryType;
	}

	@Override
	public void setBaseLdapPath(LdapName baseLdapPath) {
		this.baseLdapPath = baseLdapPath;
	}

	public Iterable<User> findAll() {
		return this.users.findAll();
	}

	public User findUser(String userId) {
		return this.users.findById(LdapUtils.newLdapName(userId))
			.orElseThrow(() -> new NameNotFoundException("user not found"));
	}

	public User createUser(User user) {
		User savedUser = this.users.save(user);

		Group userGroup = getUserGroup();

		// The DN the member attribute must be absolute
		userGroup.addMember(toAbsoluteDn(savedUser.getId()));
		this.groups.save(userGroup);

		return savedUser;
	}

	public LdapName toAbsoluteDn(Name relativeName) {
		return LdapNameBuilder.newInstance(this.baseLdapPath).add(relativeName).build();
	}

	/**
	 * This method expects absolute DNs of group members. In order to find the actual
	 * users the DNs need to have the base LDAP path removed.
	 * @param absoluteIds the DNs for the set of members to lookup
	 * @return the found members
	 */
	public Set<User> findAllMembers(Iterable<Name> absoluteIds) {
		return new LinkedHashSet<>(this.users.findAllById(toRelativeIds(absoluteIds)));
	}

	public Iterable<Name> toRelativeIds(Iterable<Name> absoluteIds) {
		return StreamSupport.stream(absoluteIds.spliterator(), false)
			.map((input) -> (Name) LdapUtils.removeFirst(input, UserService.this.baseLdapPath))
			.toList();
	}

	public User updateUser(String userId, User user) {
		LdapName originalId = LdapUtils.newLdapName(userId);
		User existingUser = findUser(userId);

		existingUser.setFirstName(user.getFirstName());
		existingUser.setLastName(user.getLastName());
		existingUser.setFullName(user.getFullName());
		existingUser.setEmail(user.getEmail());
		existingUser.setPhone(user.getPhone());
		existingUser.setTitle(user.getTitle());
		existingUser.setDepartment(user.getDepartment());
		existingUser.setUnit(user.getUnit());

		if (this.directoryType == DirectoryType.AD) {
			return updateUserAd(originalId, existingUser);
		}
		else {
			return updateUserStandard(originalId, existingUser);
		}
	}

	/**
	 * Update the user and - if its id changed - update all group references to the user.
	 * @param originalId the original id of the user.
	 * @param existingUser the user, populated with new data
	 * @return the updated entry
	 */
	private User updateUserStandard(LdapName originalId, User existingUser) {
		User savedUser = this.users.save(existingUser);

		if (!originalId.equals(savedUser.getId())) {
			// The user has moved - we need to update group references.
			LdapName oldMemberDn = toAbsoluteDn(originalId);
			LdapName newMemberDn = toAbsoluteDn(savedUser.getId());

			Collection<Group> groups = this.groups.findByMember(oldMemberDn);
			updateGroupReferences(groups, oldMemberDn, newMemberDn);
		}
		return savedUser;
	}

	/**
	 * Special behaviour in AD forces us to get the group membership before the user is
	 * updated, because AD clears group membership for removed entries, which means that
	 * once the user is update we've lost track of which groups the user was originally
	 * member of, preventing us to update the membership references so that they point to
	 * the new DN of the user.
	 *
	 * This is slightly less efficient, since we need to get the group membership for all
	 * updates even though the user may not have been moved. Using our knowledge of which
	 * attributes are part of the distinguished name we can do this more efficiently if we
	 * are implementing specifically for Active Directory - this approach is just to
	 * highlight this quite significant difference.
	 * @param originalId the original id of the user.
	 * @param existingUser the user, populated with new data
	 * @return the updated entry
	 */
	private User updateUserAd(LdapName originalId, User existingUser) {
		LdapName oldMemberDn = toAbsoluteDn(originalId);
		Collection<Group> groups = this.groups.findByMember(oldMemberDn);

		User savedUser = this.users.save(existingUser);
		LdapName newMemberDn = toAbsoluteDn(savedUser.getId());

		if (!originalId.equals(savedUser.getId())) {
			// The user has moved - we need to update group references.
			updateGroupReferences(groups, oldMemberDn, newMemberDn);
		}
		return savedUser;
	}

	private void updateGroupReferences(Collection<Group> groups, Name originalId, Name newId) {
		for (Group group : groups) {
			group.removeMember(originalId);
			group.addMember(newId);

			this.groups.save(group);
		}
	}

	public List<User> searchByNameName(String lastName) {
		return this.users.findByFullNameContains(lastName);
	}

}
