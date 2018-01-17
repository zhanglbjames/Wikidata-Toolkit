package org.wikidata.wdtk.datamodel.implementation;

/*
 * #%L
 * Wikidata Toolkit Data Model
 * %%
 * Copyright (C) 2014 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;

import org.wikidata.wdtk.datamodel.helpers.Equality;
import org.wikidata.wdtk.datamodel.helpers.Hash;
import org.wikidata.wdtk.datamodel.helpers.ToString;
import org.wikidata.wdtk.datamodel.interfaces.NoValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.SnakVisitor;

/**
 * Implementation of {@link NoValueSnak}.
 *
 * @author Markus Kroetzsch
 *
 */
public class NoValueSnakImpl extends SnakImpl implements NoValueSnak, Serializable {

	private static final long serialVersionUID = -8040774434722723542L;

	/**
	 * Constructor.
	 */
	NoValueSnakImpl(PropertyIdValue propertyId) {
		super(propertyId);
	}

	@Override
	public <T> T accept(SnakVisitor<T> snakVisitor) {
		return snakVisitor.visit(this);
	}

	@Override
	public int hashCode() {
		return Hash.hashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return Equality.equalsNoValueSnak(this, obj);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}

}
