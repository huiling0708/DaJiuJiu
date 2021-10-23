package main.java.transform;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.sql.SqlFileType;
import com.intellij.ui.EditorTextField;
import main.java.bean.*;
import main.java.common.*;
import main.java.component.DataToEntityTable;
import main.java.component.PDMFileChoose;
import main.java.form.BuildClassForm;
import main.java.form.ChooseTableForm;
import main.java.form.UIFieldTable;
import main.java.utils.PDMUtils;
import main.java.utils.SQLConvertUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * 数据源转换为实体处理类
 */
public abstract class DataTransformEntityHandler {

    /**
     * sql 语句转换为实体类
     *
     * @param event
     */
    public static void sqlToEntity(AnActionEvent event) {
        //获取实体包
        PsiDirectory directory = getEntityDirectory(event);
        if (directory == null) {
            return;
        }

        EditorTextField myInput = new EditorTextField("", event.getProject(), SqlFileType.INSTANCE);
        myInput.setOneLineMode(false);

        //输入了值
        JBPopupListener popupListener = new JBPopupListener() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent le) {
                String text = myInput.getText();
                try {
                    //根据输入的SQL 语句 生成数据实体 再调用实体处理方法
                    DataEntity dataEntity = SQLConvertUtils.buildDataBySQL(text);
                    entityHandle(event.getProject(), directory, dataEntity);
                } catch (Exception exception) {
                    DJJMessage.errorDialog(exception);
                }
            }
        };

        //通过JBPopupFactory 创建临时窗体
        JBPopupFactory instance = JBPopupFactory.getInstance();
        instance.createComponentPopupBuilder(new JScrollPane(myInput), myInput)
                .setTitle("请输入SQL建表语句或查询语句")
                .addListener(popupListener)
                .setMovable(true)
                .setResizable(true)
                .setMayBeParent(false)
                .setNormalWindowLevel(false)
                .setRequestFocus(true)
                .setMinSize(new Dimension(600, 300))
                .createPopup()
                .showCenteredInCurrentWindow(event.getProject());
    }

    /**
     * pdm 转换为实体类
     *
     * @param event
     */
    public static void pdmFileToEntity(AnActionEvent event) {
        PsiDirectory directory = getEntityDirectory(event);
        if (directory == null) {
            return;
        }
        new ChooseTableForm(dataEntity -> {
            //处理实体
            entityHandle(event.getProject(), directory, dataEntity);
        });
    }

    /**
     * 获取实体包
     *
     * @param event
     * @return
     */
    private static PsiDirectory getEntityDirectory(AnActionEvent event) {
        VirtualFile data = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (data == null) {
            return null;
        }
        PsiDirectory directory = PsiDirectoryFactory.getInstance(event.getProject())
                .createDirectory(data);

        if (!"entity".equals(directory.getName())) {
            throw new RuntimeException("实体只能在entity包下创建");
        }
        return directory;
    }

    /**
     * 实体类处理
     *
     * @param project
     * @param entityDirectory
     * @param dataEntity
     */
    private static void entityHandle(Project project, PsiDirectory entityDirectory, DataEntity dataEntity) {
        if (dataEntity == null) {
            throw new RuntimeException("待转换数据异常：数据加载为空");
        }

        //表单通用处理
        IClassForm iClassForm = new IClassForm() {
            @Override
            public String getTitle() {
                return "Create Entity";
            }

            @Override
            public String getClassName() {
                return dataEntity.getClassName();
            }

            @Override
            public String getClassDescription() {
                return dataEntity.getDescribe();
            }
        };

        //成功处理
        IButtonEvent okButtonEvent = new IButtonEvent() {
            @Override
            public boolean handle(MouseEvent e, JFrame jFrame, JTextField classNameText, JTextField descriptionText) {
                dataEntity.setClassName(classNameText.getText());
                dataEntity.setDescribe(descriptionText.getText());

                boolean b = PsiHelper.checkClassName(entityDirectory, dataEntity.getClassName());
                if (!b) {
                    return false;
                }

                //主键个数>1 多主键处理
                if (dataEntity.getPrimaryCount() > 1) {
                    //创建一个pk 类，当前包下创建 pk 包，并创建pk类
                    String pkClassName = dataEntity.getClassName() + "Pk";
                    PsiDirectory pkDirectory = entityDirectory.findSubdirectory("pk");
                    String pkDirectoryName = null;
                    if (pkDirectory == null) {
                        pkDirectoryName = "pk";//子包不存在时，表示需要创建
                    } else {
                        //获取并检查文件名
                        pkClassName = PsiHelper.getAndCheckClassName(pkDirectory, pkClassName);
                        if (DJJHelper.isBlank(pkClassName)) {
                            return false;
                        }
                    }
                    //如果存在多个主键 则把主键字段生成一个Pk类
                    DataPkEntity dataPkEntity = new DataPkEntity(pkClassName, dataEntity.getDataFields());
                    //创建Pk类文件
                    PsiHelper.createJavaFile(project, "entityPk.ftl",
                            dataPkEntity,
                            pkDirectory == null ? entityDirectory : pkDirectory
                            , pkDirectoryName);
                    if (pkDirectory == null) {
                        pkDirectory = entityDirectory.findSubdirectory("pk");
                    }
                    //Pk类增加到实体类的导入中
                    PsiJavaFile file = (PsiJavaFile) pkDirectory.findFile(dataPkEntity.fileName());
                    dataEntity.getImportContent().add(file.getClasses()[0].getQualifiedName());
                }

                //字段导入处理
                dataEntity.getDataFields().forEach(f -> f.importHandle(dataEntity));

                //生成 SerialVersionUID
                dataEntity.setSerialVersionUIDValue(DJJHelper.buildSerialVersionUID());
                //创建实体类
                PsiHelper.createJavaFile(project, "entity.ftl", dataEntity, entityDirectory);
                return true;
            }
        };

        //字段详情按钮处理
        IButtonEvent fieldsButtonEvent = new IButtonEvent() {
            @Override
            public void handle(JFrame mainFrame, MouseEvent e) {
                new UIFieldTable(new TableDataHandle<>(
                        DataField.class, dataEntity.getDataFields()),
                        new Dimension(750, 300), "字段详情",
                        h -> new DataToEntityTable(h)
                                .setProject(project)
                                .setParentJFrame(mainFrame));
            }
        };

        //展示生成类窗体
        new BuildClassForm(iClassForm, okButtonEvent, fieldsButtonEvent);
    }

    /**
     * 创建字典
     *
     * @param project
     * @param dictBean
     */
    public static void createDict(Project project, DictBean dictBean) {
        //字典名称首字母大写
        dictBean.setDictName(DJJHelper.firstToUpperCase(dictBean.getDictName()));
        //字典包
        PsiPackage dictPackage = JavaPsiFacade.getInstance(project)
                .findPackage(UseNeedImportClass.DictPackage
                        .getImportContent(project));
        if (dictPackage == null) {
            throw new RuntimeException("字典包[dict]丢失");
        }
        //目录
        PsiDirectory directory = dictPackage.getDirectories()[0];
        //检查字典名称是否已经存在
        PsiFile file = directory.findFile(dictBean.fileName());
        if (file != null) {
            throw new RuntimeException("字典[" + dictBean.getDictName() + "]已存在");
        }
        if (dictBean.getValues() == null || dictBean.getValues().size() == 0) {
            throw new RuntimeException("至少添加一个字典值");
        }
        Set<String> keys = new HashSet<>();
        //字段名处理
        for (DictValueBean value : dictBean.getValues()) {
            String name = value.getName();
            if (DJJHelper.isBlank(name)) {
                throw new RuntimeException(String.format("第[%s]条字典未输入有效Value",
                        value.getIndex()));
            }
            if (DJJHelper.isBlank(value.getDescribe())) {
                throw new RuntimeException(String.format("第[%s]条字典未输入有效Describe",
                        value.getIndex()));
            }
            name=DJJHelper.addLineAndUpperCase(name);
            if (!keys.add(name)) {
                throw new RuntimeException(String.format("第[%s]条字典输入了相同的Value[%s]",
                        value.getIndex(), name));
            }
            value.setName(name);
        }

        dictBean.setQualifiedName(dictPackage.getQualifiedName() + "." +
                dictBean.getDictName());
        PsiHelper.createJavaFile(project,
                "dictCreate.ftl", dictBean, directory);
    }

}
