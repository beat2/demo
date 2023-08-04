package com.example.demo.config;


import com.example.demo.SalaryDeclarationConsumerEndpoint;
import java.util.*;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.dom.WSConstants;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.ws.config.annotation.*;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;
import org.springframework.ws.soap.server.endpoint.interceptor.*;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.xml.xsd.SimpleXsdSchema;

@Configuration
@EnableWs
@Import({SwissdecWsConfig.class})
public class WebserviceServerConfig extends WsConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebserviceServerConfig.class);

    private static final List<String> ACTIONS_TO_BE_SECURED = Arrays.asList("DeclareSalaryConsumer", "GetResultFromDeclareSalaryConsumer", "SynchronizeContractConsumer");
    private static final String WSDL_LOCATION = "wsdl/SalaryDeclarationConsumerServiceTypes.xsd";

    @Autowired
    ApplicationContext appContext;
    @Value("${keystore.path}")
    String keystoreLocation;
    @Value("${keystore.password}")
    String keystorePassword;
    @Value("${privateKeyAlias}")
    String privateKeyAlias;


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

        EncryptionDecryptionInterceptor securityInterceptor = setupSecurityInterceptor();

        // Note: ordering is relevant! First decrypt then log plain and then validate.
        interceptors.add(securityInterceptor);
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

    private EncryptionDecryptionInterceptor setupSecurityInterceptor() {
        Wss4jSecurityInterceptor sec = new Wss4jSecurityInterceptor();
        // incoming WSS config (decrypt Request)
        sec.setValidationActions(ConfigurationConstants.TIMESTAMP + " " + ConfigurationConstants.SIGNATURE + " " + ConfigurationConstants.ENCRYPT);
        sec.setValidationDecryptionCrypto(createDecryptionCrypto());

        // is needed for signature check, even though the cert is embedded
        sec.setValidationSignatureCrypto(createDecryptionCrypto());

        // outgoing WSS config (encrypt Response)
        sec.setSecurementActions(ConfigurationConstants.ENCRYPT);
        sec.setSecurementEncryptionUser(ConfigurationConstants.USE_REQ_SIG_CERT);
        sec.setSecurementEncryptionSymAlgorithm(WSConstants.AES_256);
        sec.setSecurementEncryptionKeyTransportAlgorithm(WSConstants.KEYTRANSPORT_RSAOAEP);
        // outgoing WSS config (sign Response), not needed by default
        sec.setSecurementSignatureCrypto(createDecryptionCrypto());
        sec.setSecurementSignatureUser(privateKeyAlias);
        sec.setSecurementPassword(keystorePassword);

        // NOTE: setSecurementEncryptionParts is unneeded, see spec 3.2.2
        KeyStoreCallbackHandler kscb = new KeyStoreCallbackHandler();
        kscb.setPrivateKeyPassword(keystorePassword);

        sec.setValidationCallbackHandler(kscb);

        // the actions
        return new EncryptionDecryptionInterceptor(sec, SalaryDeclarationConsumerEndpoint.NAMESPACE_URI, ACTIONS_TO_BE_SECURED);
    }


    private Resource getKeystoreAsResource() {
        // getResource ohne prefix versucht nur mittels ClassPath zu laden
        if (!keystoreLocation.contains(":")) {
            if (keystoreLocation.startsWith("/")) {
                keystoreLocation = "file://" + keystoreLocation;
            } else {
                keystoreLocation = "file:///" + keystoreLocation;
            }
        }
        return appContext.getResource(keystoreLocation);
    }

    @Bean
    public Crypto createDecryptionCrypto() {
        CryptoFactoryBean factory = new CryptoFactoryBean();
        factory.setDefaultX509Alias(privateKeyAlias);
        factory.setKeyStorePassword(keystorePassword);
        try {
            Resource res = getKeystoreAsResource();
            factory.setKeyStoreLocation(res);

            // we cannot add anymore certificates with UCD, so we supply our own truststore...
            ClassPathResource truststore = new ClassPathResource("/truststore_soap.jks");
            Properties truststoreProps = new Properties();
            String pathToTruststore = truststore.getPath();
            LOGGER.info("WS-Security using truststore: {}", pathToTruststore);
            truststoreProps.put("org.apache.ws.security.crypto.merlin.truststore.file", pathToTruststore);
            truststoreProps.put("org.apache.ws.security.crypto.merlin.truststore.password", "secret");
            truststoreProps.put("org.apache.ws.security.crypto.merlin.truststore.type", "JKS");
            factory.setConfiguration(truststoreProps);

            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (Exception e) {
            LOGGER.warn("Could not get the keystore for incoming crypto setup (tomcat keystore)", e);
            throw new IllegalArgumentException(e);
        }
    }

}
