/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.json;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringPool;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author Georgel Pop
 */
public class UnmodifiableJSONArrayWrapper implements JSONArray {

	public UnmodifiableJSONArrayWrapper() {
		_jsonArray = null;
	}

	public UnmodifiableJSONArrayWrapper(JSONArray jsonArray) {
		if (jsonArray == null) {
			throw new IllegalArgumentException("JSONArray cannot be null");
		}

		_jsonArray = jsonArray;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object instanceof UnmodifiableJSONArrayWrapper) {
			UnmodifiableJSONArrayWrapper wrapper =
				(UnmodifiableJSONArrayWrapper)object;

			return Objects.equals(_jsonArray, wrapper._jsonArray);
		}

		return Objects.equals(_jsonArray, object);
	}

	@Override
	public Object get(int index) {
		if (_jsonArray == null) {
			return null;
		}

		return _getObject(_jsonArray.get(index));
	}

	@Override
	public boolean getBoolean(int index) {
		if (_jsonArray == null) {
			return false;
		}

		return _jsonArray.getBoolean(index);
	}

	@Override
	public double getDouble(int index) {
		if (_jsonArray == null) {
			return 0.0;
		}

		return _jsonArray.getDouble(index);
	}

	@Override
	public int getInt(int index) {
		if (_jsonArray == null) {
			return 0;
		}

		return _jsonArray.getInt(index);
	}

	@Override
	public JSONArray getJSONArray(int index) {
		if (_jsonArray == null) {
			return null;
		}

		JSONArray childJSONArray = _jsonArray.getJSONArray(index);

		if (childJSONArray != null) {
			return new UnmodifiableJSONArrayWrapper(childJSONArray);
		}

		return null;
	}

	@Override
	public JSONObject getJSONObject(int index) {
		if (_jsonArray == null) {
			return null;
		}

		JSONObject childJSONObject = _jsonArray.getJSONObject(index);

		if (childJSONObject != null) {
			return new UnmodifiableJSONObjectWrapper(childJSONObject);
		}

		return null;
	}

	@Override
	public long getLong(int index) {
		if (_jsonArray == null) {
			return 0;
		}

		return _jsonArray.getLong(index);
	}

	@Override
	public String getString(int index) {
		if (_jsonArray == null) {
			return null;
		}

		return _jsonArray.getString(index);
	}

	@Override
	public int hashCode() {
		if (_jsonArray == null) {
			return 0;
		}

		return _jsonArray.hashCode();
	}

	@Override
	public boolean isNull(int index) {
		if (_jsonArray == null) {
			return true;
		}

		return _jsonArray.isNull(index);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<Object> iterator() {
		if (_jsonArray == null) {
			return Collections.emptyIterator();
		}

		final Iterator<Object> originalIterator = _jsonArray.iterator();

		return new Iterator<Object>() {

			@Override
			public boolean hasNext() {
				return originalIterator.hasNext();
			}

			@Override
			public Object next() {
				return _getObject(originalIterator.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
					"This JSONArray is unmodifiable");
			}

		};
	}

	@Override
	public String join(String separator) throws JSONException {
		if (_jsonArray == null) {
			return StringPool.BLANK;
		}

		return _jsonArray.join(separator);
	}

	@Override
	public int length() {
		if (_jsonArray == null) {
			return 0;
		}

		return _jsonArray.length();
	}

	@Override
	public JSONArray put(boolean value) {
		return _blockModification();
	}

	@Override
	public JSONArray put(double value) {
		return _blockModification();
	}

	@Override
	public JSONArray put(int value) {
		return _blockModification();
	}

	@Override
	public JSONArray put(JSONArray jsonArray) {
		return _blockModification();
	}

	@Override
	public JSONArray put(JSONObject jsonObject) {
		return _blockModification();
	}

	@Override
	public JSONArray put(long value) {
		return _blockModification();
	}

	@Override
	public JSONArray put(Object value) {
		return _blockModification();
	}

	@Override
	public JSONArray put(String value) {
		return _blockModification();
	}

	@Override
	public JSONArray put(
		UnsafeSupplier<Object, Exception> valueUnsafeSupplier) {

		return _blockModification();
	}

	@Override
	public void readExternal(ObjectInput objectInput)
		throws ClassNotFoundException, IOException {

		throw new UnsupportedOperationException(
			"Cannot deserialize into an unmodifiable wrapper");
	}

	@Override
	public String toJSONString() {
		if (_jsonArray == null) {
			return "[]";
		}

		return _jsonArray.toString();
	}

	@Override
	public String toString() {
		if (_jsonArray == null) {
			return "[]";
		}

		return _jsonArray.toString();
	}

	@Override
	public String toString(int indentFactor) throws JSONException {
		if (_jsonArray == null) {
			return "[]";
		}

		return _jsonArray.toString(indentFactor);
	}

	@Override
	public Writer write(Writer writer) throws JSONException {
		if (_jsonArray == null) {
			try {
				return writer.append("[]");
			}
			catch (IOException ioException) {
				throw new JSONException(ioException);
			}
		}

		return _jsonArray.write(writer);
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		if (_jsonArray != null) {
			_jsonArray.writeExternal(objectOutput);
		}
	}

	private JSONArray _blockModification() {
		throw new UnsupportedOperationException(
			"This JSONArray is unmodifiable");
	}

	private Object _getObject(Object item) {
		if (item == null) {
			return null;
		}

		if (item instanceof JSONObject) {
			return new UnmodifiableJSONObjectWrapper((JSONObject)item);
		}

		if (item instanceof JSONArray) {
			return new UnmodifiableJSONArrayWrapper((JSONArray)item);
		}

		return item;
	}

	private final JSONArray _jsonArray;

}