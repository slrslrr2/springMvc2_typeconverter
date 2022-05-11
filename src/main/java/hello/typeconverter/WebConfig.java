package hello.typeconverter;

import hello.typeconverter.converter.IpPortToStringConverter;
import hello.typeconverter.converter.StringIpPortConverter;
import hello.typeconverter.formatter.MyNumberFormatter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
//        registry.addConverter(new IntegerToStringConverter());
//        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new IpPortToStringConverter());
        registry.addConverter(new StringIpPortConverter());

        // 추가
        registry.addFormatter(new MyNumberFormatter());
    }
}
