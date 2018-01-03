/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.x.jdbc.template.params;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Helper methods for named parameter parsing.
 * <p>
 * <p>Only intended for internal use within Spring's JDBC framework.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class NamedParameterUtils2 {
    /**
     * Logger available to subclasses
     */
    /**
     * Set of characters that qualify as parameter separators,
     * indicating that a parameter name in a SQL String has ended.
     */
    private static final char[] PARAMETER_SEPARATORS =
            new char[]{'"', '\'', ':', '&', ',', ';', '(', ')', '|', '=', '+', '-', '*', '%', '/', '\\', '<', '>', '^'};

    /**
     * Set of characters that qualify as comment or quotes starting characters.
     */
    private static final String[] START_SKIP =
            new String[]{"'", "\"", "--", "/*"};

    /**
     * Set of characters that at are the corresponding comment or quotes ending characters.
     */
    private static final String[] STOP_SKIP =
            new String[]{"'", "\"", "\n", "*/"};


    //-------------------------------------------------------------------------
    // Core methods used by NamedParameterJdbcTemplate and SqlQuery/SqlUpdate
    //-------------------------------------------------------------------------

    /**
     * Parse the SQL statement and locate any placeholders or named parameters.
     * Named parameters are substituted for a JDBC placeholder.
     *
     * @param sql the SQL statement
     * @return the parsed statement, represented as ParsedSql2 instance
     */
    public static ParsedSql2 parseSqlStatement(final String sql) {
        Assert.notNull(sql, "SQL must not be null");

        Set<String> namedParameters = new HashSet<String>();
        String sqlToUse = sql;
        List<ParameterHolder> parameterList = new ArrayList<ParameterHolder>();

        char[] statement = sql.toCharArray();
        int namedParameterCount = 0;
        int unnamedParameterCount = 0;
        int totalParameterCount = 0;

        int escapes = 0;
        int i = 0;
        while (i < statement.length) {
            int skipToPosition = i;
            while (i < statement.length) {
                // 跳过注释
                skipToPosition = skipCommentsAndQuotes(statement, i);
                if (i == skipToPosition) {
                    break;
                } else {
                    i = skipToPosition;
                }
            }
            // 如果sql中都是注释的话,则停止循环
            if (i >= statement.length) {
                break;
            }
            // 开始执行sql语句
            char c = statement[i];
            if (c == ':' || c == '&') {
                int j = i + 1;
                if (j < statement.length && statement[j] == ':' && c == ':') {
                    // Postgres-style "::" casting operator - to be skipped.
                    i = i + 2;
                    continue;
                }
                String parameter = null;
                if (j < statement.length && c == ':' && statement[j] == '{') {
                    // :{x} style parameter
                    while (j < statement.length && !('}' == statement[j])) {
                        j++;
                        if (':' == statement[j] || '{' == statement[j]) {
                            throw new InvalidDataAccessApiUsageException("Parameter name contains invalid character '" +
                                    statement[j] + "' at position " + i + " in statement: " + sql);
                        }
                    }
                    if (j >= statement.length) {
                        throw new InvalidDataAccessApiUsageException(
                                "Non-terminated named parameter declaration at position " + i + " in statement: " + sql);
                    }
                    if (j - i > 3) {
                        parameter = sql.substring(i + 2, j);
                        namedParameterCount = addNewNamedParameter(namedParameters, namedParameterCount, parameter);
                        totalParameterCount = addNamedParameter(parameterList, totalParameterCount, escapes, i, j + 1, parameter);
                    }
                    j++;
                } else {
                    while (j < statement.length && !isParameterSeparator(statement[j])) {
                        j++;
                    }
                    if (j - i > 1) {
                        parameter = sql.substring(i + 1, j);
                        namedParameterCount = addNewNamedParameter(namedParameters, namedParameterCount, parameter);
                        totalParameterCount = addNamedParameter(parameterList, totalParameterCount, escapes, i, j, parameter);
                    }
                }
                i = j - 1;
            } else {
                if (c == '\\') {
                    int j = i + 1;
                    if (j < statement.length && statement[j] == ':') {
                        // this is an escaped : and should be skipped
                        sqlToUse = sqlToUse.substring(0, i - escapes) + sqlToUse.substring(i - escapes + 1);
                        escapes++;
                        i = i + 2;
                        continue;
                    }
                }
                if (c == '?') {
                    unnamedParameterCount++;
                    totalParameterCount++;
                }
            }
            i++;
        }
        ParsedSql2 parsedSql = new ParsedSql2(sqlToUse);
        for (ParameterHolder ph : parameterList) {
            parsedSql.addNamedParameter(ph.getParameterName(), ph.getStartIndex(), ph.getEndIndex());
        }
        parsedSql.setNamedParameterCount(namedParameterCount);
        parsedSql.setUnnamedParameterCount(unnamedParameterCount);
        parsedSql.setTotalParameterCount(totalParameterCount);
        return parsedSql;
    }

    private static int addNamedParameter(
            List<ParameterHolder> parameterList, int totalParameterCount, int escapes, int i, int j, String parameter) {

        parameterList.add(new ParameterHolder(parameter, i - escapes, j - escapes));
        totalParameterCount++;
        return totalParameterCount;
    }

    private static int addNewNamedParameter(Set<String> namedParameters, int namedParameterCount, String parameter) {
        if (!namedParameters.contains(parameter)) {
            namedParameters.add(parameter);
            namedParameterCount++;
        }
        return namedParameterCount;
    }

    /**
     * Skip over comments and quoted names present in an SQL statement
     *
     * @param statement character array containing SQL statement
     * @param position  current position of statement
     * @return next position to process after any comments or quotes are skipped
     */
    private static int skipCommentsAndQuotes(char[] statement, int position) {
        for (int i = 0; i < START_SKIP.length; i++) {
            if (statement[position] == START_SKIP[i].charAt(0)) {
                boolean match = true;
                for (int j = 1; j < START_SKIP[i].length(); j++) {
                    if (!(statement[position + j] == START_SKIP[i].charAt(j))) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    int offset = START_SKIP[i].length();
                    for (int m = position + offset; m < statement.length; m++) {
                        if (statement[m] == STOP_SKIP[i].charAt(0)) {
                            boolean endMatch = true;
                            int endPos = m;
                            for (int n = 1; n < STOP_SKIP[i].length(); n++) {
                                if (m + n >= statement.length) {
                                    // last comment not closed properly
                                    return statement.length;
                                }
                                if (!(statement[m + n] == STOP_SKIP[i].charAt(n))) {
                                    endMatch = false;
                                    break;
                                }
                                endPos = m + n;
                            }
                            if (endMatch) {
                                // found character sequence ending comment or quote
                                return endPos + 1;
                            }
                        }
                    }
                    // character sequence ending comment or quote not found
                    return statement.length;
                }

            }
        }
        return position;
    }

    /**
     * Parse the SQL statement and locate any placeholders or named parameters. Named
     * parameters are substituted for a JDBC placeholder, and any select list is expanded
     * to the required number of placeholders. Select lists may contain an array of
     * objects, and in that case the placeholders will be grouped and enclosed with
     * parentheses. This allows for the use of "expression lists" in the SQL statement
     * like: <br /><br />
     * {@code select id, name, state from table where (name, age) in (('John', 35), ('Ann', 50))}
     * <p>The parameter values passed in are used to determine the number of placeholders to
     * be used for a select list. Select lists should be limited to 100 or fewer elements.
     * A larger number of elements is not guaranteed to be supported by the database and
     * is strictly vendor-dependent.
     *
     * @param parsedSql   the parsed representation of the SQL statement
     * @param paramSource the source for named parameters
     * @return the SQL statement with substituted parameters
     * @see #parseSqlStatement
     */
    public static String substituteNamedParameters(ParsedSql2 parsedSql, SqlParameterSource paramSource) {
        String originalSql = parsedSql.getOriginalSql();
        StringBuilder actualSql = new StringBuilder();
        List<String> paramNames = parsedSql.getParameterNames();
        int lastIndex = 0;
        int paramsCount = 0;
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            int[] indexes = parsedSql.getParameterIndexes(i);
            int startIndex = indexes[0];
            int endIndex = indexes[1];

            if (paramSource != null && paramSource.hasValue(paramName)) {
                Object value = paramSource.getValue(paramName);
                if (value == null) {
                    lastIndex = endIndex;
                    continue;
                }
                actualSql.append(originalSql, lastIndex, startIndex);
                if (value instanceof SqlParameterValue) {
                    value = ((SqlParameterValue) value).getValue();
                }
                if (value instanceof Collection) {
                    Iterator<?> entryIter = ((Collection<?>) value).iterator();
                    int k = 0;
                    while (entryIter.hasNext()) {
                        if (k > 0) {
                            actualSql.append(", ");
                        }
                        k++;
                        Object entryItem = entryIter.next();
                        if (entryItem instanceof Object[]) {
                            Object[] expressionList = (Object[]) entryItem;
                            actualSql.append("(");
                            for (int m = 0; m < expressionList.length; m++) {
                                if (m > 0) {
                                    actualSql.append(", ");
                                }
                                actualSql.append("?");
                            }
                            actualSql.append(")");
                        } else {
                            actualSql.append("?");
                        }
                    }
                } else if (value instanceof Object[]) {
                    Object[] expressionList = (Object[]) value;
                    actualSql.append("(");
                    for (int m = 0; m < expressionList.length; m++) {
                        if (m > 0) {
                            actualSql.append(", ");
                        }
                        actualSql.append("?");
                    }
                    actualSql.append(")");
                } else {
//                    //校验第一个参数是否合法
//                    if (paramsCount == 0) {
//                        String firstCheckSql = originalSql.substring(lastIndex, startIndex);
//                        int errorPosition = validFristParamsIsNullSql(firstCheckSql, ",");
//                        lastIndex = lastIndex + errorPosition;
//                    }
                    // TODO 将null参数直接替换掉
                    actualSql.append("?");
                    paramsCount++;
                }
            } else {
//                // 如果第一个参数为空的情况下
//                if (actualSql.toString().trim().length() == 0) {
//                    actualSql.append(getFrontSql(originalSql));
//                }
                actualSql.append(" ");
            }
            lastIndex = endIndex;
        }
        actualSql.append(originalSql, lastIndex, originalSql.length());
        return actualSql.toString();
    }

    private static int validFristParamsIsNullSql(String sql, String wrongfulSymbol) {
        int i = sql.indexOf(wrongfulSymbol);
        if (i > -1) {
            i = i + wrongfulSymbol.length();
        } else {
            i = 0;
        }
        return i;
    }

//    private static String replaceDynamicStr(String sql){
//        int indexNo = sql.indexOf("$");
//        int kg = sql.indexOf("\r", indexNo);
//        int hh = sql.indexOf("\n",indexNo);
//
//    }


    public static String replaceDynamic(String sql, SqlParameterSource mapSqlParameterSource) {
        int indexNo = sql.indexOf("$");
        if (indexNo > 0) {
            int index = 0;
            for (int i = indexNo; i < sql.length(); i++) {
                indexNo = sql.indexOf("$", index + 1);
                if (indexNo < 0) {
                    break;
                }
                int kg = sql.indexOf("\r", indexNo - 1);
                int hh = sql.indexOf(" ", indexNo);
                String table = "";
                if (kg > hh) {
                    table = sql.substring(indexNo + 1, kg);
                } else if (hh > kg) {
                    table = sql.substring(indexNo + 1, hh);
                } else {
                    table = sql.substring(indexNo + 1, sql.length());
                    indexNo = sql.length();
                }
                index = indexNo;
                Object value = null;
                try {
                    value = mapSqlParameterSource.getValue(table.trim());
                } catch (Exception e) {
                    System.err.println(" !!!!!!!!动态参数为必填项 : $"+table.trim()+" 必须为它赋值 .!!!!!!!!!!!!!");
                    e.printStackTrace();
                }
                sql = sql.replace("$" + table.trim(), value.toString());
            }
        }
        return sql;
    }

    /**
     * 检查sql第一个参数为null的情况下,进行sql校验
     *
     * @param sql
     * @param s
     * @param parsedSql
     * @param mapSqlParameterSource
     * @return
     */
    public static String validCheckSql(String sql, String s, ParsedSql2 parsedSql, SqlParameterSource mapSqlParameterSource) {


        if (parsedSql.getParameterNames().size() > 0) {
            // 如果是update的情况下
            String s1 = parsedSql.getParameterNames().get(0);
            // 第一个参数为空的情况下
            if (!mapSqlParameterSource.hasValue(s1) || mapSqlParameterSource.getValue(s1) == null) {
                if (sql.trim().startsWith("update")) {
                    if (s.trim().startsWith(",")) {
                        s = s.replaceFirst(",", " ");
                        String firstSql = sql.substring(0, sql.indexOf("set") + 3);
                        s = firstSql + s;
                    }
                } else if (sql.trim().startsWith("select")) {
                    String firstSql = sql.substring(0, sql.indexOf("where") + 5) + " 1=1 ";
                    s = firstSql + s;
                }
            }
//            else { // 如果是查询的情况下
//                if (sql.trim().startsWith("select")) {
//                    String firstSql = sql.substring(0, sql.indexOf("where ") - 5);
//                    s = firstSql + s;
//                }
//            }
        }
        return s;
    }

    /**
     * Convert a Map of named parameter values to a corresponding array.
     *
     * @param parsedSql      the parsed SQL statement
     * @param paramSource    the source for named parameters
     * @param declaredParams the List of declared SqlParameter objects
     *                       (may be {@code null}). If specified, the parameter metadata will
     *                       be built into the value array in the form of SqlParameterValue objects.
     * @return the array of values
     */
    public static Object[] buildValueArray(
            ParsedSql2 parsedSql, SqlParameterSource paramSource, List<SqlParameter> declaredParams) {
        List<Object> dataList = new ArrayList<Object>();
//        Object[] paramArray = new Object[parsedSql.getTotalParameterCount()];
        if (parsedSql.getNamedParameterCount() > 0 && parsedSql.getUnnamedParameterCount() > 0) {
            throw new InvalidDataAccessApiUsageException(
                    "Not allowed to mix named and traditional ? placeholders. You have " +
                            parsedSql.getNamedParameterCount() + " named parameter(s) and " +
                            parsedSql.getUnnamedParameterCount() + " traditional placeholder(s) in statement: " +
                            parsedSql.getOriginalSql());
        }
        List<String> paramNames = parsedSql.getParameterNames();
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            try {
                Object value = paramSource.getValue(paramName);
                if (value == null) {
                    continue;
                }
                // 如果是集合的情况 类似于批量添加的时候
                else if (value instanceof Collection) {
                    Iterator<?> entryIter = ((Collection<?>) value).iterator();
                    while (entryIter.hasNext()) {
                        Object entryItem = entryIter.next();
                        dataList.add(entryItem);
                    }
                }
                // 如果是数组的情况 类似与 in (1,2,3)的时候
                else if (value instanceof Object[]) {
                    for (int j = 0; j < ((Object[]) value).length; j++) {
                        dataList.add(((Object[]) value)[j]);
                    }
                } else {
                    dataList.add(value);
                }
//                SqlParameter param = findParameter(declaredParams, paramName, i);
//                paramArray[count] = (param != null ? new SqlParameterValue(param, value) : value);
            } catch (IllegalArgumentException ex) {
//                throw new InvalidDataAccessApiUsageException(
//                        "No value supplied for the SQL parameter '" + paramName + "': " + ex.getMessage());
            }
        }
        Object[] paramArray = dataList.toArray();
        return paramArray;
    }

    /**
     * Find a matching parameter in the given list of declared parameters.
     *
     * @param declaredParams the declared SqlParameter objects
     * @param paramName      the name of the desired parameter
     * @param paramIndex     the index of the desired parameter
     * @return the declared SqlParameter, or {@code null} if none found
     */
    private static SqlParameter findParameter(List<SqlParameter> declaredParams, String paramName, int paramIndex) {
        if (declaredParams != null) {
            // First pass: Look for named parameter match.
            for (SqlParameter declaredParam : declaredParams) {
                if (paramName.equals(declaredParam.getName())) {
                    return declaredParam;
                }
            }
            // Second pass: Look for parameter index match.
            if (paramIndex < declaredParams.size()) {
                SqlParameter declaredParam = declaredParams.get(paramIndex);
                // Only accept unnamed parameters for index matches.
                if (declaredParam.getName() == null) {
                    return declaredParam;
                }
            }
        }
        return null;
    }

    /**
     * Determine whether a parameter name ends at the current position,
     * that is, whether the given character qualifies as a separator.
     */
    private static boolean isParameterSeparator(char c) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        for (char separator : PARAMETER_SEPARATORS) {
            if (c == separator) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert parameter types from an SqlParameterSource into a corresponding int array.
     * This is necessary in order to reuse existing methods on JdbcTemplate.
     * Any named parameter types are placed in the correct position in the
     * Object array based on the parsed SQL statement info.
     *
     * @param parsedSql   the parsed SQL statement
     * @param paramSource the source for named parameters
     */
    public static int[] buildSqlTypeArray(ParsedSql2 parsedSql, SqlParameterSource paramSource) {
        int[] sqlTypes = new int[parsedSql.getTotalParameterCount()];
        List<String> paramNames = parsedSql.getParameterNames();
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            sqlTypes[i] = paramSource.getSqlType(paramName);
        }
        return sqlTypes;
    }

    /**
     * Convert parameter declarations from an SqlParameterSource to a corresponding List of SqlParameters.
     * This is necessary in order to reuse existing methods on JdbcTemplate.
     * The SqlParameter for a named parameter is placed in the correct position in the
     * resulting list based on the parsed SQL statement info.
     *
     * @param parsedSql   the parsed SQL statement
     * @param paramSource the source for named parameters
     */
    public static List<SqlParameter> buildSqlParameterList(ParsedSql2 parsedSql, SqlParameterSource paramSource) {
        List<String> paramNames = parsedSql.getParameterNames();
        List<SqlParameter> params = new LinkedList<SqlParameter>();
        for (String paramName : paramNames) {
            params.add(new SqlParameter(paramName, paramSource.getSqlType(paramName), paramSource.getTypeName(paramName)));
        }
        return params;
    }


    //-------------------------------------------------------------------------
    // Convenience methods operating on a plain SQL String
    //-------------------------------------------------------------------------

    /**
     * Parse the SQL statement and locate any placeholders or named parameters.
     * Named parameters are substituted for a JDBC placeholder.
     * <p>This is a shortcut version of
     * {@link #parseSqlStatement(String)} in combination with
     * {@link #substituteNamedParameters(ParsedSql2, SqlParameterSource)}.
     *
     * @param sql the SQL statement
     * @return the actual (parsed) SQL statement
     */
    public static String parseSqlStatementIntoString(String sql) {
        ParsedSql2 parsedSql = parseSqlStatement(sql);
        return substituteNamedParameters(parsedSql, null);
    }

    /**
     * Parse the SQL statement and locate any placeholders or named parameters.
     * Named parameters are substituted for a JDBC placeholder and any select list
     * is expanded to the required number of placeholders.
     * <p>This is a shortcut version of
     * {@link #substituteNamedParameters(ParsedSql2, SqlParameterSource)}.
     *
     * @param sql         the SQL statement
     * @param paramSource the source for named parameters
     * @return the SQL statement with substituted parameters
     */
    public static String substituteNamedParameters(String sql, SqlParameterSource paramSource) {
        ParsedSql2 parsedSql = parseSqlStatement(sql);
        return substituteNamedParameters(parsedSql, paramSource);
    }

    /**
     * Convert a Map of named parameter values to a corresponding array.
     * <p>This is a shortcut version of
     * {@link #buildValueArray(ParsedSql2, SqlParameterSource, java.util.List)}.
     *
     * @param sql      the SQL statement
     * @param paramMap the Map of parameters
     * @return the array of values
     */
    public static Object[] buildValueArray(String sql, Map<String, ?> paramMap) {
        ParsedSql2 parsedSql = parseSqlStatement(sql);
        return buildValueArray(parsedSql, new MapSqlParameterSource(paramMap), null);
    }


    private static class ParameterHolder {

        private final String parameterName;

        private final int startIndex;

        private final int endIndex;

        public ParameterHolder(String parameterName, int startIndex, int endIndex) {
            this.parameterName = parameterName;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getParameterName() {
            return this.parameterName;
        }

        public int getStartIndex() {
            return this.startIndex;
        }

        public int getEndIndex() {
            return this.endIndex;
        }
    }

}