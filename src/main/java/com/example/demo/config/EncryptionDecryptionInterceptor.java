package com.example.demo.config;

import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import org.apache.wss4j.common.ConfigurationConstants;
import org.slf4j.*;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.DelegatingSmartSoapEndpointInterceptor;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Implementation of the
 * {@link org.springframework.ws.soap.server.SmartSoapEndpointInterceptor SmartSoapEndpointInterceptor} interface that
 * only intercepts requests that have a specified namespace URI or local part (or both) as payload root.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class EncryptionDecryptionInterceptor extends DelegatingSmartSoapEndpointInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionDecryptionInterceptor.class);
    private final String namespaceUri;
    private final List<String> localParts;
    private final TransformerHelper transformerHelper = new TransformerHelper();


    EncryptionDecryptionInterceptor(EndpointInterceptor delegate,
        String namespaceUri,
        List<String> localParts) {
        super(delegate);
        Assert.hasLength(namespaceUri, "namespaceUri can not be empty");
        this.namespaceUri = namespaceUri;
        this.localParts = localParts;
    }


    @Override
    protected boolean shouldIntercept(WebServiceMessage request, Object endpoint) {
        reconfigureSecurityActions();

        try {
            QName payloadRootName = PayloadRootUtils.getPayloadRootQName(request.getPayloadSource(), transformerHelper);
            if (payloadRootName == null || !namespaceUri.equals(payloadRootName.getNamespaceURI())) {
                return false;
            }
            for (String part : localParts) {
                if (part.equals(payloadRootName.getLocalPart())) {
                    return true;
                }
            }
            return false;

        } catch (TransformerException e) {
            return false;
        }
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        if (shouldIntercept(messageContext.getRequest(), endpoint)) {
            return super.handleRequest(messageContext, endpoint);
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (shouldIntercept(messageContext.getResponse(), endpoint)) {
            return super.handleResponse(messageContext, endpoint);
        }
        return true;
    }

    // Evaluates the current configured actions to be applied, based on the configuration from the admin
    // needed for plan b...
    private void reconfigureSecurityActions() {
        if (this.getDelegate() instanceof Wss4jSecurityInterceptor) {
            Wss4jSecurityInterceptor interceptor = (Wss4jSecurityInterceptor) this.getDelegate();
            interceptor.setValidationActions(
                ConfigurationConstants.TIMESTAMP + " " + ConfigurationConstants.SIGNATURE + " " + ConfigurationConstants.ENCRYPT);
            interceptor.setSecurementActions(ConfigurationConstants.ENCRYPT);
        }
    }

}
