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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ldap.samples.useradmin.domain.DepartmentRepository;
import org.springframework.ldap.samples.useradmin.domain.User;
import org.springframework.ldap.samples.useradmin.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The endpoints for administering users.
 *
 * @author Mattias Hellborg Arthursson
 */
@Controller
public class UserController {

	private final AtomicInteger nextEmployeeNumber = new AtomicInteger(10);

	private final UserService userService;

	private final DepartmentRepository departments;

	public UserController(UserService userService, DepartmentRepository departments) {
		this.userService = userService;
		this.departments = departments;
	}

	@GetMapping({ "/", "/users" })
	public String index(ModelMap map, @RequestParam(name = "name", required = false) String name) {
		if (StringUtils.hasText(name)) {
			map.put("users", this.userService.searchByNameName(name));
		}
		else {
			map.put("users", this.userService.findAll());
		}
		return "listUsers";
	}

	@GetMapping("/users/{userid}")
	public String getUser(@PathVariable("userid") String userid, ModelMap map) throws JsonProcessingException {
		map.put("user", this.userService.findUser(userid));
		populateDepartments(map);
		return "editUser";
	}

	@GetMapping("/newuser")
	public String initNewUser(ModelMap map) throws JsonProcessingException {
		User user = new User();
		user.setEmployeeNumber(this.nextEmployeeNumber.getAndIncrement());

		map.put("isNew", true);
		map.put("user", user);
		populateDepartments(map);

		return "editUser";
	}

	private void populateDepartments(ModelMap map) throws JsonProcessingException {
		Map<String, List<String>> departmentMap = this.departments.getDepartmentMap();
		ObjectMapper objectMapper = new ObjectMapper();
		String departmentsAsJson = objectMapper.writeValueAsString(departmentMap);
		map.put("departments", departmentsAsJson);
	}

	@PostMapping("/newuser")
	public String createUser(User user) {
		User savedUser = this.userService.createUser(user);

		return "redirect:/users/" + savedUser.getId();
	}

	@PostMapping("/users/{userid}")
	public String updateUser(@PathVariable("userid") String userid, User user) {
		User savedUser = this.userService.updateUser(userid, user);

		return "redirect:/users/" + savedUser.getId();
	}

}
