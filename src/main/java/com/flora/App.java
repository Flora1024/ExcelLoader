package com.flora;

import com.flora.reader.ExcelReader;

import javax.swing.*;
import java.io.File;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel文件", "xlsx"));
        fileChooser.setDialogTitle("请选择Excel文件");

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("用户选择的文件: " + selectedFile.getAbsolutePath());

            String outputFileName = selectedFile.getName().replace(".xlsx", ".md");
            ExcelReader.readAndConvert(selectedFile.getAbsolutePath(), outputFileName);
        } else {
            System.out.println("用户取消了选择");
        }
    }
}
