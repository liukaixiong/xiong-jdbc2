//package com.x.jdbc.sql;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author liuhx on 2016/12/14 10:27
// * @version V1.0
// * @email liuhx@elab-plus.com
// */
//public class OutSqlHelp {
//
//
//    private static String parseSQLForIn(String sql, LinkedHashMap<String, Object> params) {
//
//
//        StringBuilder sqlBuffer = new StringBuilder();
//        for (String key : params.keySet()) {
//            Object value = params.get(key);
//            if (value == null) {
//                continue;
//            }
//            if (value instanceof List) {
//                StringBuilder stringBuilder = new StringBuilder();
//
//                List invalues = (List) value;
//                for (int i = 0; i < invalues.size(); i++) {
//                    stringBuilder.append("?");
//                    stringBuilder.append(",");
//                }
//
//                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//                sql = sql.replaceAll("\\s*:\\s*" + key.trim() + "\\b", stringBuilder.toString());
//            } else if (value.getClass().isArray()) {
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append("?");
//                Object[] arr = (Object[]) value;
//
//                for (int i = 0; i < arr.length; i++) {
//                    if (arr[i] == null) {
//                        continue;
//                    } else {
//                        sql = sql.replaceAll("\\s*:\\s*" + key.trim() + i + "\\b", stringBuilder.toString());
//                    }
//                }
//            } else {
//                StringBuilder stringBuilder = new StringBuilder();
//
//                stringBuilder.append("?");
//
//                sql = sql.replaceAll("\\s*:\\s*" + key.trim() + "\\b", stringBuilder.toString());
//            }
//
//        }
//
//        return sql;
//    }
//
//
//    public static String processNull(String sqlString, LinkedHashMap<String, Object> params) {
//
//        String sql = sqlString;
//        int num = 0;
//        for (Map.Entry entry : params.entrySet()) {
//            Object value = entry.getValue();
//            String key = entry.getKey().toString();
////            if (key.contains(".")) {
////                key = key.split("\\.")[1];
////            }
//            if (value == null) {
//                if (key.matches("[0-9]{1}[a-z]+")) {
//                    sql = sql.replaceFirst("(\\w*\\.?" + key.substring(1) + "\\b\\s*\\S+\\s*:" + key + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
//                } else {
//                    sql = sql.replaceFirst("(\\w*\\.?" + key + "\\b\\s*\\S+\\s*:" + key + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
//                }
//                sql = sql.replaceAll("(and\\s*order\\s*by\\s*|or\\s*order\\s*by\\s*)", " order by ");
//                sql = sql.replaceAll("(and\\s*group\\s*by\\s*|or\\s*group\\s*by\\s*)", " group by ");
//                sql = sql.replaceAll("(and\\s*$|or\\s*$)", "  ");
//                num++;
//            } else if (value != null && value.getClass().isArray()) {
//                boolean flag = false;
//                Object[] arr = (Object[]) value;
//                for (int i = 0; i < arr.length; i++) {
//
//                    if (arr[i] == null) {
//
//                        if (key.matches("[0-9]{1}[a-z]+")) {
//                            sql = sql.replaceFirst("(\\w*\\.?" + key.substring(1) + "\\b\\s*\\S+\\s*:" + key + i + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
//                        } else {
//                            sql = sql.replaceFirst("(\\w*\\.?" + key + "\\b\\s*\\S+\\s*:" + key + i + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
//                        }
////                        sql = sql.replaceAll("(\\w*\\.?"+entry.getKey().toString()+"\\b\\s*\\S+\\s*:" + entry.getKey().toString()+i + "\\b\\s*)(\\s*and\\s*|\\s*or\\s*|\\s*)", "  ");
//                        sql = sql.replaceAll("(and\\s*order\\s*by\\s*|or\\s*$)", " order by ");
//                        sql = sql.replaceAll("(and\\s*group\\s*by\\s*|or\\s*$)", " group by ");
//                        sql = sql.replaceAll("(and\\s*$|or\\s*$)", "  ");
//                        if (!flag) {
//                            num++;
//                            flag = true;
//                        }
//
//                    } else if (arr[i] != null) {
//                        num--;
//                    }
//
//
//                }
//
//
//            }
//
//            if (num == params.size()) {
//                sql = sql.replaceFirst(" where ", " ");
//            }
//
//        }
//        return sql;
//    }
//
//    private static String processNullForUpdate(String sqlString, LinkedHashMap<String, Object> params) {
//
//        String sql = sqlString;
//
//        for (Map.Entry entry : params.entrySet()) {
//            Object value = entry.getValue();
//
//            if (value == null) {
//                sql = sql.replaceAll("(,?\\s*\\w*\\.?" + entry.getKey().toString() + "\\s*\\S+\\s*:" + entry.getKey().toString() + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)", "  ");
//                sql = sql.replaceAll("(and\\s*$|or\\s*$)", "  ");
//                sql = sql.replaceAll(",\\s*where\\s*", " where ");
//
//            } else if (value != null && value.getClass().isArray()) {
//                Object[] arr = (Object[]) value;
//                for (int i = 0; i < arr.length; i++) {
//                    if (arr[i] == null) {
//                        sql = sql.replaceAll("(,?\\s*\\w*\\.?" + entry.getKey().toString() + "\\s*\\S+\\s*:" + entry.getKey().toString() + i + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)", "  ");
//                        sql = sql.replaceAll("(and\\s*$|or\\s*$)", "  ");
//                        sql = sql.replaceAll(",\\s*where\\s*", " where ");
//
//                    }
//                }
//            }
//
//        }
//        return sql;
//
//    }
//
//
//    private static String processNullForInsert(String sqlString, LinkedHashMap<String, Object> params) {
//
//
//        String sql = sqlString;
//
//        String columnString = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")"));
//        String[] sqlColumns = columnString.split(",");
//
//        for (String column : sqlColumns) {
//            if (!params.containsKey(column.trim())) {
//                sql = sql.replaceFirst(":" + column.trim() + "\\b", " null ");
//
//            }
//        }
//
//
//        return sql;
//    }
//
//
//}
