package cixtoctags;

import org.gjt.sp.jedit.jEdit;
import java.util.Vector;
import java.io.*;
import ctagsinterface.main.VFSHelper;
import org.gjt.sp.jedit.Macros;

/**
 *
 * @author tp
 */
public class PathHelper
{

    public void deleteFiles(String fileName)
    {
        String[] types = {"cix", "sig", "tag"};
        for (String type : types) {
            new File(getPath(fileName, type)).delete();
        }
    }

    public String getFileName(String path)
    {
        return VFSHelper.getFileName(path);
//        String[] pathSplit = path.split("\\"+File.separator);
//        if(pathSplit.length>=1)
//            return pathSplit[pathSplit.length-1];
//        return "";
    }

    public String getFileNamePre(String path)
    {
        String fileName = getFileName(path);
        String[] fileSplit = fileName.split("\\.");
        String fileNamePre = "";
        if (fileSplit.length>=1) {
            fileNamePre = fileSplit[0];
            for (int i = 1; i < fileSplit.length-1; i++) {
                fileNamePre += "." + fileSplit[i];
            }
        }
        return fileNamePre;
    }

    public String getFileNameExt(String path)
    {
        String fileName = getFileName(path);
        String[] fileSplit = fileName.split("\\.");
        if(fileSplit.length>=1)
            return fileSplit[fileSplit.length-1];
        return "";
    }


    public void add(String filePath)
    {
        File file = new File(filePath);
        add(file);
    }

    public void add(File file)
    {
        try {
            File parentDir = new File(file.getParent());
            if (!parentDir.exists())
                parentDir.mkdirs();
            file.createNewFile();
        } catch (IOException ex) {
            System.out.println("Error while Creating File in Java" + ex);
        } catch (SecurityException ex) {
            System.out.println("Error while Creating File in Java" + ex);
        }
    }

    public void copy(String file, String path){
        add(path);
        VFSHelper.copy(file, path);
    }

    public String getCixPath(String fileName)
    {
        return getPath(fileName, "cix");
    }

    public String getSigPath(String fileName)
    {
        return getPath(fileName, "sig");
    }

    public String getTagPath(String fileName)
    {
        return getPath(fileName, "tag");
    }

    public String getCixDirectory()
    {
        return getDirectory("cix");
    }

    public String getSigDirectory()
    {
        return getDirectory("sig");
    }

    public String getTagDirectory()
    {
        return getDirectory("tag");
    }

    public Vector<String> getCixNames()
    {
        return getFileNames("cix");
    }

    public Vector<String> getCixPaths()
    {
        return getFilePaths("cix");
    }

    public Vector<String> getSigPaths()
    {
        return getFilePaths("sig");
    }

    public Vector<String> getTagPaths()
    {
        return getFilePaths("tag");
    }

    private Vector<String> getFilePaths(String fileType)
    {
        Vector<String> files = getFileNames(fileType);
        String dir = getDirectory(fileType);
        for (int i = 0; i < files.size(); i++) {
            files.set(i, dir + files.get(i));
        }
        return files;
    }

    private Vector<String> getFileNames(String fileType)
    {
        Vector<String> files = new Vector<String>();
        File dir = new File(getDirectory(fileType));
        String[] children = dir.list();
        if (children != null) {
            for (int i = 0; i < children.length; i++)
                files.add(children[i]);
        }
        return files;
    }

    private String getPath(String fileName, String type)
    {
        return getDirectory(type) + fileName + "." + type;
    }

    private String getDirectory(String dir)
    {
        return getCixToCtagsDirectory() + dir + File.separator ;
    }

    private String getCixToCtagsDirectory()
    {
        return jEdit.getSettingsDirectory() + File.separator + "CixToCtags" + File.separator ;
    }

}