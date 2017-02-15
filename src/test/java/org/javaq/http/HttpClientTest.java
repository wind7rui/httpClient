package org.javaq.http;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wangxq on 17/2/14.
 */
public class HttpClientTest {

    @Test
    public void should_return_success_code() throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        HttpClient httpClient = (HttpClient) applicationContext.getBean("httpClient");
        System.out.println(httpClient.sendGetRequestAsString("http://www.baissdu.com"));
    }
}
