package com.x.jdbc.template.binding;

import com.x.jdbc.template.IJDBCTemplate;
import org.apache.ibatis.io.Resources;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
    private IJDBCTemplate jdbcTemplate;

    public <T> DaoMethod(Class<?> mapperInterface, Method method, IJDBCTemplate jdbcTemplate) {
        this.mapperInterface = mapperInterface;
        this.method = method;
        this.jdbcTemplate = jdbcTemplate;
    }


    public Object execute(IJDBCTemplate jdbcTemplate, Object[] args) {
        String methodName = method.getName();
        String statementName = mapperInterface.getName() + "." + methodName;
//        String sql = SqlKit.sql(statementName);
        if (methodName.startsWith("insert")) {
            return this.jdbcTemplate.executeInsert(statementName, args);
        } else if (methodName.startsWith("update") || statementName.startsWith("delete")) {
            return this.jdbcTemplate.executeUpdate(statementName, args);
        } else {
            if (!methodName.startsWith("select")) {
                System.out.println("语句类型有误!");
                return null;
            }
            Class<?> returnType = this.method.getReturnType();
            boolean returnsVoid = Void.TYPE.equals(returnType);
            boolean returnsMany = (returnType == List.class);
            boolean returnsMap = (returnType == Map.class);
            int argsLength = args.length;
            Object argObject = null;

            // TODO 待优化 , 可以获取该方法的全称,然后去解析里面的参数名称
            if (argsLength != 1) {
                throw new IllegalArgumentException(methodName + " 方法 参数尽量保持在1个,可以是Map,可以是Object");
            }

            if (returnsMany) {
                Type type = this.method.getGenericReturnType();
                Class typeClass = getReturnType(type.getTypeName());
                if (typeClass == Map.class) {
                    return this.jdbcTemplate.executeQueryforListMap(statementName, args[0]);
                } else {
                    return this.jdbcTemplate.executeQueryList(statementName, args[0], typeClass);
                }
            } else if (returnsMap) {
                return this.jdbcTemplate.executeQueryMap(statementName, returnType);
            } else {
                return this.jdbcTemplate.executeQueryObject(statementName, args[0], returnType);
            }
        }
    }


    private Class getReturnType(String listString) {
        int startIdnex = listString.indexOf("<") + 1;
        int endIndex = listString.indexOf(">");
        String classType = listString.substring(startIdnex, endIndex);
        try {
            return Resources.classForName(classType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
