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

import java.io.IOException;

import javax.naming.Name;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.springframework.ldap.support.LdapUtils;

public class NameDeserializer extends JsonDeserializer<Name> {

	@Override
	public Name deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		if (p.currentToken() == JsonToken.START_OBJECT) {
			p.nextToken();
		}
		String value = p.getCodec().readValue(p, String.class);
		return LdapUtils.newLdapName(value);
	}

}
