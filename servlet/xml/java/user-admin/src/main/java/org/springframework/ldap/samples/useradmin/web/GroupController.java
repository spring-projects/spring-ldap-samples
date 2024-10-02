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

package org.springframework.ldap.samples.useradmin.web;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.springframework.ldap.samples.useradmin.domain.Group;
import org.springframework.ldap.samples.useradmin.domain.GroupRepository;
import org.springframework.ldap.samples.useradmin.domain.User;
import org.springframework.ldap.samples.useradmin.service.UserService;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The endpoints for administering groups.
 *
 * @author Mattias Hellborg Arthursson
 */
@Controller
public class GroupController {

	private final GroupRepository groups;

	private final UserService userService;

	public GroupController(GroupRepository groups, UserService userService) {
		this.groups = groups;
		this.userService = userService;
	}

	@GetMapping("/groups")
	public String listGroups(ModelMap map) {
		map.put("groups", this.groups.getAllGroupNames());
		return "listGroups";
	}

	@GetMapping("/newGroup")
	public String initNewGroup() {
		return "newGroup";
	}

	@PostMapping("/groups")
	public String newGroup(Group group) {
		this.groups.create(group);

		return "redirect:groups/" + group.getName();
	}

	@GetMapping("/groups/{name}")
	public String editGroup(@PathVariable("name") String name, ModelMap map) {
		Group foundGroup = this.groups.findByName(name);
		map.put("group", foundGroup);

		final Set<User> groupMembers = this.userService.findAllMembers(foundGroup.getMembers());
		map.put("members", groupMembers);

		Iterable<User> all = this.userService.findAll();
		List<User> otherUsers = StreamSupport.stream(all.spliterator(), false)
			.filter((user) -> !groupMembers.contains(user))
			.toList();
		map.put("nonMembers", new LinkedList<>(otherUsers));

		return "editGroup";
	}

	@PostMapping("/groups/{name}/members")
	public String addUserToGroup(@PathVariable("name") String name, @RequestParam("userId") String userId) {
		Group group = this.groups.findByName(name);
		group.addMember(this.userService.toAbsoluteDn(LdapUtils.newLdapName(userId)));

		this.groups.save(group);

		return "redirect:/groups/" + name;
	}

	@DeleteMapping("/groups/{name}/members")
	public String removeUserFromGroup(@PathVariable("name") String name, @RequestParam("userId") String userId) {
		Group group = this.groups.findByName(name);
		group.removeMember(this.userService.toAbsoluteDn(LdapUtils.newLdapName(userId)));

		this.groups.save(group);

		return "redirect:/groups/" + name;
	}

}
