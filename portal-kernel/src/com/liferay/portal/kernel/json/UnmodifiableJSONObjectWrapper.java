/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.json;

import com.liferay.petra.function.UnsafeSupplier;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Georgel Pop
 */
public class UnmodifiableJSONObjectWrapper implements JSONObject {

	public UnmodifiableJSONObjectWrapper() {
		_jsonObject = null;
	}

	public UnmodifiableJSONObjectWrapper(JSONObject jsonObject) {
		if (jsonObject == null) {
			throw new IllegalArgumentException("JSONObject cannot be null");
		}

		_jsonObject = jsonObject;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object instanceof UnmodifiableJSONObjectWrapper) {
			UnmodifiableJSONObjectWrapper wrapper =
				(UnmodifiableJSONObjectWrapper)object;

			return Objects.equals(_jsonObject, wrapper._jsonObject);
		}

		return Objects.equals(_jsonObject, object);
	}

	@Override
	public Object get(String key) {
		if (_jsonObject == null) {
			return null;
		}

		return _getObject(_jsonObject.get(key));
	}

	@Override
	public boolean getBoolean(String key) {
		if (_jsonObject == null) {
			return false;
		}

		return _jsonObject.getBoolean(key);
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		if (_jsonObject == null) {
			return defaultValue;
		}

		return _jsonObject.getBoolean(key, defaultValue);
	}

	@Override
	public double getDouble(String key) {
		if (_jsonObject == null) {
			return 0.0;
		}

		return _jsonObject.getDouble(key);
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		if (_jsonObject == null) {
			return defaultValue;
		}

		return _jsonObject.getDouble(key, defaultValue);
	}

	@Override
	public int getInt(String key) {
		if (_jsonObject == null) {
			return 0;
		}

		return _jsonObject.getInt(key);
	}

	@Override
	public int getInt(String key, int defaultValue) {
		if (_jsonObject == null) {
			return defaultValue;
		}

		return _jsonObject.getInt(key, defaultValue);
	}

	@Override
	public JSONArray getJSONArray(String key) {
		if (_jsonObject == null) {
			return null;
		}

		JSONArray childJSONArray = _jsonObject.getJSONArray(key);

		if (childJSONArray != null) {
			return new UnmodifiableJSONArrayWrapper(childJSONArray);
		}

		return null;
	}

	@Override
	public JSONObject getJSONObject(String key) {
		if (_jsonObject == null) {
			return null;
		}

		JSONObject childJSONObject = _jsonObject.getJSONObject(key);

		if (childJSONObject != null) {
			return new UnmodifiableJSONObjectWrapper(childJSONObject);
		}

		return null;
	}

	@Override
	public long getLong(String key) {
		if (_jsonObject == null) {
			return 0;
		}

		return _jsonObject.getLong(key);
	}

	@Override
	public long getLong(String key, long defaultValue) {
		if (_jsonObject == null) {
			return defaultValue;
		}

		return _jsonObject.getLong(key, defaultValue);
	}

	@Override
	public String getString(String key) {
		if (_jsonObject == null) {
			return null;
		}

		return _jsonObject.getString(key);
	}

	@Override
	public String getString(String key, String defaultValue) {
		if (_jsonObject == null) {
			return defaultValue;
		}

		return _jsonObject.getString(key, defaultValue);
	}

	@Override
	public boolean has(String key) {
		if (_jsonObject == null) {
			return false;
		}

		return _jsonObject.has(key);
	}

	@Override
	public int hashCode() {
		if (_jsonObject == null) {
			return 0;
		}

		return _jsonObject.hashCode();
	}

	@Override
	public boolean isNull(String key) {
		if (_jsonObject == null) {
			return true;
		}

		return _jsonObject.isNull(key);
	}

	@Override
	public Iterator<String> keys() {
		if (_jsonObject == null) {
			return Collections.emptyIterator();
		}

		return Collections.unmodifiableSet(
			_jsonObject.keySet()
		).iterator();
	}

	@Override
	public Set<String> keySet() {
		if (_jsonObject == null) {
			return Collections.emptySet();
		}

		return Collections.unmodifiableSet(_jsonObject.keySet());
	}

	@Override
	public int length() {
		if (_jsonObject == null) {
			return 0;
		}

		return _jsonObject.length();
	}

	@Override
	public JSONArray names() {
		if (_jsonObject == null) {
			return null;
		}

		return _jsonObject.names();
	}

	@Override
	public Object opt(String key) {
		if (_jsonObject == null) {
			return null;
		}

		return _getObject(_jsonObject.opt(key));
	}

	@Override
	public JSONObject put(String key, boolean value) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, Date value) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, double value) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, int value) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, JSONArray jsonArray) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, JSONObject jsonObject) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, long value) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, Object value) {
		return _blockModification();
	}

	@Override
	public JSONObject put(String key, String value) {
		return _blockModification();
	}

	@Override
	public JSONObject put(
		String key, UnsafeSupplier<Object, Exception> valueUnsafeSupplier) {

		return _blockModification();
	}

	@Override
	public JSONObject putException(Exception exception) {
		return _blockModification();
	}

	@Override
	public void readExternal(ObjectInput objectInput)
		throws ClassNotFoundException, IOException {

		throw new UnsupportedOperationException(
			"Cannot deserialize into an unmodifiable wrapper");
	}

	@Override
	public Object remove(String key) {
		throw new UnsupportedOperationException(
			"This JSONObject is unmodifiable");
	}

	@Override
	public String toJSONString() {
		if (_jsonObject == null) {
			return "{}";
		}

		return _jsonObject.toString();
	}

	@Override
	public Map<String, Object> toMap() {
		if (_jsonObject == null) {
			return Collections.emptyMap();
		}

		return _jsonObject.toMap();
	}

	@Override
	public String toString() {
		if (_jsonObject == null) {
			return "{}";
		}

		return _jsonObject.toString();
	}

	@Override
	public String toString(int indentFactor) throws JSONException {
		if (_jsonObject == null) {
			return "{}";
		}

		return _jsonObject.toString(indentFactor);
	}

	@Override
	public Writer write(Writer writer) throws JSONException {
		if (_jsonObject == null) {
			try {
				return writer.append("{}");
			}
			catch (IOException ioException) {
				throw new JSONException(ioException);
			}
		}

		return _jsonObject.write(writer);
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		if (_jsonObject != null) {
			_jsonObject.writeExternal(objectOutput);
		}
	}

	private JSONObject _blockModification() {
		throw new UnsupportedOperationException(
			"This JSONObject is unmodifiable");
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

	private final JSONObject _jsonObject;

}