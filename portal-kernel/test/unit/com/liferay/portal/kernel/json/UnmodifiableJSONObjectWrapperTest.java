/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.json;

import java.io.ObjectInput;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Georgel Pop
 */
public class UnmodifiableJSONObjectWrapperTest {

	@Before
	public void setUp() {
		_mockJSONObject = Mockito.mock(JSONObject.class);

		_wrapper = new UnmodifiableJSONObjectWrapper(_mockJSONObject);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor() {
		new UnmodifiableJSONObjectWrapper(null);
	}

	@Test
	public void testImmutability() {
		_assertThrows(() -> _wrapper.put("k", "v"));

		_assertThrows(() -> _wrapper.remove("k"));

		_assertThrows(
			() -> _wrapper.readExternal(Mockito.mock(ObjectInput.class)));
	}

	@Test
	public void testReads() {
		Mockito.when(
			_mockJSONObject.getString("k")
		).thenReturn(
			"v"
		);

		Assert.assertEquals("v", _wrapper.getString("k"));
	}

	@Test
	public void testWrapping() {
		JSONObject mockChildJSONObject = Mockito.mock(JSONObject.class);

		Mockito.when(
			_mockJSONObject.getJSONObject("c")
		).thenReturn(
			mockChildJSONObject
		);

		Assert.assertTrue(
			_wrapper.getJSONObject("c") instanceof
				UnmodifiableJSONObjectWrapper);
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

	private JSONObject _mockJSONObject;
	private UnmodifiableJSONObjectWrapper _wrapper;

	@FunctionalInterface
	private interface UnsafeAction {

		public void execute() throws Exception;

	}

}