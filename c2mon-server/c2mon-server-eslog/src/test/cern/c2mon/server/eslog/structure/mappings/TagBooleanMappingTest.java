/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.mappings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;

/**
 * Verify the good behaviour of the TagBooleanMapping class.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagBooleanMappingTest {

	@Test
	public void testGetBooleanMapping() {
		TagBooleanMapping mapping = new TagBooleanMapping(ValueType.boolType);
		String valueType = mapping.properties.getValueType();
		assertEquals(ValueType.boolType.toString(), valueType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetBooleanMapping() {
		TagBooleanMapping mapping = new TagBooleanMapping(ValueType.stringType);
		mapping.properties.getValueType();
	}
}
