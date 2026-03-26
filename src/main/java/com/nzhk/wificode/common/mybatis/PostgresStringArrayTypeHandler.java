package com.nzhk.wificode.common.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL varchar[] ↔ Java String[]
 */
@MappedTypes(String[].class)
@MappedJdbcTypes(JdbcType.ARRAY)
public class PostgresStringArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        Connection conn = ps.getConnection();
        Array arr = conn.createArrayOf("varchar", parameter);
        ps.setArray(i, arr);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toStringArray(rs.getArray(columnName));
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toStringArray(rs.getArray(columnIndex));
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toStringArray(cs.getArray(columnIndex));
    }

    private static String[] toStringArray(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return null;
        }
        try {
            Object o = sqlArray.getArray();
            if (o instanceof String[]) {
                return (String[]) o;
            }
            if (o instanceof Object[]) {
                Object[] arr = (Object[]) o;
                String[] out = new String[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    out[i] = arr[i] != null ? arr[i].toString() : null;
                }
                return out;
            }
            return null;
        } finally {
            sqlArray.free();
        }
    }
}
