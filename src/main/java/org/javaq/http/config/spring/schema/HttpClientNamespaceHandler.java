package org.javaq.http.config.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


public class HttpClientNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("httpClient", new HttpClientBeanDefinitionParser());
    }
}
