package com.pack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java.io.FileOutputStream; 

import org.apache.tools.ant.Project; 
import org.apache.tools.ant.taskdefs.Expand;

import de.innosystec.unrar.Archive; 
import de.innosystec.unrar.rarfile.FileHeader; 

public class ZipRarRelevant {
	/**
	 * ��ȡ�ļ���������ѹ����
	 * @param Path
	 * @param Listpath
	 * @param Listname
	 */
	public static void ReadFilesZip(String Path,List<String> Listpath,List<String> Listname) {
		File file = new File(Path);
		if(file.exists()) {
			File[] files = file.listFiles();
			for(File f : files) {
				if(f.isDirectory()) {
					ReadFilesZip(f.getPath(), Listpath, Listname);
				}else {
					if( f.getPath().endsWith("zip") || f.getPath().endsWith("ZIP")
					 || f.getPath().endsWith("rar") || f.getPath().endsWith("RAR") ) {
						if(Listpath != null) {
							Listpath.add(f.getPath());
						}
						if(Listname != null) {
							Listname.add(f.getName());
						}
					}	
				}
			}
		}
	}	
	   /**  
	* ��ѹzip��ʽѹ����  
	* ��Ӧ����ant.jar  
	*/   
	private static void unzip(String sourceZip,String destDir) throws Exception{ 
		try{ 
			Project p = new Project(); 
			Expand e = new Expand(); 
			e.setProject(p); 
			e.setSrc(new File(sourceZip)); 
			e.setOverwrite(false); 
			e.setDest(new File(destDir)); 
			e.setEncoding("gbk"); //��ѹ��ʱҪ�ƶ������ʽ  
			e.execute(); 
		}catch(Exception e){ 
			throw e; 
		} 
	} 
	/**  
	 * ��ѹrar��ʽѹ������  
	 * ��Ӧ����java-unrar-0.3.jar������java-unrar-0.3.jar�ֻ��õ�commons-logging-1.1.1.jar  
	 */
	private static void unrar(String sourceRar,String destDir) throws Exception{ 
		 Archive a = null; 
		 FileOutputStream fos = null; 
		 try{ 
			  a = new Archive(new File(sourceRar)); 
			  FileHeader fh = a.nextFileHeader(); 
			  while(fh!=null){ 
				if(!fh.isDirectory()){ 
					 //1 ���ݲ�ͬ�Ĳ���ϵͳ�õ���Ӧ�� destDirName �� destFileName 
					 String compressFileName = fh.getFileNameString().trim(); 
					 String destFileName = ""; 
					 String destDirName = ""; 
					 //��windowsϵͳ 
					 if(File.separator.equals("/")){ 
						  destFileName = destDir + compressFileName.replaceAll("\\\\", "/"); 
						  destDirName = destFileName.substring(0, destFileName.lastIndexOf("/")); 
					 //windowsϵͳ  
					 }else{ 
						  destFileName = destDir + compressFileName.replaceAll("/", "\\\\"); 
						  destDirName = destFileName.substring(0, destFileName.lastIndexOf("\\")); 
					 } 
					 //2�����ļ��� 
					 File dir = new File(destDirName); 
					 if(!dir.exists()||!dir.isDirectory()){ 
						 dir.mkdirs(); 
					 } 
					 //3��ѹ���ļ� 
					 fos = new FileOutputStream(new File(destFileName)); 
					 a.extractFile(fh, fos); 
					 fos.close(); 
					 fos = null; 
				} 
				fh = a.nextFileHeader(); 
		  } 
			  a.close(); 
			  a = null; 
		 }catch(Exception e){ 
			 throw e; 
		 }finally{ 
			 if(fos!=null){ 
				 try{fos.close();fos=null;}catch(Exception e){e.printStackTrace();} 
			 } 
			 if(a!=null){ 
				 try{a.close();a=null;}catch(Exception e){e.printStackTrace();} 
			 } 
		 } 
	} 
	/**  
	 * ��ѹ��  
	 */
	public static void deCompress(String sourceFile,String destDir) throws Exception{ 
		//��֤�ļ���·�������"/"����"\" 
		char lastChar = destDir.charAt(destDir.length()-1); 
		if(lastChar!='/'&&lastChar!='\\'){ 
			destDir += File.separator; 
		} 
		//�������ͣ�������Ӧ�Ľ�ѹ�� 
		String type = sourceFile.substring(sourceFile.lastIndexOf(".")+1); 
		if(type.equals("zip")){ 
			unzip(sourceFile, destDir); 
		}else if(type.equals("rar")){ 
			unrar(sourceFile, destDir); 
		}else{ 
			throw new Exception("ֻ֧��zip��rar��ʽ��ѹ������"); 
		} 
	} 
	/**
	 * ѹ����ת�ļ�
	 * @param Path
	 */
	public static void ZipRar2files(String Path) {
		String ZipPath = "";
		String ZipFatherPath = "";
		List<String> Listpath = new ArrayList<String>();
		
		ReadFilesZip(Path, Listpath, null);
		for(int i = 0; i < Listpath.size(); i++) {
			ZipPath = Listpath.get(i);
			File files = new File(ZipPath.substring(0, ZipPath.length()-4)); 
			files.mkdirs(); 
			ZipFatherPath = ZipPath.substring(0, ZipPath.length()-4) + "\\";
			try {
				deCompress(ZipPath, ZipFatherPath);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}	
	}	
}
