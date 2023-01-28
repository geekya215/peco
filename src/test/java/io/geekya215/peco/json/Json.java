package io.geekya215.peco.json;

import io.geekya215.peco.Input;
import io.geekya215.peco.Parser;
import io.geekya215.peco.Result;
import io.geekya215.peco.Tuple;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.geekya215.peco.Combinator.*;

public class Json {
    public static final Parser<JValue> jNull = setLabel(map(__ -> new JValue.JNull(), string("null")), "null");
    public static final Parser<JValue> jBool = setLabel(
        or(
            map(__ -> new JValue.JBool(true), string("true")),
            map(__ -> new JValue.JBool(false), string("false"))),
        "bool");
    public static final Parser<Character> jUnescapedChar = satisfy(c -> c != '\\' && c != '\"', "char");
    public static final Parser<Character> jEscapedChar = setLabel(
        choice(List.of(
            map(__ -> '\"', string("\\\"")),
            map(__ -> '\\', string("\\\\")),
            map(__ -> '/', string("\\/")),
            map(__ -> '\b', string("\\b")),
            map(__ -> '\f', string("\\f")),
            map(__ -> '\n', string("\\n")),
            map(__ -> '\r', string("\\r")),
            map(__ -> '\t', string("\\t"))
        )), "escaped char");
    public static final Parser<Character> jUnicodeChar = getJUnicodeChar();
    public static final Parser<JValue> jString = setLabel(
        map(JValue.JString::new, getQuotedString()),
        "quoted string");
    public static final Parser<JValue> jNumber = setLabel(
        map(JValue.JNumber::new, getNumber()),
        "number"
    );
    public static Ref<Parser<JValue>> jValueRef = Ref.of(
        Parser.of(input -> Result.Failure.of("unknown", "unknown", Input.getLocationFromState(input)), "unknown")
    );
    public static Parser<JValue> jValue = Parser.of(
        input -> runOnInput(jValueRef.get(), input), "unknown"
    );
    public static final Parser<JValue> jArray = setLabel(
        map(JValue.JArray::new, getJArray()),
        "array"
    );
    public static final Parser<JValue> jObject = setLabel(
        map(JValue.JObject::new, getJObject()),
        "object"
    );

    static {
        jValueRef.set(choice(List.of(
            jNull,
            jBool,
            jNumber,
            jString,
            jArray,
            jObject
        )));
    }

    public static Parser<Character> getJUnicodeChar() {
        var backslash = character('\\');
        var uChar = character('u');
        var hexDigit = satisfy(c ->
            (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'), "hex digit");
        var fourHexDigits = count(4, hexDigit);

        return map(xs -> {
            String str = String.format("%c%c%c%c", xs.get(0), xs.get(1), xs.get(2), xs.get(3));
            return (char) Integer.parseInt(str, 16);
        }, discardL(backslash, discardL(uChar, fourHexDigits)));
    }

    public static Parser<String> getQuotedString() {
        var quote = character('\"');
        var jChar = or(or(jUnescapedChar, jEscapedChar), jUnicodeChar);
        return map(
            xs -> xs.stream().map(String::valueOf).collect(Collectors.joining()),
            discardL(quote, discardR(many(jChar), quote)));
    }

    public static Parser<Double> getNumber() {
        var optSign = opt(character('-'));
        var zero = string("0");
        var oneToNine = satisfy(c -> c >= '1' && c <= '9', "one to nine digit");
        var point = character('.');
        var exp = or(character('e'), character('E'));
        var optPlusMinus = opt(or(character('+'), character(('-'))));
        var nonZeroInt = map(
            t -> t.t1() + t.t2().stream().map(String::valueOf).collect(Collectors.joining()),
            then(oneToNine, many(digit())));
        var intPart = or(zero, nonZeroInt);
        var fractionPart = discardL(
            point,
            map(xs -> xs.stream().map(String::valueOf).collect(Collectors.joining()),
                many1(digit())));
        var exponentPart =
            then(
                discardL(exp, optPlusMinus),
                map(xs -> xs.stream().map(String::valueOf).collect(Collectors.joining()), many1(digit())));
        return
            map(n -> {
                    var _optSign = n.t1().t1().t1();
                    var _intPart = n.t1().t1().t2();
                    var _fractionPart = n.t1().t2();
                    var _expPart = n.t2();

                    var signStr = _optSign.map(String::valueOf).orElse("");
                    var fractionPartStr = _fractionPart.map(s -> "." + s).orElse("");
                    var expPartStr = _expPart.map(e -> "e" + e.t1().map(String::valueOf).orElse("") + e.t2()).orElse("");
                    return Double.valueOf(signStr + _intPart + fractionPartStr + expPartStr);
                }
                , then(then(then(optSign, intPart), opt(fractionPart)), opt(exponentPart))
            );
    }

    public static Parser<List<JValue>> getJArray() {
        var left = discardR(character('['), spaces());
        var right = discardR(character(']'), spaces());
        var comma = discardR(character(','), spaces());
        var value = discardR(jValue, spaces());

        var values = sepBy(value, comma);
        return between(left, values, right);
    }

    public static Parser<Map<String, JValue>> getJObject() {
        var left = discardR(character('{'), spaces());
        var right = discardR(character('}'), spaces());
        var colon = discardR(character(':'), spaces());
        var comma = discardR(character(','), spaces());
        var key = discardR(getQuotedString(), spaces());
        var value = discardR(jValue, spaces());

        var keyValue = then(discardR(key, colon), value);
        var keyValues = sepBy(keyValue, comma);
        return map(
            xs -> xs.stream().collect(Collectors.toMap(Tuple::t1, Tuple::t2)),
            between(left, keyValues, right));
    }
}
