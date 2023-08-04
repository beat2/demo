package com.example.demo;

import ch.swissdec.schema.sd._20130514.salarydeclarationconsumercontainer.*;
import javax.xml.bind.JAXBElement;
import org.slf4j.*;
import org.springframework.ws.server.endpoint.annotation.*;


@Endpoint
public class SalaryDeclarationConsumerEndpoint {

    public static final String NAMESPACE_URI = "http://www.swissdec.ch/schema/sd/20130514/SalaryDeclarationConsumerServiceTypes";
    private static final Logger LOGGER = LoggerFactory.getLogger(SalaryDeclarationConsumerEndpoint.class);

    /**
     * PingConsumer
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "PingConsumer")
    @ResponsePayload
    public JAXBElement<PingConsumerResponseType> pingConsumer(@RequestPayload JAXBElement<PingConsumerType> request) {

        LOGGER.debug("*** Method pingConsumer() called");

        return null;
    }


    /**
     * DeclareSalaryConsumer
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeclareSalaryConsumer")
    @ResponsePayload
    public JAXBElement<DeclareSalaryConsumerResponseType> declareSalaryConsumer(
        @RequestPayload JAXBElement<DeclareSalaryConsumerType> request) {
        LOGGER.info("*** Method declareSalaryConsumer() called");

        return null;
    }

}
