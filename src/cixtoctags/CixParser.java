//convert cix to ctags
package cixtoctags;

import org.gjt.sp.jedit.jEdit;
import java.io.*;
import java.util.Vector;

public class CixParser 
{   
    public String parseCixList(Vector<String> cixList)
    {
        File tagFile = new File(getPathToCixCtag());
        tagFile.delete();
        String tagStr = "";
        if (cixList.size()>0) {
            for (int i = 0; i < cixList.size(); i++) {
                tagStr = parseCixFile(cixList.get(i));
                try {
                    appendTagString(tagFile, tagStr+"\\\n");            
                } catch (IOException io) {

                }            
            }                
        }          
        return getPathToCixCtag();
    }
    
    public String parseCixFile(String cixFile)
    {
        String tag;
        tag = "getSearchActiveProjectAndDeps	/home/tp/wk/proj/jedit/CtagsInterface/src/ctagsinterface/options/ProjectsOptionPane.java	/^	static public boolean getSearchActiveProjectAndDeps()$/;\"	kind:method	line:191	language:Java	class:ProjectsOptionPane	access:public	signature:()";
        return tag;
    }
    
    public String getPathToCixCtag() 
    {
        return jEdit.getSettingsDirectory() + "/CixToCtags/tags";
    }
    
    private void appendTagString(File file, String tag) throws IOException
    {
        FileWriter fstream = new FileWriter(file,true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(tag.replace("\\", ""));
        out.close();    
    }
    
    
}
