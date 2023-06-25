package khanin.dmitrii.hashMap.UI;

import khanin.dmitrii.hashMap.Logic;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private JTextArea originalCodeTextArea;
    private JTextArea obfuscatedCodeTextArea;
    private JButton importBtn;
    private JButton exportBtn;
    private JButton obfuscationBtn;
    private JFileChooser fileChooserOpen;
    private JFileChooser fileChooserSave;

    public MainFrame() {
        this.setTitle("Simple obfuscation");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.setSize(1500, 500);

        obfuscatedCodeTextArea.setEditable(false);

        FileFilter javaFilter = new FileNameExtensionFilter("Java files (*.java)", "java");

        fileChooserOpen = new JFileChooser();
        fileChooserOpen.setCurrentDirectory(new File("./files/input"));
        fileChooserOpen.addChoosableFileFilter(javaFilter);

        fileChooserSave = new JFileChooser();
        fileChooserSave.setCurrentDirectory(new File("./files/input"));
        fileChooserSave.addChoosableFileFilter(javaFilter);
        fileChooserSave.setAcceptAllFileFilterUsed(false);
        fileChooserSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserSave.setApproveButtonText("Save");

        obfuscationBtn.addActionListener(new ObfuscationBtnActionListener());
    }

    class ObfuscationBtnActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            obfuscatedCodeTextArea.setText(Logic.obfuscation(originalCodeTextArea.getText()));
        }
    }
}
