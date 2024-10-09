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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.Attributes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.ldap.core.NameAwareAttribute;
import org.springframework.ldap.core.NameAwareAttributes;
import org.springframework.util.CollectionUtils;

public class AttributesSerializer extends JsonSerializer<Attributes> {

	@Override
	public void serialize(Attributes value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (!(value instanceof NameAwareAttributes attributes)) {
			serializers.defaultSerializeValue(value, gen);
			return;
		}
		Iterator<NameAwareAttribute> iterator = CollectionUtils.toIterator(attributes.getAll());
		while (iterator.hasNext()) {
			NameAwareAttribute attribute = iterator.next();
			if (attribute.size() == 0) {
				serializers.defaultSerializeField(attribute.getID(), null, gen);
			}
			else if (attribute.size() == 1) {
				serializers.defaultSerializeField(attribute.getID(), attribute.get(), gen);
			}
			else {
				List<Object> mapElement = new ArrayList<>();
				attribute.forEach(mapElement::add);
				serializers.defaultSerializeField(attribute.getID(), mapElement, gen);
			}
		}
	}

}
