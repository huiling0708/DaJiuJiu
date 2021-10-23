package main.java.form;

import main.java.bean.DataEntity;
import main.java.common.DJJMessage;
import main.java.common.TableDataHandle;
import main.java.component.BaseDJJForm;
import main.java.component.PDMBuildEntityTable;
import main.java.component.PDMFileChoose;
import main.java.utils.PDMUtils;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 选择数据表窗体
 */
public class ChooseTableForm extends BaseDJJForm {
    private JPanel mainPanel;
    private JTextField tableText;
    private JTextField describeText;
    private JTable pdmTable;
    private JButton OKButton;
    private JButton cancelButton;
    private JButton pdmButton;
    private JComboBox pdmBox;
    private DataEntity selectedDataEntity;

    private final static List<String> pdmPathList = new ArrayList<>();

    public ChooseTableForm(Consumer<DataEntity> consumer) {
        JFrame jFrame = this.windowCenter("Create Entity", this.mainPanel);
        jFrame.getRootPane().setDefaultButton(OKButton);
        this.loadPdmBox(null);
        if (pdmPathList.size() != 0) {
            String path = pdmPathList.get(0);
            updatePdmTableData(path);
        }
        //选择pdm文件
        pdmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameVisibleHandle(() -> {
                    pdmButtonHandle();
                    tableText.setText("");
                    describeText.setText("");
                }, jFrame);
            }
        });
        //选择监听
        pdmBox.addItemListener(e -> {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                String item = (String) e.getItem();
                DJJComboBox box = (DJJComboBox) pdmBox;
                String path = box.getValue(item);
                updatePdmTableData(path);
            }
        });
        pdmTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = pdmTable.getSelectedRow();
            if (selectedRow != -1) {
                PDMBuildEntityTable table = (PDMBuildEntityTable) pdmTable;
                DataEntity dataEntity = table.getHandle().getDataList().get(selectedRow);
                setSelectedDataEntity(dataEntity);
            }
        });
        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    okHandle(consumer);
                    jFrame.dispose();
                } catch (Exception exception) {
                    DJJMessage.errorDialog(exception);
                }
            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jFrame.dispose();
            }
        });
    }

    /**
     * ok 处理
     *
     * @param consumer
     */
    private void okHandle(Consumer<DataEntity> consumer) {
        if (selectedDataEntity == null) {
            throw new RuntimeException("请先选择数据表");
        }
        DJJComboBox box = (DJJComboBox) pdmBox;
        String path = box.getValue((String) box.getSelectedItem());
        //解析指定数据表 补全DataEntity 其它值
        PDMUtils.handleSingleDataEntity(new File(path), selectedDataEntity);
        consumer.accept(selectedDataEntity);
    }

    /**
     * 设置选择的数据实体
     *
     * @param dataEntity
     */
    private void setSelectedDataEntity(DataEntity dataEntity) {
        this.selectedDataEntity = dataEntity;
        if (this.selectedDataEntity == null) {
            this.tableText.setText("");
            this.describeText.setText("");
        } else {
            this.tableText.setText(selectedDataEntity.getTableName());
            this.describeText.setText(selectedDataEntity.getDescribe());
        }
    }

    /**
     * 更新pdm table 数据
     *
     * @param path
     */
    private void updatePdmTableData(String path) {
        //解析pdm文件获取 实体数据bean 列表
        List<DataEntity> dataEntityList =
                PDMUtils.getDataEntityList(new File(path));
        PDMBuildEntityTable table = (PDMBuildEntityTable) this.pdmTable;
        TableDataHandle handle = new TableDataHandle(DataEntity.class, dataEntityList,
                false, false);
        table.updateHandle(handle);
    }

    /**
     * pdm选择按钮处理
     */
    private void pdmButtonHandle() {
        //根据pdm选择器获取 pdm源文件
        PDMFileChoose choose = new PDMFileChoose("请选择你的数据模型PDM文件");
        File selectedFile = choose.getSelectedFile();
        if (selectedFile == null || selectedFile.isDirectory()) {
            DJJMessage.errorDialog("未选择任何文件");
            return;
        }
        String path = selectedFile.getPath();
        if (!pdmPathList.contains(path)) {
            pdmPathList.add(path);
        }
        loadPdmBox(path);
        this.updatePdmTableData(path);
    }

    /**
     * 加载pdm box
     *
     * @param path
     */
    private void loadPdmBox(String path) {
        if (pdmPathList.size() == 0) {
            return;
        }
        pdmBox.removeAllItems();
        pdmPathList.forEach(x -> {
            pdmBox.addItem(x);
        });
        if (path == null) {
            pdmBox.setSelectedIndex(0);
        } else {
            ((DJJComboBox) pdmBox).setItemByPath(path);
        }
    }

    private void createUIComponents() {
        pdmTable = new PDMBuildEntityTable(new TableDataHandle(DataEntity.class, new ArrayList(),
                false, false));
        pdmBox = new DJJComboBox();
    }

    private class DJJComboBox extends JComboBox {
        private Map<String, String> pathMap = new HashMap<>();

        /**
         * 重写addItem方法，让 取路径尾部的文件名称作为展示
         * 同时可以根据文件名称获取完整路径
         *
         * @param item
         */
        @Override
        public void addItem(Object item) {
            String itemValue = (String) item;
            int i = itemValue.lastIndexOf(File.separator);
            if (i > 0) {
                String substring = itemValue.substring(i + 1);
                pathMap.put(substring, itemValue);
                pathMap.put(itemValue, substring);
                super.addItem(substring);
            } else {
                pathMap.put(itemValue, itemValue);
                super.addItem(itemValue);
            }
        }

        public void setItemByPath(String path) {
            this.setSelectedItem(getValue(path));
        }

        public String getValue(String item) {
            return pathMap.get(item);
        }
    }
}
