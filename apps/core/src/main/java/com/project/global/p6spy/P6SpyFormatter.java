package com.project.global.p6spy;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.logging.P6LogOptions;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import com.project.utils.SqlExecutionTracker;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Locale;
import java.util.Stack;

public class P6SpyFormatter implements MessageFormattingStrategy, ApplicationContextAware {
    private boolean isHighlight = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());

        String excludeCategories = applicationContext.getEnvironment().getProperty("p6spy.excludecategories", "info,debug,result,resultset");
        String executionThreshold = applicationContext.getEnvironment().getProperty("p6spy.executionThreshold", "0");
        String autoflush = applicationContext.getEnvironment().getProperty("p6spy.autoflush", "false");
        String databaseDialectTimestampFormat = applicationContext.getEnvironment().getProperty("databaseDialectTimestampFormat", "yyyy-MM-dd HH:mm:ss.SSS");
        String excludePattern = applicationContext.getEnvironment().getProperty("p6spy.excludepatterns", "");
        isHighlight = BooleanUtils.toBoolean(applicationContext.getEnvironment().getProperty("p6spy.highlight", "false"));

        P6LogOptions.getActiveInstance().setExcludecategories(excludeCategories);
        P6LogOptions.getActiveInstance().setExecutionThreshold(executionThreshold);
        P6SpyOptions.getActiveInstance().setAutoflush(autoflush);
        P6SpyOptions.getActiveInstance().setDatabaseDialectTimestampFormat(databaseDialectTimestampFormat);

        // exclude pattern 추가
        P6LogOptions.getActiveInstance().setFilter(true);
        if (StringUtils.isNotBlank(excludePattern)) P6LogOptions.getActiveInstance().setExclude(excludePattern);
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        sql = formatSql(category, sql);
        // sql 이 없다면 출력하지 않아도 됨
        if (sql.trim().isEmpty()) {
            return "";
        }else {
            // 연속된 공백 라인 제거
            sql = sql.replaceAll("(?m)^[ \t]*\r?\n", "");
        }
        // statement sql은 marking (빈 SQL문 후 커밋,롤백은 필터링용도)
        if (!Category.COMMIT.getName().equals(category) && !Category.ROLLBACK.getName().equals(category)) {
            SqlExecutionTracker.markExecuted(connectionId);
        }

        if (Category.COMMIT.getName().equals(category) || Category.ROLLBACK.getName().equals(category)) {
            // 빈 SQL문 후 커밋 or 롤백시는 로그를 출력하지 않는다.
            if (SqlExecutionTracker.hasExecuted(connectionId)) {
                SqlExecutionTracker.clearForConnection(connectionId);
                sql = "Connection ID : " + connectionId + sql;
            } else {
                return "_dummy_commit_rollback"; // 해당키워드로 log-back.xml 필터링처리
            }
        }

        //return String.format("[%s] | %d ms | %s", category, elapsed, formatSql(category, sql));
        return String.format("[%s] | %d ms | %s", category, elapsed, sql);
    }

    private String formatSql(String category, String sql) {
        if (!isHighlight) {
            return formatBasicSql(category, sql);
        }

        if (sql != null && !sql.trim().isEmpty() &&
                (Category.STATEMENT.getName().equals(category) || Category.BATCH.getName().equals(category))) {
            String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSQL.startsWith("create") || trimmedSQL.startsWith("alter") || trimmedSQL.startsWith("comment")) {
                sql = FormatStyle.DDL.getFormatter().format(sql);
            } else {
                sql = FormatStyle.HIGHLIGHT.getFormatter().format(sql);
            }
            return "\t"+sql;
        }

        return "\t"+FormatStyle.HIGHLIGHT.getFormatter().format(category);
    }


    private String formatBasicSql(String category, String sql) {
        if (sql != null && !sql.trim().isEmpty() &&
                (Category.STATEMENT.getName().equals(category) || Category.BATCH.getName().equals(category))) {
            String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSQL.startsWith("create") || trimmedSQL.startsWith("alter") || trimmedSQL.startsWith("comment")) {
                // 색상 코드 없는 기본 포맷터 사용
                sql = FormatStyle.DDL.getFormatter().format(sql);
            } else {
                // HIGHLIGHT 대신 BASIC 포맷터 사용하여 색상 코드 제거
                sql = FormatStyle.BASIC.getFormatter().format(sql);
            }
            return "\t"+sql;
        }

        // 카테고리도 색상 코드 없이 출력
        return "\t"+FormatStyle.BASIC.getFormatter().format(category);
    }

    // stack 콘솔 표기
    private String createStack(int connectionId, long elapsed) {
        Stack<String> callStack = new Stack<>();
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String lastStack = "";

        // 현재 요청의 HTTP 정보를 가져옴(Async 사용시 문제로 주석처리 함)
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
//        String endpoint = request != null ? request.getMethod() + " " + request.getRequestURI() : "Unknown Endpoint";

        for (StackTraceElement stackTraceElement : stackTrace) {
            String trace = stackTraceElement.toString();

            // 적절한 Controller 클래스 필터링
            if (trace.startsWith("bizwell.xclick")
                    && !trace.startsWith("bizwell.xclick.global")
                    && !trace.startsWith("bizwell.xclick.aop")
                    && !trace.contains("SpringCGLIB")) {
                lastStack = trace; // 가장 마지막 관련 스택을 저장
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(lastStack);

        return new StringBuilder()
                .append("\n\n\tConnection ID : ").append(connectionId)
                .append(" | Execution Time : ").append(elapsed).append(" ms\n")
//                .append("\tEndpoint : ").append(endpoint).append("\n") // 엔드포인트 정보 추가
                .append("\tCall Stack : ").append(sb).append("\n")
                .append("-------------------------------------------------")
                .toString();
    }


    public boolean isOnAfterExecuteBatchCalled() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement ste : stackTrace) {
            if (ste.getMethodName().equals("onAfterExecuteBatch")) { //onAfterAnyAddBatch
                return true;
            }
        }
        return false;
    }
}
