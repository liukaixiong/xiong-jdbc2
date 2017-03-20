package sql.config;

import com.google.common.base.Throwables;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author liuhx on 2016/12/14 09:37
 * @version V1.0
 * @email liuhx@elab-plus.com
 */
public class JaxbKit {
    public static <T> T unmarshal(String src, Class<T> clazz) {
        T result = null;
        try {
            Unmarshaller avm = JAXBContext.newInstance(new Class[]{clazz}).createUnmarshaller();
            result = (T) avm.unmarshal(new StringReader(src));
        } catch (JAXBException e) {
            Throwables.propagate(e);
        }
        return result;
    }

    public static <T> T unmarshal(File xmlFile, Class<T> clazz) {
        T result = null;
        try {
            Unmarshaller avm = JAXBContext.newInstance(new Class[]{clazz}).createUnmarshaller();
            result = (T) avm.unmarshal(xmlFile);
        } catch (JAXBException e) {
            Throwables.propagate(e);
        }
        return result;
    }

    public static String marshal(Object jaxbElement) {
        StringWriter sw = null;
        try {
            Marshaller fm = JAXBContext.newInstance(new Class[]{jaxbElement.getClass()}).createMarshaller();
            fm.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
            sw = new StringWriter();
            fm.marshal(jaxbElement, sw);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }
}