package com.example.demo.config;


import java.util.*;
import org.slf4j.*;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.ws.config.annotation.*;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.*;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.xml.xsd.SimpleXsdSchema;

@Configuration
@EnableWs
@Import({SwissdecWsConfig.class})
public class WebserviceServerConfig extends WsConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebserviceServerConfig.class);

    private static final String WSDL_LOCATION = "wsdl/SalaryDeclarationConsumerServiceTypes.xsd";


    @Bean
    public ConversionService conversionService(Set<Converter<?, ?>> converters) {
        ConversionServiceFactoryBean csfb = new ConversionServiceFactoryBean();
        csfb.setConverters(converters);
        csfb.afterPropertiesSet();
        return csfb.getObject();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        final PayloadValidatingInterceptor payloadValidatingInterceptor =
            new PayloadValidatingInterceptor();
        final var xsdResource = new ClassPathResource(WSDL_LOCATION);
        payloadValidatingInterceptor.setXsdSchema(new SimpleXsdSchema(xsdResource));

        payloadValidatingInterceptor.setValidateRequest(true);
        payloadValidatingInterceptor.setValidateResponse(true);

        interceptors.add(new SoapEnvelopeLoggingInterceptor());
        interceptors.add(payloadValidatingInterceptor);
    }

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
        ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

}
