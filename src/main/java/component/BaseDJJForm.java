package main.java.component;

import main.java.common.DoFunction;

import javax.swing.*;
import java.awt.*;

/**
 * 基础窗体
 */
public abstract class BaseDJJForm {

    /**
     * 窗体是否显示
     *
     * @param function
     * @param frames
     */
    public static void frameVisibleHandle(DoFunction function, JFrame... frames) {
        // 因为Frame最开始设置为在最前端展示，
        // 理论上打开 TreeJavaClassChooserDialog 类选择器的时候应该把窗体先隐藏掉
        // 但当TreeJavaClassChooserDialog 选择类之后，再显示Frame 时，窗体会自动最大化
        // 这个bug在调试阶段不会出现，但插件打包后100%复现，不知道跟idea 版本还是mac操作系统有关
        // 解决方案 替换为 当打开TreeJavaClassChooserDialog 时，把窗体在最前端显示设置为false

        for (JFrame frame : frames) {
            //jFrame.setVisible(false);
            frame.setAlwaysOnTop(false);
        }
        function.todo();
        for (JFrame frame : frames) {
            frame.setAlwaysOnTop(true);
            frame.setExtendedState(JFrame.NORMAL);//正常大小
            frame.setVisible(true);//重写显示窗体
        }

    }

    protected JFrame windowCenter(String title, JPanel jPanel) {
        return windowCenter(title, jPanel, null, JFrame.DISPOSE_ON_CLOSE);
    }

    protected JFrame windowCenter(String title, JPanel jPanel, Dimension frameSize) {
        return windowCenter(title, jPanel, frameSize, JFrame.DISPOSE_ON_CLOSE);
    }

    protected JFrame windowCenter(String title, JPanel jPanel, Dimension frameSize, int operation) {
        JFrame frame = new JFrame(title);
        frame.setContentPane(jPanel);
        frame.setDefaultCloseOperation(operation);
        frame.pack();
        if (frameSize != null) {
            frame.setSize(frameSize);
        }
        this.frameCenter(frame);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        return frame;
    }

    /**
     * 窗体居中显示
     * @param frame
     */
    protected void frameCenter(JFrame frame){
        Dimension size = frame.getSize();
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setBounds((int) (width - size.getWidth()) / 2,
                (int) (height - size.getHeight()) / 4, (int) size.getWidth(), (int) size.getHeight());
    }

    /**
     * 调整高度
     * @param jFrame
     * @param adjustHeight
     * @return
     */
    protected void adjustHeight(JFrame jFrame,int adjustHeight) {
        Dimension size = jFrame.getSize();
        jFrame.setSize(new Dimension(Double.valueOf(size.width).intValue(), adjustHeight));
        this.frameCenter(jFrame);
        jFrame.setVisible(true);
    }
}
