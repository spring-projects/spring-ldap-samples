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

package org.springframework.ldap.samples.plain.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.naming.Name;

import org.apache.commons.lang.StringUtils;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.samples.plain.domain.Person;
import org.springframework.ldap.samples.plain.repository.PersonRepository;
import org.springframework.ldap.samples.utils.HtmlRowLdapTreeVisitor;
import org.springframework.ldap.samples.utils.LdapTree;
import org.springframework.ldap.samples.utils.LdapTreeBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Default controller.
 *
 * @author Mattias Hellborg Arthursson
 */
@Controller
public class DefaultController {

	private final LdapTreeBuilder ldapTreeBuilder;

	private final PersonRepository persons;

	public DefaultController(LdapTreeBuilder ldapTreeBuilder, PersonRepository persons) {
		this.ldapTreeBuilder = ldapTreeBuilder;
		this.persons = persons;
	}

	@RequestMapping("/welcome.do")
	public void welcomeHandler() {
	}

	@RequestMapping("/showTree.do")
	public ModelAndView showTree() {
		LdapTree ldapTree = this.ldapTreeBuilder.getLdapTree(LdapUtils.emptyLdapName());
		HtmlRowLdapTreeVisitor visitor = new PersonLinkHtmlRowLdapTreeVisitor();
		ldapTree.traverse(visitor);
		return new ModelAndView("showTree", "rows", visitor.getRows());
	}

	@RequestMapping("/addPerson.do")
	public String addPerson() {
		Person person = getPerson();

		this.persons.create(person);
		return "redirect:/showTree.do";
	}

	@RequestMapping("/updatePhoneNumber.do")
	public String updatePhoneNumber() {
		Person person = this.persons.findByPrimaryKey("Sweden", "company1", "John Doe");
		person.setPhone(StringUtils.join(new String[] { person.getPhone(), "0" }));

		this.persons.update(person);
		return "redirect:/showTree.do";
	}

	@RequestMapping("/removePerson.do")
	public String removePerson() {
		Person person = getPerson();

		this.persons.delete(person);
		return "redirect:/showTree.do";
	}

	@RequestMapping("/showPerson.do")
	public ModelMap showPerson(String country, String company, String fullName) {
		Person person = this.persons.findByPrimaryKey(country, company, fullName);
		return new ModelMap("person", person);
	}

	private Person getPerson() {
		Person person = new Person();
		person.setFullName("John Doe");
		person.setLastName("Doe");
		person.setCompany("company1");
		person.setCountry("Sweden");
		person.setDescription("Test user");
		return person;
	}

	/**
	 * Generates appropriate links for person leaves in the tree.
	 *
	 * @author Mattias Hellborg Arthursson
	 */
	private static final class PersonLinkHtmlRowLdapTreeVisitor extends HtmlRowLdapTreeVisitor {

		@Override
		protected String getLinkForNode(DirContextOperations node) {
			String[] objectClassValues = node.getStringAttributes("objectClass");
			if (containsValue(objectClassValues, "person")) {
				Name dn = node.getDn();
				String country = encodeValue(LdapUtils.getStringValue(dn, "c"));
				String company = encodeValue(LdapUtils.getStringValue(dn, "ou"));
				String fullName = encodeValue(LdapUtils.getStringValue(dn, "cn"));

				return "showPerson.do?country=" + country + "&company=" + company + "&fullName=" + fullName;
			}
			else {
				return super.getLinkForNode(node);
			}
		}

		private String encodeValue(String value) {
			return URLEncoder.encode(value, StandardCharsets.UTF_8);
		}

		private boolean containsValue(String[] values, String value) {
			for (String oneValue : values) {
				if (StringUtils.equals(oneValue, value)) {
					return true;
				}
			}
			return false;
		}

	}

}
