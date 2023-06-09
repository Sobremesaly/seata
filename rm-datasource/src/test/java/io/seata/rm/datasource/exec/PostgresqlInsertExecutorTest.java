/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.rm.datasource.exec;

import io.seata.rm.datasource.ConnectionProxy;
import io.seata.rm.datasource.PreparedStatementProxy;
import io.seata.rm.datasource.StatementProxy;
import io.seata.rm.datasource.exec.postgresql.PostgresqlInsertExecutor;
import io.seata.rm.datasource.sql.struct.ColumnMeta;
import io.seata.rm.datasource.sql.struct.TableMeta;
import io.seata.sqlparser.SQLInsertRecognizer;
import io.seata.sqlparser.struct.SqlDefaultExpr;
import io.seata.sqlparser.util.JdbcConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author jsbxyyx
 */
public class PostgresqlInsertExecutorTest {

    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String USER_NAME_COLUMN = "user_name";
    private static final String USER_STATUS_COLUMN = "user_status";
    private static final Integer PK_VALUE_ID = 100;
    private static final Integer PK_VALUE_USER_ID = 200;

    private StatementProxy statementProxy;

    private SQLInsertRecognizer sqlInsertRecognizer;

    private TableMeta tableMeta;

    private PostgresqlInsertExecutor insertExecutor;

    private final int pkIndexId = 0;
    private final int pkIndexUserId = 1;
    private HashMap<String, Integer> pkIndexMap;
    private HashMap<String, Integer> multiPkIndexMap;

    @BeforeEach
    public void init() {
        ConnectionProxy connectionProxy = mock(ConnectionProxy.class);
        when(connectionProxy.getDbType()).thenReturn(JdbcConstants.POSTGRESQL);

        statementProxy = mock(PreparedStatementProxy.class);
        when(statementProxy.getConnectionProxy()).thenReturn(connectionProxy);

        StatementCallback statementCallback = mock(StatementCallback.class);
        sqlInsertRecognizer = mock(SQLInsertRecognizer.class);
        tableMeta = mock(TableMeta.class);
        insertExecutor = Mockito.spy(new PostgresqlInsertExecutor(statementProxy, statementCallback, sqlInsertRecognizer));

        pkIndexMap = new HashMap<String, Integer>() {
            {
                put(ID_COLUMN, pkIndexId);
            }
        };

        multiPkIndexMap = new HashMap<String, Integer>() {
            {
                put(ID_COLUMN, pkIndexId);
                put(USER_ID_COLUMN, pkIndexUserId);
            }
        };
    }

    @Test
    public void testInsertDefault_ByDefault() throws Exception {
        mockInsertColumns();
        mockInsertRows();
        mockParametersPkWithDefault();

        Map<String, ColumnMeta> pkMap = new HashMap<>();
        ColumnMeta columnMeta = mock(ColumnMeta.class);
        doReturn("nextval('test_id_seq'::regclass)").when(columnMeta).getColumnDef();
        pkMap.put(ID_COLUMN, columnMeta);
        doReturn(pkMap).when(tableMeta).getPrimaryKeyMap();
        doReturn(tableMeta).when(insertExecutor).getTableMeta();

        List<Object> pkValuesAuto = new ArrayList<>();
        pkValuesAuto.add(PK_VALUE_ID);
        //mock getPkValuesByAuto
        doReturn(pkValuesAuto).when(insertExecutor).getGeneratedKeys(ID_COLUMN);
        Map<String, List<Object>> pkValuesMap = insertExecutor.getPkValuesByColumn();
        //pk value = DEFAULT so getPkValuesByDefault
        doReturn(new ArrayList<>()).when(insertExecutor).getPkValuesByDefault(ID_COLUMN);

        verify(insertExecutor).getPkValuesByDefault(ID_COLUMN);
        Assertions.assertEquals(pkValuesMap.get(ID_COLUMN), pkValuesAuto);
    }

    @Test
    public void testInsertDefault_ByDefault_MultiPk() throws Exception {
        mockInsertColumns_MultiPk();
        mockInsertRows_MultiPk();
        mockParametersPkWithDefault_MultiPk();

        Map<String, ColumnMeta> pkMap = new HashMap<>();
        ColumnMeta columnMeta = mock(ColumnMeta.class);
        doReturn("nextval('test_id_seq'::regclass)").when(columnMeta).getColumnDef();
        pkMap.put(ID_COLUMN, columnMeta);
        pkMap.put(USER_ID_COLUMN, columnMeta);
        doReturn(pkMap).when(tableMeta).getPrimaryKeyMap();
        doReturn(tableMeta).when(insertExecutor).getTableMeta();

        List<Object> pkValuesAutoId = new ArrayList<>();
        pkValuesAutoId.add(PK_VALUE_ID);
        List<Object> pkValuesAutoUserId = new ArrayList<>();
        pkValuesAutoUserId.add(PK_VALUE_USER_ID);
        //mock getPkValuesByAuto
        doReturn(pkValuesAutoId).when(insertExecutor).getGeneratedKeys(ID_COLUMN);
        doReturn(pkValuesAutoUserId).when(insertExecutor).getGeneratedKeys(USER_ID_COLUMN);
        Map<String, List<Object>> pkValuesMap = insertExecutor.getPkValuesByColumn();
        //pk value = DEFAULT so getPkValuesByDefault
        doReturn(new ArrayList<>()).when(insertExecutor).getPkValuesByDefault(ID_COLUMN);
        doReturn(new ArrayList<>()).when(insertExecutor).getPkValuesByDefault(USER_ID_COLUMN);

        verify(insertExecutor).getPkValuesByDefault(ID_COLUMN);
        verify(insertExecutor).getPkValuesByDefault(USER_ID_COLUMN);
        Assertions.assertEquals(pkValuesMap.get(ID_COLUMN), pkValuesAutoId);
        Assertions.assertEquals(pkValuesMap.get(USER_ID_COLUMN), pkValuesAutoUserId);
    }

    private void mockParametersPkWithDefault() {
        Map<Integer, ArrayList<Object>> parameters = new HashMap<>(4);
        ArrayList arrayList0 = new ArrayList<>();
        arrayList0.add(SqlDefaultExpr.get());
        ArrayList arrayList1 = new ArrayList<>();
        arrayList1.add("userId1");
        ArrayList arrayList2 = new ArrayList<>();
        arrayList2.add("userName1");
        ArrayList arrayList3 = new ArrayList<>();
        arrayList3.add("userStatus1");
        parameters.put(1, arrayList0);
        parameters.put(2, arrayList1);
        parameters.put(3, arrayList2);
        parameters.put(4, arrayList3);
        PreparedStatementProxy psp = (PreparedStatementProxy) this.statementProxy;
        when(psp.getParameters()).thenReturn(parameters);
    }

    private void mockParametersPkWithDefault_MultiPk() {
        Map<Integer, ArrayList<Object>> parameters = new HashMap<>(4);
        ArrayList arrayList0 = new ArrayList<>();
        arrayList0.add(SqlDefaultExpr.get());
        ArrayList arrayList1 = new ArrayList<>();
        arrayList1.add(SqlDefaultExpr.get());
        ArrayList arrayList2 = new ArrayList<>();
        arrayList2.add("userName1");
        ArrayList arrayList3 = new ArrayList<>();
        arrayList3.add("userStatus1");
        parameters.put(1, arrayList0);
        parameters.put(2, arrayList1);
        parameters.put(3, arrayList2);
        parameters.put(4, arrayList3);
        PreparedStatementProxy psp = (PreparedStatementProxy) this.statementProxy;
        when(psp.getParameters()).thenReturn(parameters);
    }

    private void mockInsertRows() {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("?", "?", "?"));
        when(sqlInsertRecognizer.getInsertRows(pkIndexMap.values())).thenReturn(rows);
    }

    private void mockInsertRows_MultiPk() {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("?", "?"));
        when(sqlInsertRecognizer.getInsertRows(multiPkIndexMap.values())).thenReturn(rows);
    }

    private List<String> mockInsertColumns() {
        List<String> columns = new ArrayList<>();
        columns.add(ID_COLUMN);
        columns.add(USER_ID_COLUMN);
        columns.add(USER_NAME_COLUMN);
        columns.add(USER_STATUS_COLUMN);
        when(sqlInsertRecognizer.getInsertColumns()).thenReturn(columns);
        doReturn(pkIndexMap).when(insertExecutor).getPkIndex();
        return columns;
    }

    private List<String> mockInsertColumns_MultiPk() {
        List<String> columns = new ArrayList<>();
        columns.add(ID_COLUMN);
        columns.add(USER_ID_COLUMN);
        columns.add(USER_NAME_COLUMN);
        columns.add(USER_STATUS_COLUMN);
        when(sqlInsertRecognizer.getInsertColumns()).thenReturn(columns);
        doReturn(multiPkIndexMap).when(insertExecutor).getPkIndex();
        return columns;
    }

}
