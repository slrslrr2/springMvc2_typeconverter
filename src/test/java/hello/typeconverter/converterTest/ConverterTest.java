package hello.typeconverter.converterTest;

import hello.typeconverter.converter.IntegerToStringConverter;
import hello.typeconverter.converter.IpPortToStringConverter;
import hello.typeconverter.converter.StringIpPortConverter;
import hello.typeconverter.converter.StringToIntegerConverter;
import hello.typeconverter.type.IpPort;
import hello.typeconverter.type.TestObject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ConverterTest {
    @Test
    void stringToInteger(){
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Integer result = converter.convert("10");
        Assertions.assertThat(result).isEqualTo(10);
    }

    @Test
    void IntegerTostring(){
        IntegerToStringConverter converter = new IntegerToStringConverter();
        String result = converter.convert(10);
        Assertions.assertThat(result).isEqualTo("10");
    }

    @Test
    void ipPortToString(){
        IpPortToStringConverter converter = new IpPortToStringConverter();
        IpPort source = new IpPort("127.0.0.1", 8080);
        String result = converter.convert(source);
        Assertions.assertThat(result).isEqualTo("127.0.0.1:8080");
    }

    @Test
    void StringToIpPort(){
        StringIpPortConverter converter = new StringIpPortConverter();
        String source = "127.0.0.1:8080";
        IpPort result = converter.convert(source);
        Assertions.assertThat(result).isEqualTo(new IpPort("127.0.0.1", 8080));
    }

    @Test
    void test(){
        Map<String, Integer> map1 = new HashMap<>();
        map1.put("1", 1);
        map1.put("2", 2);

        Map<String, Integer> map2 = new HashMap<>();
        map2.put("1", 1);
        map2.put("2", 2);

        Assertions.assertThat(map1).isEqualTo(map2);
    }

    @Test
    void test2(){
        TestObject testObject = new TestObject("1");
        TestObject testObject2 = new TestObject("1");

        Assertions.assertThat(testObject).isEqualTo(testObject2);
    }
}
