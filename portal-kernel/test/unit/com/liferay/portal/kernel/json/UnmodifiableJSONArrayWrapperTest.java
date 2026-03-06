/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.json;

import java.io.ObjectInput;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Georgel Pop
 */
public class UnmodifiableJSONArrayWrapperTest {

	@Before
	public void setUp() {
		_mockJSONArray = Mockito.mock(JSONArray.class);

		_wrapper = new UnmodifiableJSONArrayWrapper(_mockJSONArray);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor() {
		new UnmodifiableJSONArrayWrapper(null);
	}

	@Test
	public void testImmutability() {
		_assertThrows(() -> _wrapper.put("fail"));

		_assertThrows(() -> _wrapper.put(1));

		Iterator<Object> iterator = _wrapper.iterator();

		_assertThrows(iterator::remove);

		_assertThrows(
			() -> _wrapper.readExternal(Mockito.mock(ObjectInput.class)));
	}

	@Test
	public void testReads() {
		Mockito.when(
			_mockJSONArray.getString(0)
		).thenReturn(
			"val"
		);

		Mockito.when(
			_mockJSONArray.length()
		).thenReturn(
			1
		);

		Assert.assertEquals("val", _wrapper.getString(0));
		Assert.assertEquals(1, _wrapper.length());
	}

	@Test
	public void testWrapping() {
		JSONObject mockChildJSONObject = Mockito.mock(JSONObject.class);

		Mockito.when(
			_mockJSONArray.getJSONObject(0)
		).thenReturn(
			mockChildJSONObject
		);

		Assert.assertTrue(
			_wrapper.getJSONObject(0) instanceof UnmodifiableJSONObjectWrapper);
	}

	private void _assertThrows(UnsafeAction action) {
		try {
			action.execute();

			Assert.fail("Expected exception not thrown");
		}
		catch (UnsupportedOperationException unsupportedOperationException) {
			String message = unsupportedOperationException.getMessage();

			Assert.assertTrue(
				message.toLowerCase(
				).contains(
					"unmodifiable"
				));
		}
		catch (Exception exception) {
			throw new AssertionError(
				"Wrong exception: " + exception.getClass(), exception);
		}
	}

	private JSONArray _mockJSONArray;
	private UnmodifiableJSONArrayWrapper _wrapper;

	@FunctionalInterface
	private interface UnsafeAction {

		public void execute() throws Exception;

	}

}