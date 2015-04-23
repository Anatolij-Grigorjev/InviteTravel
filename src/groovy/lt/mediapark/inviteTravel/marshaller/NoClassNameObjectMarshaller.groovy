package com.wiseapps.travelline.marshallers

import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller
import org.codehaus.groovy.grails.web.json.JSONWriter
import org.springframework.beans.BeanUtils

import java.beans.PropertyDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Custom Marshaller to avoid class attribute in generated Json
 *
 * @author Denis Sergeev
 */
public class NoClassNameObjectMarshaller implements ObjectMarshaller<JSON> {

    @Override
    public boolean supports(Object object) {
        return !(object instanceof Collection) && !(object instanceof Map) && object!= null
    }

    @Override
    public void marshalObject(Object o, JSON json) throws
            ConverterException {
        JSONWriter writer = json.writer
        try {
            writer.object()
            BeanUtils.getPropertyDescriptors(o.getClass()).each {PropertyDescriptor property ->
                def name = property.name
                Method readMethod = property.readMethod
                if (readMethod && !(name in ['declaringClass','metaClass','class'])) {
                    def value
                    try {
                        value = readMethod.invoke(o, null)
                        if (value) {
                            writer.key(name)
                            json.convertAnother(value)
                        }
                    }
                    catch (e) {
                    }
                }
            }
            for (Field field : o.getClass().getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) &&
                        !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers))) {
                    writer.key(field.getName());
                    json.convertAnother(field.get(o));
                }
            }
            writer.endObject();
        }
        catch (ConverterException ce) {
            throw ce;
        }
        catch (Exception e) {
            throw new ConverterException(e);
        }
    }
}
