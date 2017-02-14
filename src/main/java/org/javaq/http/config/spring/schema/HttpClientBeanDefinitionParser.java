package org.javaq.http.config.spring.schema;

import org.javaq.http.HttpClient;
import org.javaq.http.core.SSLContextFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by wangxq on 17/2/14.
 */
public class HttpClientBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return HttpClient.class;
    }

    @Override
    protected String getBeanClassName(Element element) {
        return super.getBeanClassName(element);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        RuntimeBeanReference sslContextFactoryReference = buildSSLContextFactoryReference(element, parserContext);
        builder.addPropertyValue("sslContextFactory", sslContextFactoryReference);
    }

    private RuntimeBeanReference buildSSLContextFactoryReference(Element element, ParserContext parserContext) {
        String privateKeyPath = getAttributeValue(element, "privateKeyPath");
        String privateKeyPassword = getAttributeValue(element, "privateKeyPassword");
        String trustedCertsPath = getAttributeValue(element, "trustedCertsPath");
        String trustedStorePassword = getAttributeValue(element, "trustedStorePassword");
        String trustedHosts = getAttributeValue(element, "trustedHosts");

        RootBeanDefinition sslContextFactory = new RootBeanDefinition(SSLContextFactory.class);
        parserContext.getRegistry().registerBeanDefinition("sslContextFactory", sslContextFactory);
        sslContextFactory.getPropertyValues().add("privateKeyPath", privateKeyPath);
        sslContextFactory.getPropertyValues().add("privateKeyPassword", privateKeyPassword);
        sslContextFactory.getPropertyValues().add("trustedCertsPath", trustedCertsPath);
        sslContextFactory.getPropertyValues().add("trustedStorePassword", trustedStorePassword);
        sslContextFactory.getPropertyValues().add("trustedHosts", trustedHosts);

        parserContext.registerComponent(new BeanComponentDefinition(sslContextFactory, "sslContextFactory"));

        return new RuntimeBeanReference("sslContextFactory");
    }

    private String getAttributeValue(Element element, String attributeName) {
        return element.hasAttribute(attributeName) ? element.getAttribute(attributeName) : null;
    }
}
