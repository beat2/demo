package com.example.demo.config;

import javax.xml.datatype.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

/**
 * Swissdec Webservice Config
 */
@Configuration
public class SwissdecWsConfig {


    @Bean("test")
    public SimpleWsdl11Definition salaryDeclarationConsumerService() {
        return new SimpleWsdl11Definition(new ClassPathResource("wsdl/SalaryDeclarationConsumerService.wsdl"));
    }

    @Bean
    public DatatypeFactory datatypeFactory() {
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        return datatypeFactory;
    }

    @Bean
    public ch.swissdec.schema.sd._20130514.salarydeclarationcontainer.ObjectFactory salaryDeclarationContainerObjectFactory() {
        return new ch.swissdec.schema.sd._20130514.salarydeclarationcontainer.ObjectFactory();
    }

    @Bean
    public ch.swissdec.schema.sd._20130514.salarydeclarationconsumerservicetypes.ObjectFactory salaryDeclarationConsumerServiceTypesObjectFactory() {
        return new ch.swissdec.schema.sd._20130514.salarydeclarationconsumerservicetypes.ObjectFactory();
    }

    @Bean
    public ch.swissdec.schema.sd._20130514.salarydeclarationconsumercontainer.ObjectFactory salaryDeclarationConsumerContainerObjectFactory() {
        return new ch.swissdec.schema.sd._20130514.salarydeclarationconsumercontainer.ObjectFactory();
    }

    @Bean
    public ch.swissdec.schema.sd._20130514.salarydeclaration.ObjectFactory salaryDeclarationObjectFactory() {
        return new ch.swissdec.schema.sd._20130514.salarydeclaration.ObjectFactory();
    }

}


