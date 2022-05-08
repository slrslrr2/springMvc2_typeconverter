# springMvc2_typeconverter
스프링 MVC 2편 - 백엔드 웹 개발 활용 기술 - 스프링 타입 컨버터(typeconverter)

타입 컨버터를 이용하면 X타입을 Y 타입으로 변환해줄 수 있다. <br>예를들면 아래와같다.

```java
@GetMapping("/hello-v1")
public String helloV1(HttpServletRequest request) {
  String data = request.getParameter("data"); //문자 타입 조회 
  Integer intValue = Integer.valueOf(data); //숫자 타입으로 변경 
  System.out.println("intValue = " + intValue);
  return "ok";
}
```

모든 request.getParameter는 문자이다. 아를 **Integer.valueOf(data)**를 사용해서 숫자로 변환해주는 과정은 매우 귀찮은 일이기에<br>Spring에서는 **@RequestParam**를 이용해서 문자를 숫자로 변환해주는 메시지컨버터를 제공해준다.

```java
@GetMapping("/hello-v2")
public String helloV2(@RequestParam Integer data) {
  System.out.println("data = " + data);
  return "ok";
}
```

--------

# 컨버터

아래 인터페이스를 사용해서 다양한 컨버터를 만들어보자

```java
public interface Converter<S, T> {
      T convert(S source);
}
```



##### **1. StringToIntegerConverter -** **문자를 숫자로 변환하는 타입 컨버터**

```java
@Slf4j
public class StringToIntegerConverter implements Converter<String, Integer> {
  @Override
  public Integer convert(String source) {
    log.info("convert source={}", source);
    return Integer.valueOf(source);
  } 
}
```



##### 2. **IntegerToStringConverter -** 숫자를 문자로 변환하는 타입 컨버터

```java
@Slf4j
public class IntegerToStringConverter implements Converter<Integer, String> {
  @Override
  public String convert(Integer source) {
    log.info("convert source={}", source);
    return String.valueOf(source);
  }
}
```



##### 3. **StringToIpPortConverter -** 컨버터

```java
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class IpPort {
    private String ip;
    private Integer port;
}
```

```java
@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {
  @Override
  public IpPort convert(String source) {
    log.info("convert source={}", source);
    String[] split = source.split(":");
    String ip = split[0];
    int port = Integer.parseInt(split[1]);
    return new IpPort(ip, port);
  }
}
```



##### 4. IpPortToStringConverter

```java
@Slf4j
public class IpPortToStringConverter implements Converter<IpPort, String> {
  @Override
  public String convert(IpPort source) {
    log.info("convert source={}", source);
    return source.getIp() + ":" + source.getPort();
  }
}
```



위 Converter를 상속받은 구현체들을 테스트 해보는 방법은 2가지이다.

1. 구현제 가져다 사용하기
2. DefaultConversionService()에 등록해서 사용하기
3. Spring에 등록해서 사용하기
   - @Configuration, implements WebMvcConfigurer



#### 1. 구현체 가져다 사용하기

```java
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
```

> 참고)<br>isEqualTo로 동등조건인데 IpPort에 **@EqualsAndHashCode**를 선언 안하면 Exception터짐<br> https://g-study.tistory.com/169



#### 2. DefaultConversionService 등록해서 사용하기

```java
public class ConversionServiceTest {
  @Test
  void conversionTest(){
    //등록
    DefaultConversionService conversionService = new DefaultConversionService();
    conversionService.addConverter(new IntegerToStringConverter());
    conversionService.addConverter(new StringToIntegerConverter());
    conversionService.addConverter(new IpPortToStringConverter());
    conversionService.addConverter(new StringIpPortConverter());
		
    // 사용
    assertThat(conversionService.convert("10", Integer.class)).isEqualTo(10);
    assertThat(conversionService.convert(10, String.class)).isEqualTo("10");
    assertThat(conversionService.convert("127.0.0.1:8080", IpPort.class)).isEqualTo(new IpPort("127.0.0.1", 8080));
    assertThat(conversionService.convert(new IpPort("127.0.0.1", 8080), String.class)).isEqualTo("127.0.0.1:8080");
  }
}
```

DefaultConversionService는 아래와 같이 상속받았다.

<img width="392" alt="image-20220507210422889" src="https://user-images.githubusercontent.com/58017318/167278044-2243ca9a-caa2-4091-bf48-c733ad96f8ac.png">

**인터페이스 분리 원칙** **- ISP(Interface Segregation Principal)

** 인터페이스 분리 원칙은 클라이언트가 자신이 이용하지 않는 메서드에 의존하지 않아야 한다<br>.DefaultConversionService 는 다음 두 인터페이스를 구현했다. 

```
ConversionService : 컨버터 [사용]에 초점
ConverterRegistry : 컨버터 [등록]에 초점
```

> 이렇게 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와 <br>컨버터를 등록하고 관리하는 클라이언트의 **관심사를 명확하게 분리**할 수 있다. <br>특히 컨버터를 사용하는 클라이언트는 ConversionService에서만 의존하면 되므로, <br>컨버터를 어떻게 등록하고 관리하는지는 전혀 몰라도 된다. <br>결과적으로 **컨버터를 사용하는 클라이언트는 꼭 필요한 메서드만 알게된다**. 이렇게 인터페이스를 분리하는 것을 **ISP** 라 한다.



#### 3. Spring에 등록해서 사용하기

등록하기

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new IntegerToStringConverter());
        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new IpPortToStringConverter());
        registry.addConverter(new StringIpPortConverter());
    }
}
```



사용하기

![image-20220507215052852](image-20220507215052852.png)

--------

# 뷰 템플릿에 컨버터 적용하기



##### ConverterController.java

```java
@Controller
public class ConverterController {
    @GetMapping("/converter-view")
    public String converterView(Model model){
        model.addAttribute("number", 10000);
        model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));

        return "converter-view";
    }
}
```



##### converter-view.html

```html
<ul>
    <li>${number}: <span th:text="${number}"></span></li>
    <li>${{number}}: <span th:text="${{number}}"></span></li>
    <li>${ipPort}: <span th:text="${ipPort}"></span></li>
    <li>${{ipPort}}: <span th:text="${{ipPort}}"></span></li>
</ul>
```

> 변수 표현식 : ${...}<br>컨버전 서비스 적용 : ${{...}}

##### 실행결과

```html
•   ${number}: 10000
•   ${{number}}: 10000
•   ${ipPort}: hello.typeconverter.type.IpPort@59cb0946
•   ${{ipPort}}: 127.0.0.1:8080
```

> 뷰 템플릿은 **데이터를 문자**로 출력한다. <br>그렇기때문에 **${...}(변수표현식)**을 사용하면 ${ipPort}는 toString메서드가 실행되면서 주소값이 찍힌다. (hello.typeconverter.type.IpPort@59cb0946)<br>**컨버전 서비스${{...}}**를 적용하면 해당객체를 **String으로 변환**해주는**IpPortToStringConverter**이 적용된다.



------

# 뷰 템플릿에 컨버터 적용하기2

```java
@GetMapping("/converter/edit")
public String converterForm(Model model) {
  IpPort ipPort = new IpPort("127.0.0.1", 8080);
  Form form = new Form(ipPort);
  model.addAttribute("form", form);
  return "converter-form";
}
```

```html
<form th:object="${form}" th:method="post">
  th:field <input type="text" th:field="*{ipPort}"><br/>
  th:value <input type="text" th:value="*{ipPort}">(보여주기 용도)<br/>
  <input type="submit"/>
</form>
```

<img width="343" alt="image-20220507225741691" src="https://user-images.githubusercontent.com/58017318/167278049-8cf445cb-aac9-47e4-8231-0a1a33df88d5.png">



**th:field**를 사용하면 자동으로 **컨버전 서비스**를 적용해주어서 **${{ipPort}}** 처럼 적용이 되었다. <br>따라서IpPort String 으로 변환된다.



만약, submit할 경우 <br>ipPort는 문자열인 **"127.0.0.1:8080"**이 전송되지만,<br>Controller에서는 @ModelAttribute Form form으로 받기때문에<br>Form으로 받았는데 안에 IpPort가 있기에<br>**StringIpPortConverter**를 통해 Controller에서 받고<br>

<img width="771" alt="image-20220507230056455" src="https://user-images.githubusercontent.com/58017318/167278051-55ad5c68-99e3-4f2e-a374-f3df57e6c60e.png">

```java
@PostMapping("/converter/edit")
public String converterEdit(@ModelAttribute Form form, Model model) {
  IpPort ipPort = form.getIpPort();
  model.addAttribute("ipPort", ipPort);
  return "converter-view";
}

@Data
static class Form {
  private IpPort ipPort;
  public Form(IpPort ipPort) {
    this.ipPort = ipPort;
  }
}
```

타임리프에서 뿌려줄 때 ${{ipPort}}로 인해 컨버전서비스를 제공해주어 **h.t.converter.IpPortToStringConverter**를 이용해 <br>**127.0.0.1:8080**을 뿌린다.

```html
<ul>
    <li>${number}: <span th:text="${number}"></span></li>
    <li>${{number}}: <span th:text="${{number}}"></span></li>
    <li>${ipPort}: <span th:text="${ipPort}"></span></li>
    <li>${{ipPort}}: <span th:text="${{ipPort}}"></span></li>
</ul>
```
<img width="397" alt="image-20220507230721966" src="https://user-images.githubusercontent.com/58017318/167278052-7395a400-238a-465f-88ed-681eff77e5c8.png">
--------

# 포매터

##### Locale

여기에 추가로 날짜 숫자의 표현 방법은 **Locale 현지화 정보**가 사용될 수 있다.<br>이렇게 객체를 **특정한 포멧**에 맞추어 **문자로 출력**하거나 또는 그 반대의 역할을 하는 것에 특화된 기능이 바로 포맷터( Formatter )이다. <br>포맷터는 컨버터의 특별한 버전으로 이해하면 된다.

> **웹 애플리케이션에서 객체를 문자로 문자를 객체로 변환하는 예** 
>
> - 1000 => "1,000"
> - "1,000"=> 1000
> - 날짜객체 => "2021-01-01 10:50:11" 와 같이 출력하거나 또는 그 반대의 상황



##### Converter vs Formatter

- Converter 는 범용(객체 객체)
- Formatter 는 문자에 특화(객체 문자, 문자 객체) + 현지화(Locale)
  - Converter 의 특별한 버전



##### 1. 포매터 만들기

```java
@Slf4j
public class MyNumberFormatter implements Formatter<Number> {
  @Override
  public Number parse(String text, Locale locale) throws ParseException {
    log.info("text={}, locale={}", text, locale);
    return NumberFormat.getInstance(locale).parse(text);
  }

  @Override
  public String print(Number object, Locale locale) {
    log.info("object={}, locale={}", object, locale);
    return NumberFormat.getInstance(locale).format(object);
  }
}
```



##### 2. TestCode 만들기

```java
class MyNumberFormatterTest {
    MyNumberFormatter formatter = new MyNumberFormatter();

    @Test
    void parse() throws ParseException {
        Number result = formatter.parse("1,000", Locale.KOREAN);
        assertThat(result).isEqualTo(1000L);
    }

    @Test
    void print() {
        String result = formatter.print(1000, Locale.KOREAN);
        assertThat(result).isEqualTo("1,000");
    }
}
```



##### 3. DefaultFormattingConversionService로 등록해서 사용하기

DefaultFormattingConversionService는<br> DefaultConversionService가 상속받는 ConversionService와 ConvertRegistry를 상속받기때문에<br>컨버터와 포매터 모두 등록이 가능하다.

```java
public class FormattingConversionServiceTest {
  @Test
  void formattingConversionService() {
    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();

    //컨버터 등록
    conversionService.addConverter(new StringIpPortConverter());
    conversionService.addConverter(new IpPortToStringConverter());
    //포맷터 등록
    conversionService.addFormatter(new MyNumberFormatter());

    //컨버터 사용
    IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
    assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));

    //포맷터 사용
    assertThat(conversionService.convert(1000, String.class)).isEqualTo("1,000");
    assertThat(conversionService.convert("1,000", Long.class)).isEqualTo(1000L);
  }
}
```


<img width="639" alt="image-20220507235231543" src="https://user-images.githubusercontent.com/58017318/167278053-2f4b50f1-5550-4245-b261-0d7cfb236f44.png">



##### 4. WebMvcConfigurer에 등록하기

```java
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
```

> Test
>
> <img width="1416" alt="image-20220508000357077" src="https://user-images.githubusercontent.com/58017318/167278055-33be7233-0026-4517-9c35-f17e812fca01.png">

> 결과
>
> <img width="403" alt="image-20220508000505403" src="https://user-images.githubusercontent.com/58017318/167278057-612c421b-9535-4296-a57d-e7f3cc676e3b.png">





----



스프링이 제공하는 기본 포맷터

스프링은 자바에서 기본으로 제공하는 타입들에 대해 수 많은 포맷터를 기본으로 제공한다.
 IDE에서 Formatter 인터페이스의 구현 클래스를 찾아보면 수 많은 날짜나 시간 관련 포맷터가 제공되는 것을 확인할 수 있다.
 그런데 포맷터는 기본 형식이 지정되어 있기 때문에, 객체의 각 필드마다 다른 형식으로 포맷을 지정하기는 어렵다.

스프링은 이런 문제를 해결하기 위해 애노테이션 기반으로 원하는 형식을 지정해서 사용할 수 있는 매우 유용한 포맷터 두 가지를 기본으로 제공한다.

- **@NumberFormat** : 숫자 관련 형식 지정 포맷터 사용, 
- **@DateTimeFormat** : 날짜 관련 형식 지정 포맷터 사용,

<img width="1320" alt="image-20220508001457161" src="https://user-images.githubusercontent.com/58017318/167278058-9fb73580-f37b-4c94-8460-e24111b6a70b.png">
<img width="380" alt="image-20220508001600221" src="https://user-images.githubusercontent.com/58017318/167278059-e4f3fe1b-68d8-45e3-a04e-6d3d5d5ea367.png">

------

keyword

Integer.valueOf(data)
 implements Converter<String, Integer>
	convert

isEqualTo

DefaultConversionService
conversionService.addConverter(new StringToIntegerConverter());
assertThat(conversionService.convert("127.0.0.1:8080", IpPort.class)).isEqualTo(new IpPort("127.0.0.1", 8080));

ConversionService : 컨버터 [사용]에 초점
ConverterRegistry : 컨버터 [등록]에 초점
ISP

WebMvcConfigurer
addFormatters

변수 표현식 : ${...}
컨버전 서비스 적용 : ${{…}}
th:field="*{ipPort}"


@NumberFormat(pattern = "###,###")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")

Formatter<Number>
DefaultFormattingConversionService
@Data
static class Form {
    @NumberFormat(pattern = "###,###")
    private Integer number;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime localDateTime;
}
