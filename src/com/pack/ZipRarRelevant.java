package com.pack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.FileOutputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

public class ZipRarRelevant {
    static String[] zipEx = new String[]{".zip", ".rar"};

    /**
     * 读取文件夹内所有压缩包
     *
     * @param Path
     * @param Listpath
     * @param Listname
     */
    public static void ReadFilesZip(String Path, List<String> Listpath, List<String> Listname) {
        File file = new File(Path);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    ReadFilesZip(f.getPath(), Listpath, Listname);
                } else {
                    String filePath = f.getPath();
                    if (filePath.indexOf(".") != -1) {
                        String dotName = filePath.substring(filePath.indexOf(".")).toLowerCase();
                        List<String> zipExList = Arrays.asList(zipEx);
                        if (zipExList.contains(dotName)) {
                            if (Listpath != null) {
                                Listpath.add(f.getPath());
                            }
                            if (Listname != null) {
                                Listname.add(f.getName());
                            }
                        }
                    }
                }
            }//for
        }
    }

    /**
     * 解压zip格式压缩包
     * 对应的是ant.jar
     */
    private static void unzip(String sourceZip, String destDir) throws Exception {
        Project p = new Project();
        Expand e = new Expand();
        e.setProject(p);
        e.setSrc(new File(sourceZip));
        e.setOverwrite(false);
        e.setDest(new File(destDir));
        e.setEncoding("gbk"); //解压缩时要制定编码格式 winRAR软件压缩是用的windows默认的GBK或者GB2312编码
        e.execute();
    }

    /**
     * 解压rar格式压缩包。
     * 对应的是java-unrar-0.3.jar，但是java-unrar-0.3.jar又会用到commons-logging-1.1.1.jar
     */
    private static void unrar(String sourceRar, String destDir) throws Exception {
        Archive a = null;
        FileOutputStream fos = null;
        try {
            a = new Archive(new File(sourceRar));
            FileHeader fh = a.nextFileHeader();
            while (fh != null) {
                if (!fh.isDirectory()) {
                    //1 根据不同的操作系统拿到相应的 destDirName 和 destFileName
                    String compressFileName = "";
                    if (fh.isUnicode()) {//解決中文乱码
                        compressFileName = fh.getFileNameW().trim();
                    } else {
                        compressFileName = fh.getFileNameString().trim();
                    }
                    String destFileName = "";
                    String destDirName = "";
                    //非windows系统
                    if (File.separator.equals("/")) {
                        destFileName = destDir + compressFileName.replaceAll("\\\\", "/");
                        destDirName = destFileName.substring(0, destFileName.lastIndexOf("/"));
                        //windows系统
                    } else {
                        destFileName = destDir + compressFileName.replaceAll("/", "\\\\");
                        destDirName = destFileName.substring(0, destFileName.lastIndexOf("\\"));
                    }
                    //2创建文件夹
                    File dir = new File(destDirName);
                    if (!dir.exists() || !dir.isDirectory()) {
                        dir.mkdirs();
                    }
                    //3解压缩文件
                    fos = new FileOutputStream(new File(destFileName));
                    a.extractFile(fh, fos);
                    fos.close();
                    fos = null;
                }
                fh = a.nextFileHeader();
            }
            a.close();
            a = null;
        } catch (Exception e) {
            throw e;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (a != null) {
                try {
                    a.close();
                    a = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 解压缩
     */
    public static void deCompress(String sourceFile, String destDir) throws Exception {
        //保证文件夹路径最后是"/"或者"\"
        char lastChar = destDir.charAt(destDir.length() - 1);
        if (lastChar != '/' && lastChar != '\\') {
            destDir += File.separator;
        }
        //根据类型，进行相应的解压缩
        String type = sourceFile.substring(sourceFile.indexOf(".")).toLowerCase();
        if (type.equals(zipEx[0])) {
            unzip(sourceFile, destDir);
        } else if (type.equals(zipEx[1])) {
            unrar(sourceFile, destDir);
        } else {
            throw new Exception("只支持zip和rar格式的压缩包！");
        }
    }

    /**
     * 压缩包转文件 并删除原压缩包
     *
     * @param Path
     */
    public static void ZipRar2files(String Path) {
        String ZipPath = "";
        String ZipFatherPath = "";
        List<String> Listpath = new ArrayList<String>();

        ReadFilesZip(Path, Listpath, null);                                        //读取路径下所有压缩文件
        for (int i = 0; i < Listpath.size(); i++) {
            ZipPath = Listpath.get(i);                                            //得到一个压缩文件路径
            File files = new File(ZipPath.substring(0, ZipPath.length() - 4));    //↓↓
            files.mkdirs();                                                    //新建一个与压缩文件同名的文件夹
            ZipFatherPath = ZipPath.substring(0, ZipPath.length() - 4) + "\\";    //得到压缩文件父目录
            try {
                deCompress(ZipPath, ZipFatherPath);                                //压缩并输出到“ZipFatherPath”
                new File(ZipPath).delete();                                        //删除源文件
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
