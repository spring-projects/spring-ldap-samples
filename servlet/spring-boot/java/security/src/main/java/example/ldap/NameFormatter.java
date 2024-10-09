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

package example.ldap;

import java.util.Locale;

import javax.naming.Name;

import org.springframework.format.Formatter;
import org.springframework.lang.NonNull;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

@Component
public class NameFormatter implements Formatter<Name> {

	@Override
	@NonNull
	public String print(@NonNull Name object, @NonNull Locale locale) {
		return object.toString();
	}

	@Override
	@NonNull
	public Name parse(@NonNull String text, @NonNull Locale locale) {
		return LdapUtils.newLdapName(text);
	}

}
