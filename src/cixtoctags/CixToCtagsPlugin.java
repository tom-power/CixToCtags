package cixtoctags;

import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.jEdit;
import java.util.Vector;
import java.io.File;
import org.gjt.sp.jedit.Macros;

public class CixToCtagsPlugin extends EditPlugin {
    public static final String NAME = "cixtoctags";

    public String getCixPath(String cixFileName)
    {
		return getCixDirectory()+cixFileName;
	}

    public String getSigPath(String cixFileName)
    {
		return getSigDirectory()+cixFileName.replace("cix", "sig");
	}

    public String getTagPath(String cixFileName)
    {
		return getTagDirectory()+cixFileName.replace("cix", "tag");
	}

    public String getDeletePath()
    {
		return getTagDirectory()+"delete";
	}

    public String getCixDirectory()
    {
		return getDirectory("cix");
	}

    public String getSigDirectory()
    {
		return getDirectory("sigs");
	}

    public String getTagDirectory()
    {
		return getDirectory("tags");
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
		return getFilePaths("sigs");
	}

    public Vector<String> getTagPaths()
	{
		return getFilePaths("tags");
	}

    private Vector<String> getFilePaths(String fileType) {
        Vector<String> files = getFileNames(fileType);
        String dir = getDirectory(fileType);
        for (int i = 0; i < files.size(); i++)
            files.set(i, dir+files.get(i));
        return files;
    }

    private Vector<String> getFileNames(String fileType) {
        Vector<String> files = new Vector<String>();
        File dir = new File(getDirectory(fileType));
        String[] children = dir.list();
        if (children != null) {
            for (int i=0; i<children.length; i++)
                if(!children[i].matches("delete"))
                    files.add(children[i]);
        }
        return files;
    }


    private String getDirectory(String dir) {
        return getCixToCtagsDirectory() + dir + "/";
    }

    private String getCixToCtagsDirectory()
    {
		String settingsDir = "~/.jedit";
        if (jEdit.getSettingsDirectory()!=null)
            settingsDir = jEdit.getSettingsDirectory();
        return settingsDir + "/CixToCtags/";
	}

    public static void main(String[] args)  {
//         CixToCtagsPlugin cp = new CixToCtagsPlugin();
//         Vector<String> tags = cp.getCixPaths() ;
//         System.out.println(tags.size());
//         for (String tag:tags)
//             System.out.println(tag);
    }
}