package com.project.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParserHelper {

    public static class ParsedSqlResult {
        private String parsedSql;
        private List<String> paramNames;

        public ParsedSqlResult(String parsedSql, List<String> paramNames) {
            this.parsedSql = parsedSql;
            this.paramNames = paramNames;
        }

        public String getParsedSql() {
            return parsedSql;
        }

        public List<String> getParamNames() {
            return paramNames;
        }
    }

    /**
     * SQL 문에서 명명된 파라미터(:param)를 위치 기반 파라미터(?)로 변환하고,
     * 파라미터 이름을 순서대로 추출합니다.
     *
     * @param sql 원본 SQL 문
     * @return 변환된 SQL 문과 파라미터 이름 목록을 포함한 ParsedSqlResult 객체
     */
    public static ParsedSqlResult parseSql(String sql) {
        List<String> paramNames = new ArrayList<>();
        StringBuilder parsedSql = new StringBuilder();
        Pattern pattern = Pattern.compile(":([a-zA-Z][a-zA-Z0-9_]*)");
        Matcher matcher = pattern.matcher(sql);
        int last = 0;
        while (matcher.find()) {
            parsedSql.append(sql, last, matcher.start());
            parsedSql.append("?");
            paramNames.add(matcher.group(1));
            last = matcher.end();
        }
        parsedSql.append(sql.substring(last));
        return new ParsedSqlResult(parsedSql.toString(), paramNames);
    }
}

