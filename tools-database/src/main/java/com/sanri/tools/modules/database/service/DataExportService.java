package com.sanri.tools.modules.database.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sanri.tools.modules.database.service.dtos.data.DataQueryParam;
import com.sanri.tools.modules.database.service.dtos.data.DynamicQueryDto;
import com.sanri.tools.modules.database.service.dtos.data.RelationDataQueryResult;
import com.sanri.tools.modules.database.service.dtos.data.export.ExportProcessDto;
import com.sanri.tools.modules.database.service.dtos.meta.TableRelation;
import com.sanri.tools.modules.database.service.dtos.meta.TableRelationTree;
import com.sanri.tools.modules.database.service.meta.TabeRelationMetaData;
import com.sanri.tools.modules.database.service.meta.dtos.Namespace;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.core.service.NamedThreadFactory;
import com.sanri.tools.modules.core.service.file.FileManager;

import com.sanri.tools.modules.database.service.meta.dtos.ActualTableName;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.SelectUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;

@Service
@Slf4j
public class DataExportService {
    @Autowired
    private JdbcDataService jdbcService;

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Autowired
    private FileManager fileManager;

    @Autowired
    private TabeRelationMetaData tableRelationService;


    /**
     * sql ??????????????????,??????????????????????????? sql ??????
     * @param connName
     * @param catalog
     * @param sql
     * @return
     */
    public RelationDataQueryResult relationDataQuery(String connName, Namespace namespace, String sql) throws JSQLParserException {
        RelationDataQueryResult relationDataQueryResult = new RelationDataQueryResult(sql);
        // ???????????? sql , ???????????? sql ???????????????
        Select select = (Select) parserManager.parse(new StringReader(sql));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        if (CollectionUtils.isNotEmpty(tableList)){
            String tableName = tableList.get(0);
            String[] split = tableName.split("\\.");
            String schema = null;
            if (split.length == 2) {
                schema = split[0];
                tableName = split[1];
            }
            PlainSelect selectBody = (PlainSelect) select.getSelectBody();
            Expression where = selectBody.getWhere();

            ActualTableName actualTableName = new ActualTableName(namespace, tableName);
            TableRelationTree hierarchy = tableRelationService.hierarchy(connName, actualTableName);
            TableRelationTree superTypes = tableRelationService.superTypes(connName, actualTableName);

            List<String> sqls = new ArrayList<>();
            List<TableRelationTree> children = hierarchy.getChildren();
            for (TableRelationTree child : children) {
                String generateSql = childRelationsSql(namespace,hierarchy.getTableName(), child, selectBody);
                sqls.add(generateSql);
            }

//            List<String> parentSqls = relationSqls(actualTableName,childs);
//            relationDataQueryResult.setParents(parentSqls);
        }

        return relationDataQueryResult;
    }

    /**
     *
     * @param main
     * @param subTableRelation
     * @param selectBody
     * @return
     */
    private String childRelationsSql(Namespace namespace,String mainTableName,TableRelationTree subTableRelation, SelectBody selectBody) {
        Table mainTable = new Table(namespace.getSchema(), mainTableName);
        mainTable.setAlias(new Alias(DigestUtils.md5Hex(mainTableName)));
        Table subTable = new Table(namespace.getSchema(), subTableRelation.getTableName());
        subTable.setAlias(new Alias(DigestUtils.md5Hex(subTableRelation.getTableName())));

        // ????????? ????????????????????????????????? , ???????????????????????????????????????
        TableRelation.RelationEnum relation = subTableRelation.getRelation();
        switch (relation){
            case MANY_MANY:
                break;
            case ONE_MANY:
            case ONE_ONE:
                // ????????????????????????
                Select select = SelectUtils.buildSelectFromTableAndExpressions(mainTable, new Column("b.*"));

                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(new Column("a.uuid"));
                equalsTo.setRightExpression(new Column("b.event_record_id"));
                SelectUtils.addJoin(select,subTable,equalsTo);
                break;
            default:
        }

        return null;
    }

    /**
     * ??????????????????
     * @param dataQueryParam
     * @return
     * @throws JSQLParserException
     * @throws IOException
     * @throws SQLException
     */
    public DynamicQueryDto exportPreview(DataQueryParam dataQueryParam) throws JSQLParserException, IOException, SQLException {
        String connName = dataQueryParam.getConnName();
        String sql = dataQueryParam.getFirstSql();
        // sql ??????,?????? limit ????????????
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Limit limit = new Limit();
        limit.setOffset(0);
        limit.setRowCount(15);
        plainSelect.setLimit(limit);

        List<DynamicQueryDto> dynamicQueryDtos = jdbcService.executeDynamicQuery(connName, Collections.singletonList(select.toString()),dataQueryParam.getNamespace());
        return dynamicQueryDtos.get(0);
    }

    /**
     * ?????????????????????
     * @param dataQueryParam
     * @return
     */
    public ExportProcessDto exportSingleProcessor(DataQueryParam dataQueryParam) throws IOException, SQLException {
        String connName = dataQueryParam.getConnName();
        String sql = dataQueryParam.getFirstSql();

        File exportDir = fileManager.mkTmpDir("database/data/export/" + dataQueryParam.getTraceId());

        List<DynamicQueryDto> dynamicQueryDtos = jdbcService.executeDynamicQuery(connName,Collections.singletonList(sql),dataQueryParam.getNamespace());
        DynamicQueryDto dynamicQueryDto = dynamicQueryDtos.get(0);
        File excelPartFile = new File(exportDir, dataQueryParam.getTraceId()+ ".xlsx");
        log.info("Excel ?????? :{}",excelPartFile.getName());

        Workbook workbook = new SXSSFWorkbook(1000);
        Sheet sheet = workbook.createSheet(connName);
        FileOutputStream fileOutputStream = new FileOutputStream(excelPartFile);
        fillExcelSheet(dynamicQueryDto,sheet);
        workbook.write(fileOutputStream);

        Path path = fileManager.relativePath(exportDir.toPath());

        return new ExportProcessDto(path.toString(),1,1);
    }

    /**
     * ??????????????????????????????
     */
    static final int exportPerLimit = 10000;

    /**
     * ????????????????????? ??????
     */
//    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,10,0, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(100),new NamedThreadFactory("exportExcel"));
    @Autowired
    @Qualifier("publicSlowThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;
    /**
     * ?????????????????????
     * @param dataQueryParam
     * @throws IOException
     * @throws SQLException
     * @return
     */
    public ExportProcessDto exportLowMemoryMutiProcessor(DataQueryParam dataQueryParam) throws IOException, SQLException, JSQLParserException {
        String connName = dataQueryParam.getConnName();
        String sql = dataQueryParam.getFirstSql();

        // ??????????????????
        String countSql = "select count(*) from (" + sql + ") b";
        Long dataCount = jdbcService.executeQuery(connName, countSql, new ScalarHandler<Long>(1),dataQueryParam.getNamespace());

        if(dataCount < exportPerLimit){
            return exportSingleProcessor(dataQueryParam);
        }

        //???????????????
        final int threadCount = (int) ((dataCount - 1) / exportPerLimit + 1);

        log.info("???????????????????????????:{}",dataQueryParam.getTraceId());

        //??????????????????
        File exportDir = fileManager.mkTmpDir("database/data/export/"+dataQueryParam.getTraceId());
        log.info("?????????????????????????????????:{}",exportDir);

        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        //???????????????; ????????? 10 ??? ???????????? Excel,?????? Excel ?????????;  ????????????????????????,????????????,??????????????????????????? zip ??????
        for (int i = 0; i < threadCount; i++) {
            int currentBatch = i;
            final long begin = currentBatch * exportPerLimit;
            long end = (currentBatch + 1) * exportPerLimit;
            if(end > dataCount){
                end = dataCount;
            }
            final long  finalEnd = end;
            Limit limit = new Limit();
            limit.setOffset(begin);
            limit.setRowCount(end);
            plainSelect.setLimit(limit);
            final String currentSql = select.toString();

            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fileOutputStream = null;
                    try {
                        List<DynamicQueryDto> dynamicQueryDtos = jdbcService.executeDynamicQuery(connName,Collections.singletonList(currentSql),dataQueryParam.getNamespace());
                        DynamicQueryDto dynamicQueryDto = dynamicQueryDtos.get(0);
                        File excelPartFile = new File(exportDir, dataQueryParam.getTraceId()+"_" + begin + "~" + finalEnd + ".xlsx");
                        log.info("Excel ???????????? :{}",excelPartFile.getName());

                        Workbook workbook = new SXSSFWorkbook(1000);
                        Sheet sheet = workbook.createSheet(connName +  "_" + begin + "~" + finalEnd);
                        fileOutputStream = new FileOutputStream(excelPartFile);
                        fillExcelSheet(dynamicQueryDto,sheet);
                        workbook.write(fileOutputStream);
                    } catch (Exception e) {
                        log.error("exportLowMemoryMutiProcessor() error : {}",e.getMessage(),e);
                    } finally {
                        IOUtils.closeQuietly(fileOutputStream);
                    }
                }
            });

        }

        Path path = fileManager.relativePath(exportDir.toPath());
        ExportProcessDto exportProcessDto = new ExportProcessDto(path.toString(), 0, dataCount);
        return exportProcessDto;
    }

    /**
     * ?????? excel sheet ???
     * @param session
     * @param sqlExecuteResult
     * @param sheet
     */
    public static final float BASE_HEIGHT_1_PX = 15.625f;
    private void fillExcelSheet( DynamicQueryDto sqlExecuteResult, Sheet sheet) {
        Row headRow = sheet.createRow(0);
        headRow.setHeight((short)(30 * BASE_HEIGHT_1_PX));
        //???????????????
        List<DynamicQueryDto.Header> headers = sqlExecuteResult.getHeaders();

        for (int i = 0; i < headers.size(); i++) {
            DynamicQueryDto.Header header = headers.get(i);
            Cell headCell = headRow.createCell(i);
            headCell.setCellValue(header.getColumnName());
            headCell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //???????????????
        List<Map<String,Object>> rows = sqlExecuteResult.getRows();

        for (int i = 0; i < rows.size(); i++) {
            //????????????
            Map<String,Object> objects = rows.get(i);
            Row dataRow = sheet.createRow(i + 1);
            for (int j = 0; j < objects.size(); j++) {
                DynamicQueryDto.Header colTypeHeader = headers.get(j);
                String colType = colTypeHeader.getTypeName();
                String columnName = colTypeHeader.getColumnName();

                Cell cell = dataRow.createCell(j);
                Object value = objects.get(columnName);

                if(value == null){
                    // ??????
                    cell.setCellType(Cell.CELL_TYPE_BLANK);
                    continue;
                }
                if("char".equalsIgnoreCase(colType) || "varchar".equalsIgnoreCase(colType)) {
                    cell.setCellValue(String.valueOf(value));
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                }else if ("datetime".equalsIgnoreCase(colType)){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    Timestamp timestamp = (Timestamp) value;
                    long time = timestamp.getTime();
                    String format = DateFormatUtils.ISO_DATE_FORMAT.format(time);
                    cell.setCellValue(format);
                }else if("int".equalsIgnoreCase(colType) || "decimal".equalsIgnoreCase(colType) || "bigint".equalsIgnoreCase(colType)){
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(NumberUtils.toLong(String.valueOf(value)));
                }else if ("date".equalsIgnoreCase(colType) || "TIMESTAMP".equalsIgnoreCase(colType)){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(String.valueOf(value));
                }else if("TINYINT".equalsIgnoreCase(colType)){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(String.valueOf(value));
                }else {
                    log.error("???????????????????????????,????????????????????????:{},value:{}",colType,value);
                }
            }
        }

        //????????????; ????????????
//        for (int i = 0; i < headers.size(); i++) {
//            sheet.autoSizeColumn(i);
//        }
    }

}
