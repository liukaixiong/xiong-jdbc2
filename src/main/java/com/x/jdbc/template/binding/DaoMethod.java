package com.x.jdbc.template.binding;

import com.x.jdbc.sql.ConfigurableFactory;
import com.x.jdbc.sql.config.SqlCommandType;
import com.x.jdbc.template.IBaseDaoSupport;
import com.x.jdbc.template.IJdbcTemplate;
import com.x.jdbc.template.base.EntityOperation;
import com.x.jdbc.template.common.utils.BeanUtils;
import com.x.jdbc.template.common.utils.ObjectUtils;
import com.x.jdbc.template.common.utils.StringUtils;
import com.x.jdbc.template.row.JoinRowMapper;
import com.x.jdbc.template.sql.method.DefaultSQLBuilderSupport;
import com.x.jdbc.template.sql.method.ISQLBuliderSupport;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dao层方法拦截根据指定的类型调用方法
 * <p>
 * 具体dao类执行的方法,根据方法名进行判断类型
 * </p>
 *
 * @author Liukx
 * @create 2018-01-03 11:36
 * @email liukx@elab-plus.com
 **/
public class DaoMethod {

    // 具体的接口
    private Class<?> mapperInterface;

    // 需要执行的方法
    private Method method;

    // 具体执行dao层的方法
    private IJdbcTemplate jdbcTemplate;

    // dao执行类的父类泛型
    private Class<?> supperGenricType;
    // 父类的interfaceName
    private String supperInterfaceName;
    // 实体操作对象
    private EntityOperation entityOperation;
    // sql配置文件对象
    private ConfigurableFactory configurableFactory;

    private Class<?> exampleType;
    // sql的构建接口
    private List<ISQLBuliderSupport> sqlBuliderSupportList = new ArrayList<ISQLBuliderSupport>();

    private Map<String, Class<?>> methodReturnCacheMap = new ConcurrentHashMap<String, Class<?>>();

    private String defaultSupperInterface = IBaseDaoSupport.class.getName();

    /**
     * 构建一个DaoMethod的方法对象
     *
     * @param mapperInterface     dao的接口類
     * @param method              方法信息
     * @param jdbcTemplate        jdbc操作类
     * @param configurableFactory 配置工厂
     * @param <T>
     */
    public <T> DaoMethod(Class<?> mapperInterface, Method method, IJdbcTemplate jdbcTemplate, ConfigurableFactory configurableFactory) {
        this.mapperInterface = mapperInterface;
        this.method = method;
        this.jdbcTemplate = jdbcTemplate;
        this.configurableFactory = configurableFactory;
        //默认先将初始化的sql构建加入进来
        this.sqlBuliderSupportList.add(new DefaultSQLBuilderSupport());
        // 类的信息提取
        // 父类的接口类
        this.supperInterfaceName = mapperInterface.getGenericInterfaces()[0].getTypeName();
        // 泛型对象
        List<String> classGenricTypeString = BeanUtils.getClassGenricTypeString(this.supperInterfaceName);
        // 第一个db对象
        this.supperGenricType = BeanUtils.getClassByString(classGenricTypeString.get(0));
        // 第二个,案例对象
        this.exampleType = classGenricTypeString.size() > 1 ? BeanUtils.getClassByString(classGenricTypeString.get(1)) : null;
        // 实体操作类
        this.entityOperation = new EntityOperation(this.supperGenricType, null);
    }

    /**
     * 具体执行sql的方法
     * 1. 判断sql的类型
     * 2. 根据类型判断调用的方法
     * 3.
     *
     * @param args
     * @return
     */
    public Object execute(Object[] args) throws Exception {
        int argsLength = args.length;
        String sqlSource = method.getName();
        String statementName = mapperInterface.getName() + "." + method.getName();
        // TODO 待优化 , 可以获取该方法的全称,然后去解析里面的参数名称
        if (argsLength != 1) {
            throw new IllegalArgumentException(sqlSource + " 方法 参数尽量保持在1个,可以是Map,可以是Object");
        }
        Object arg = args[0];
        SqlCommandType sqlType = this.configurableFactory.getSqlConfigurableFactory().getSqlType(statementName);
        if (sqlType == null) {
            // 表示配置文件中没有找到,则会从父类接口去获取
            if (!StringUtils.isNull(this.supperInterfaceName) && this.supperInterfaceName.startsWith(defaultSupperInterface)) {
                // 如果是查找类型的方法
                if ("find".equals(method.getName())) {
                    return findMethodInvoke(arg);
                }
                // 如果是其他增删改类型的方法
                if (createSql(statementName)) {
                    return execute(args);
                }
            }
            // 抛出异常
            throw new Exception(" 找不到[" + statementName + "]对应的处理sql");
        } else {
            String sql = this.configurableFactory.getSqlConfigurableFactory().getSql(statementName);
            if (sqlType == SqlCommandType.INSERT) {
                return this.jdbcTemplate.executeInsert(sql, arg);
            } else if (sqlType == SqlCommandType.UPDATE || sqlType == SqlCommandType.DELETE) {
                return this.jdbcTemplate.executeUpdate(sql, arg);
            } else {
                if (sqlType != SqlCommandType.SELECT) {
                    System.out.println("语句类型有误!");
                    return null;
                }
                return selectInvokeMehtod(sql, arg);
            }
        }
    }

    /**
     * 执行特殊的find的方法处理过程
     *
     * @param arg
     * @return
     */
    private Object findMethodInvoke(Object arg) {
        List oredCriteriaList = (List) BeanUtils.invokegetGetterMethod(arg, "oredCriteria");
        List<Object> paramsList = new LinkedList<Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("select " + entityOperation.allColumn + " from " + entityOperation.tableName + " ");

        if (oredCriteriaList.size() > 0) {
            for (int i = 0; i < oredCriteriaList.size(); i++) {
                Object o = oredCriteriaList.get(i);
                List criteriaList = (List) BeanUtils.invokegetGetterMethod(o, "criteria");
                boolean flag = true;
                for (int j = 0; j < criteriaList.size(); j++) {
                    if (flag) {
                        sb.append(" where ");
                        flag = false;
                    } else {
                        sb.append(" and ");
                    }
                    Object o2 = criteriaList.get(j);
                    // key 条件
                    String condition = (String) BeanUtils.invokegetGetterMethod(o2, "condition");
                    // 对应的值
                    Object valueObject = BeanUtils.invokegetGetterMethod(o2, "value");
                    Object secondValue = BeanUtils.invokegetGetterMethod(o2, "secondValue");
                    // 如果是在集合的话需要单独处理  比如 in
                    if (valueObject instanceof List) {
                        List valueObject1 = (List) valueObject;
                        sb.append(condition + " ( ");
                        flag = false;
                        for (int k = 0; k < valueObject1.size(); k++) {
                            if (flag) {
                                sb.append(" , ");
                            }
                            Object o1 = valueObject1.get(i);
                            paramsList.add(o1);
                            sb.append(" ? ");
                        }
                        sb.append(" )");
                    } else {

                        sb.append(condition);

                        // is null , is not null
                        if (!ObjectUtils.isNull(valueObject)) {
                            paramsList.add(valueObject);
                            sb.append(" ? ");
                        }

                        // between 的情况
                        if (!ObjectUtils.isNull(secondValue)) {
                            sb.append(" and ? ");
                            paramsList.add(secondValue);
                        }
                    }
                    System.out.println();
                }
            }
        }
        Object[] objects = paramsList.toArray();
        return this.jdbcTemplate.query(sb.toString(), entityOperation.rowMapper, objects);
    }


    private String removeFirstAnd(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return sql;
        }
        return sql.trim().toLowerCase().replaceAll("^\\s*and", "") + " ";
    }


    /**
     * 查询执行方法
     *
     * @param sql
     * @param arg
     * @return
     */
    private Object selectInvokeMehtod(String sql, Object arg) throws Exception {
        Class<?> returnType = this.method.getReturnType();
        boolean returnsVoid = Void.TYPE.equals(returnType);
        boolean returnsMany = (returnType == List.class);
        boolean returnsMap = (returnType == Map.class);

        // 是否是返回多个对象,例如List
        if (returnsMany) {
            Class typeClass = getCacheMethodReturnClass();
            if (typeClass == Map.class) {
                return this.jdbcTemplate.executeQueryforListMap(sql, arg);
            } else {
                return this.jdbcTemplate.executeQueryList(sql, arg, typeClass);
            }
        } else if (returnsMap) {
            return this.jdbcTemplate.executeQueryMap(sql, returnType);
        } else {
            if (returnType == Object.class) {
                returnType = supperGenricType;
            }
            JoinRowMapper<?> joinRowMapper = JoinRowMapper.newInstance(jdbcTemplate, this.configurableFactory, returnType);
            return this.jdbcTemplate.executeQueryObject(sql, arg, joinRowMapper);

//            PropertiesProxyFactory propertiesProxyFactory = new PropertiesProxyFactory();
//            Object proxy = propertiesProxyFactory.createProxy(jdbcTemplate, supperGenricType, list);
            // 判断返回值中的数据是否带有join关联
//            return this.jdbcTemplate.executeQueryObject(sql, arg, returnType);
        }
    }

    /**
     * 获取方法返回值
     *
     * @return
     */
    private Class getCacheMethodReturnClass() {
        String methodName = this.method.getName();
        Class typeClass = methodReturnCacheMap.get(methodName);
        if (typeClass == null) {
            Type type = this.method.getGenericReturnType();
            typeClass = BeanUtils.getClassGenricType(type.getTypeName());
            // 可能是父类inteface的方法的情况
            if (typeClass == null) {
                typeClass = this.supperGenricType;
            } else {
                methodReturnCacheMap.put(methodName, typeClass);
            }
        }
        return typeClass;
    }

    /**
     * 创建sql的执行方法
     *
     * @param statementName
     * @return
     * @throws Exception
     */
    private boolean createSql(String statementName) throws Exception {
        List<ISQLBuliderSupport> sqlBuliderSupportList = getSqlBuliderSupportList();
        String sql = "";
        boolean isSQLNotNull = false;
        for (int i = 0; i < sqlBuliderSupportList.size(); i++) {
            // 执行每个sql构建器来匹配
            ISQLBuliderSupport sqlBuliderSupport = sqlBuliderSupportList.get(i);
            sql = sqlBuliderSupport.doCreateSQL(entityOperation, method);
            if (!StringUtils.isEmpty(sql)) {
                isSQLNotNull = true;
                break;
            }
        }

        // 注册到sql容器中
        this.configurableFactory.getSqlConfigurableFactory().registerSqlMap(statementName, sql);
        return isSQLNotNull;
    }


    public List<ISQLBuliderSupport> getSqlBuliderSupportList() {
        return sqlBuliderSupportList;
    }

    public void setSqlBuliderSupportList(List<ISQLBuliderSupport> sqlBuliderSupportList) {
        this.sqlBuliderSupportList = sqlBuliderSupportList;
    }
}
