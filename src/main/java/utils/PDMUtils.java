package main.java.utils;

import main.java.bean.DataEntity;
import main.java.bean.DataField;
import main.java.common.DJJHelper;
import main.java.common.TypeMapping;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

/**
 * pdm 生成工具
 */
public class PDMUtils {

    /**
     * 根据pdm 文件获取所有数据表 并转换为 实体数据bean
     * @param f
     * @return
     */
    public static List<DataEntity> getDataEntityList(File f) {
        Document document = getDocument(f);
        Iterator itr = document.selectNodes("//c:Tables//o:Table").iterator();//表
        //数据列表
        List<DataEntity> list = new ArrayList<>();
        while (itr.hasNext()) {
            Element table = (Element) itr.next();//表
            DataEntity entity = new DataEntity();//数据
            entity.setDescribe(table.elementTextTrim("Name"));
            entity.setTableName(table.elementTextTrim("Code"));
            list.add(entity);
        }
        Collections.sort(list, new Comparator<DataEntity>() {
            @Override
            public int compare(DataEntity o1, DataEntity o2) {
                return o1.getTableName().compareTo(o2.getTableName());
            }
        });
        return list;
    }

    /**
     * 指定表名 从pdm 文件中获取相应数据表 并生成实体数据bean
     * @param f
     * @param tableName
     * @return
     */
    public static DataEntity getSingleDataEntity(File f, String tableName) {
        if (f == null || DJJHelper.isBlank(tableName)) {
            return null;
        }
        DataEntity entity = new DataEntity();
        entity.setTableName(tableName);
        handleSingleDataEntity(f, entity);
        return entity;
    }

    /**
     * 指定实体数据bean 从pdm 文件中获取相应数据表 并补全实体数据bean中其它内容
     * @param f
     * @param entity
     */
    public static void handleSingleDataEntity(File f, DataEntity entity) {
        if (f == null || entity == null || DJJHelper.isBlank(entity.getTableName())) {
            return;
        }
        Iterator itr = getDocument(f).selectNodes("//c:Tables//o:Table").iterator();//表
        while (itr.hasNext()) {
            Element table = (Element) itr.next();//表
            String tableName = table.elementTextTrim("Code");
            if (!tableName.equals(entity.getTableName())) {
                continue;
            }
            entity.setDescribe(table.elementTextTrim("Name"));
            entity.setTableName(table.elementTextTrim("Code"));
            //table.elementTextTrim("Comment")
            //TABLE_NAME -> TableName

            //主键
            Set<String> keyIds = new HashSet<>();
            for (Element element : table.element("Keys").element("Key").element("Key.Columns").elements()) {
                keyIds.add(element.attributeValue("Ref"));
            }

            entity.setClassName(DJJHelper.clearLine(entity.getTableName(), true));
            //表字段处理
            Iterator itr1 = table.element("Columns").elements("Column").iterator();//列
            while (itr1.hasNext()) {
                DataField dataField = new DataField();//字段
                Element column = (Element) itr1.next();
                String id = column.attributeValue("Id");
                dataField.setColumn(column.elementTextTrim("Code"));//列名
                //描述与备注
                String describe = column.elementTextTrim("Name");
                String columnComment = column.elementTextTrim("Comment");
                if (describe == null) {
                    describe = columnComment;
                } else {
                    describe += columnComment == null ? "" : columnComment;
                }
                dataField.setDescribe(describe);

                String length = column.elementTextTrim("Length");
                dataField.setLength(length == null ? null : Integer.parseInt(length));//长度
                String precision = column.elementTextTrim("Precision");
                if (precision != null) {
                    dataField.setPrecision(Integer.parseInt(length));//精度
                    dataField.setScale(Integer.parseInt(precision));//精度
                }

                dataField.setMandatory("1".equals(column.elementTextTrim("Mandatory")));//是否必输
                dataField.setFieldName(DJJHelper.clearLine(dataField.getColumn(), false));//字段名称
                String dataType;
                try {
                    dataType = TypeMapping.clearDataTypeLength(column.elementTextTrim("DataType"));
                } catch (Exception e) {
                    throw new RuntimeException(entity.getTableName() + "中" + describe + "字段类型有误");
                }
                dataField.setDataType(dataType);//数据类型
                //主键判断

                if (keyIds != null && keyIds.contains(id)) {
                    dataField.setPrimary(true);//是否是主键
                    dataField.setMandatory(true);
                    entity.addPrimaryCount(dataField.getJavaType());
                }
                entity.addField(dataField);
            }
            entity.initPrimaryStrategy();
        }
    }

    private static Document getDocument(File f) {
        SAXReader sr = new SAXReader();
        Document doc = null;
        try {
            return doc = sr.read(f);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Document Exception");
        }
    }
}
