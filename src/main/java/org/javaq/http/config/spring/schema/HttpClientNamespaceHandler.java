package org.javaq.http.config.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by wangxq on 17/2/14.
 */
public class HttpClientNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("httpClient", new HttpClientBeanDefinitionParser());
    }
}
