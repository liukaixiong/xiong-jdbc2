package com.x.jdbc.sql.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author liuhx on 2016/12/14 09:45
 * @version V1.0
 * @email liuhx@elab-plus.com
 */
@XmlRootElement
public class SqlItem {
    @XmlAttribute
    String id;
    @XmlValue
    String value;
}