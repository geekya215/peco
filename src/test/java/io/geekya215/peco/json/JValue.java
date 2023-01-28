package io.geekya215.peco.json;

import java.util.List;
import java.util.Map;

public sealed interface JValue
    permits JValue.JArray, JValue.JBool, JValue.JNull, JValue.JNumber, JValue.JObject, JValue.JString {
    record JNull() implements JValue {
    }

    record JBool(Boolean value) implements JValue {
    }

    record JNumber(Double value) implements JValue {
    }

    record JString(String value) implements JValue {
    }

    record JArray(List<JValue> value) implements JValue {
    }

    record JObject(Map<String, JValue> value) implements JValue {
    }
}
