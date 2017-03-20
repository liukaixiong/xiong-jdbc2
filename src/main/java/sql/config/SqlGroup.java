package sql.config;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 匹配SQL的xml配置文件的实体类
 * @author liuhx on 2016/12/14 09:44
 * @version V1.0
 * @email liuhx@elab-plus.com
 */
@XmlRootElement
public class SqlGroup {
    @XmlAttribute
    String name;
    @XmlElement(name = "sql")
    List<SqlItem> sqlItems = Lists.newArrayList();

    void addSqlgroup(SqlItem sqlGroup) {
        this.sqlItems.add(sqlGroup);
    }
}