package com.coagent.jac.s7.fota.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Utils {
    public static boolean copyLibraryFile(String origPath, String destPath) {
        boolean copyIsFinish = false;
        try {
            File dirFile = new File(destPath.substring(0, destPath.lastIndexOf("/")));
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            FileInputStream is = new FileInputStream(new File(origPath));
            File file = new File(destPath);
            if (file.exists()) {
                // 如果文件存在则删掉
                file.delete();
//                if (file.length() == is.available()) {
//                    return true;
//                }
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }
}
