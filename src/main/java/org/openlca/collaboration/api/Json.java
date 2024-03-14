package org.openlca.collaboration.api;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;

class Json {

	private static final Gson gson = new Gson();
	private static final TypeAdapter<JsonElement> strictAdapter = gson.getAdapter(JsonElement.class);

	static boolean isValid(String json) {
		try {
			strictAdapter.fromJson(json);
		} catch (JsonSyntaxException | IOException e) {
			return false;
		}
		return true;
	}

	static JsonElement parse(String json) {
		return gson.fromJson(json, JsonElement.class);
	}

	static JsonObject toJsonObject(JsonElement element) {
		if (element == null)
			return null;
		if (!element.isJsonObject())
			return null;
		return element.getAsJsonObject();
	}

	static JsonArray toJsonArray(JsonElement element) {
		if (element == null)
			return null;
		if (!element.isJsonArray())
			return null;
		return element.getAsJsonArray();
	}

	static String getString(JsonElement element, String property) {
		var value = getValue(element, property);
		if (value == null || !value.isJsonPrimitive())
			return null;
		var prim = value.getAsJsonPrimitive();
		return prim.isString()
				? prim.getAsString()
				: null;
	}

	static Integer getInt(JsonElement element, String property, Integer defaultValue) {
		var value = getValue(element, property);
		if (value == null || !value.isJsonPrimitive())
			return defaultValue;
		var primitive = value.getAsJsonPrimitive();
		if (primitive.isNumber())
			return primitive.getAsInt();
		if (!primitive.isString())
			return defaultValue;
		try {
			return Integer.parseInt(primitive.getAsString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	static Long getLong(JsonElement element, String property, Long defaultValue) {
		var value = getValue(element, property);
		if (value == null || !value.isJsonPrimitive())
			return defaultValue;
		var primitive = value.getAsJsonPrimitive();
		if (primitive.isNumber())
			return primitive.getAsLong();
		if (!primitive.isString())
			return defaultValue;
		try {
			return Long.parseLong(primitive.getAsString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	static Boolean getBoolean(JsonElement element, String property, Boolean defaultValue) {
		var value = getValue(element, property);
		if (value == null || !value.isJsonPrimitive())
			return defaultValue;
		var primitive = value.getAsJsonPrimitive();
		if (primitive.isBoolean())
			return primitive.getAsBoolean();
		if (primitive.isNumber())
			return primitive.getAsDouble() == 1;
		if (primitive.isString())
			return Boolean.parseBoolean(primitive.getAsString());
		return defaultValue;
	}

	static JsonElement getValue(JsonElement element, String property) {
		if (element == null)
			return null;
		if (!element.isJsonObject())
			return null;
		var object = element.getAsJsonObject();
		if (property.contains(".")) {
			var next = property.substring(0, property.indexOf('.'));
			var rest = property.substring(property.indexOf('.') + 1);
			return getValue(object.get(next), rest);
		}
		if (!object.has(property))
			return null;
		return object.get(property);
	}

}
