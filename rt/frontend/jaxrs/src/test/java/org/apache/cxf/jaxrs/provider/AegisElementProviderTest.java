/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.jaxrs.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.cxf.jaxrs.fortest.AegisTestBean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class AegisElementProviderTest extends Assert {
    
    private static final String SIMPLE_BEAN_XML 
        = "<?xml version='1.0' encoding='UTF-8'?>" 
           + "<ns1:AegisTestBean xmlns:ns1=\"http://fortest.jaxrs.cxf.apache.org\""
           + " xmlns:ns2=\"http://www.w3.org/2001/XMLSchema-instance\""
           + " ns2:type=\"ns1:AegisTestBean\">"
           + "<ns1:boolValue>true</ns1:boolValue><ns1:strValue>hovercraft</ns1:strValue></ns1:AegisTestBean>";

    @After
    public void clearCache() {
        AbstractAegisProvider.clearContexts();
    }
    
    @Test
    public void testIsWriteable() {
        MessageBodyWriter<Object> p = new AegisElementProvider();
        assertTrue(p.isWriteable(AegisTestBean.class, null, null, null));
    }
    
    @Test
    public void testIsReadable() {
        MessageBodyReader<Object> p = new AegisElementProvider();
        assertTrue(p.isReadable(AegisTestBean.class, null, null, null));
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void testReadFrom() throws Exception {
        MessageBodyReader<Object> p = new AegisElementProvider();
        byte[] simpleBytes = SIMPLE_BEAN_XML.getBytes("utf-8");
        Object beanObject = p.readFrom((Class)AegisTestBean.class, null, null, 
                                          null, null, new ByteArrayInputStream(simpleBytes));
        AegisTestBean bean = (AegisTestBean) beanObject;
        assertEquals("hovercraft", bean.getStrValue());
        assertEquals(Boolean.TRUE, bean.getBoolValue());
    }
    
    @Test
    public void testWriteTo() throws Exception {
        MessageBodyWriter<Object> p = new AegisElementProvider();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AegisTestBean bean = new AegisTestBean();
        bean.setBoolValue(Boolean.TRUE);
        bean.setStrValue("hovercraft");
        p.writeTo(bean, null, null, null, null, null, os);
        byte[] bytes = os.toByteArray();
        String xml = new String(bytes, "utf-8");
        assertEquals(SIMPLE_BEAN_XML, xml);
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void testReadWriteComplexMap() throws Exception {
        Map<AegisTestBean, AegisSuperBean> testMap = 
            new HashMap<AegisTestBean, AegisSuperBean>();
        
        Class<InterfaceWithMap> iwithMapClass = InterfaceWithMap.class;
        Method method = iwithMapClass.getMethod("mapFunction");
        Type mapType = method.getGenericReturnType();

        AegisTestBean bean = new AegisTestBean();
        bean.setBoolValue(Boolean.TRUE);
        bean.setStrValue("hovercraft");
        
        AegisSuperBean bean2 = new AegisSuperBean();
        bean2.setBoolValue(Boolean.TRUE);
        bean2.setStrValue("hovercraft2");
        testMap.put(bean, bean2);
        
        MessageBodyWriter<Object> writer = new AegisElementProvider();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        writer.writeTo(testMap, testMap.getClass(), mapType, null, null, null, os);
        byte[] bytes = os.toByteArray();
        String xml = new String(bytes, "utf-8");
        MessageBodyReader<Object> reader = new AegisElementProvider();         
        byte[] simpleBytes = xml.getBytes("utf-8");
        
        Object beanObject = reader.readFrom((Class)Map.class, mapType, null, 
                                          null, null, new ByteArrayInputStream(simpleBytes));
        Map<AegisTestBean, AegisSuperBean> map2 = (Map)beanObject;
        assertEquals(1, map2.size());
        Map.Entry<AegisTestBean, AegisSuperBean> entry = map2.entrySet().iterator().next();
        AegisTestBean bean1 = entry.getKey();
        assertEquals("hovercraft", bean1.getStrValue());
        assertEquals(Boolean.TRUE, bean1.getBoolValue());
        AegisTestBean bean22 = entry.getValue();
        assertEquals("hovercraft2", bean22.getStrValue());
        assertEquals(Boolean.TRUE, bean22.getBoolValue());
        
    }
    
    public static class AegisSuperBean extends AegisTestBean {
    }
    
    private static interface InterfaceWithMap {
        Map<AegisTestBean, AegisSuperBean> mapFunction();
    }
    
}
