package main.java.component;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * pdm文件选择器
 */
public class PDMFileChoose extends JFileChooser {

    public PDMFileChoose(String openDialog) {
        this(openDialog, null);
    }

    public PDMFileChoose(String openDialog, File file) {
        if (file == null) {
            file = FileSystemView.getFileSystemView().getHomeDirectory();
        }
        this.setCurrentDirectory(file);
        this.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        this.setApproveButtonText("请选择一个" + getTipText() + "文件");
        this.setApproveButtonToolTipText("只能选择" + getTipText() + "文件");

        ThisFileFilter thisFileFilter = new ThisFileFilter(this.getFileType());
        this.addChoosableFileFilter(thisFileFilter);
        this.setFileFilter(thisFileFilter);
        this.setDialogTitle(openDialog);
        int chooseFlag = this.showDialog(null, null);
//        if (JFileChooser.APPROVE_OPTION != chooseFlag || this.getSelectedFile().isDirectory()) {
//            throw new RuntimeException("未选择任何文件");
//        }
    }

    private String getFileType() {
        return ".pdm";
    }

    private String getTipText() {
        return "PDM";
    }

    class ThisFileFilter extends FileFilter {
        private String endWith;

        ThisFileFilter(String endWith) {
            this.endWith = endWith;
        }

        public String getDescription() {
            return "*" + endWith;
        }

        public boolean accept(File file) {
            String name = file.getName();
            return file.isDirectory() || name.toLowerCase().endsWith(endWith);
        }
    }
}
