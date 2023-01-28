package io.geekya215.peco;

import io.geekya215.peco.json.JValue;
import io.geekya215.peco.json.Json;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.geekya215.peco.Combinator.run;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTest {
    private final Parser<JValue> parser = Json.jValue;

    @Test
    void testNull() {
        var expectedResult = new JValue.JNull();
        var actualResult = run(parser, "null");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }

    @Test
    void testTrue() {
        var expectedResult = new JValue.JBool(true);
        var actualResult = run(parser, "true");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }

    @Test
    void testFalse() {
        var expectedResult = new JValue.JBool(false);
        var actualResult = run(parser, "false");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }

    @Test
    void testNumber() {
        var expectedResult = new JValue.JNumber(1234.5);
        var actualResult = run(parser, "1.2345e3");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }

    @Test
    void testUnicode() {
        var expectedResult = new JValue.JString("喵");
        var actualResult = run(parser, "\"\u55b5\"");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }

    @Test
    void testString() {
        var expectedResult = new JValue.JString("hello 你好 こんにちは");
        var actualResult = run(parser, "\"hello \u4f60\u597d \u3053\u3093\u306b\u3061\u306f\"");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }

    @Test
    void testArray() {
        var expectedResult =
            new JValue.JArray(List.of(new JValue.JNull(), new JValue.JBool(false), new JValue.JNumber(1.0), new JValue.JString("hello")));
        var actualResult = run(parser, "[null, false, 1, \"hello\"]");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }

    @Test
    void testObject() {
        var expectedResult =
            new JValue.JObject(new HashMap<>() {{
                put("name", new JValue.JString("tom"));
                put("weight", new JValue.JNumber(65.4));
                put("bio", new JValue.JNull());
                put("verification", new JValue.JBool(true));
                put("followers", new JValue.JArray(new ArrayList<>() {{
                    add(new JValue.JObject(new HashMap<>() {{
                        put("id", new JValue.JNumber(1001.0));
                        put("name", new JValue.JString("jack"));
                    }}));
                    add(new JValue.JObject(new HashMap<>() {{
                        put("id", new JValue.JNumber(1002.0));
                        put("name", new JValue.JString("bob"));
                    }}));
                }}));
            }});
        var actualResult = run(parser,
            """
                {
                    "name": "tom",
                    "weight": 65.4,
                    "bio": null,
                    "verification": true,
                    "followers": [
                        {"id": 1001, "name": "jack" },
                        {"id": 1002, "name": "bob" }
                    ]
                }
                """);
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<JValue, State>>) actualResult).getValue().t1());
    }
}
