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
import java.util.Map;

import javax.naming.directory.Attributes;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.springframework.ldap.core.NameAwareAttributes;

public class AttributesDeserializer extends JsonDeserializer<Attributes> {

	@Override
	public Attributes deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		NameAwareAttributes attributes = new NameAwareAttributes();
		if (p.currentToken() == JsonToken.START_OBJECT) {
			p.nextToken();
		}
		Map<String, Object> map = p.getCodec().readValue(p, new TypeReference<>() {
		});
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			attributes.put(entry.getKey(), entry.getValue());
		}
		return attributes;
	}

}
