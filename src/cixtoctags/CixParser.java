package cixtoctags;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.gjt.sp.jedit.jEdit;
import java.io.*;
import java.util.Vector;


public class CixParser {
    private String sigFilePath;

    public String parseCixList(Vector<String> cixList) {
        String tags = "";        
        // cixfiles to string
        if (cixList.size() > 0) {
            for (int i = 0; i < cixList.size(); i++) {
                tags += cixFileToTagsSigs(getPathToCix("cix/")+cixList.get(i));// one call per cix file                
            }
        }
        // write to tag file
        File cixFile = new File(getPathToCix("tags"));
        cixFile.delete();
        try {
            writeStringToFile(cixFile, tags);
        } catch (IOException io) {
            System.out.println("Error: "+io);
        }
        // path to tag file for ctagsinterfaceplugin
        tags = getPathToCix("tags");
        return tags;
    }

    private String cixFileToTagsSigs(String cixFile) {
        String[] fileTagsSigs = {"", ""};
        try {
            File cixXml = new File(cixFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document cixDoc = dBuilder.parse(cixXml);
            cixDoc.getDocumentElement().normalize();
            NodeList nList = cixDoc.getElementsByTagName("scope");
            Node nRoot = nList.item(0);
            Element elRoot = (Element) nRoot;
            String lang = elRoot.getAttribute("lang");            
            // make tag and sig, lines synced
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elChild = (Element) nNode;
                    String ilk = elChild.getAttribute("ilk");                    
                    if (ilk.matches("function")) {                                                                        
                        String[] temp = cixEntryToTagSigs(elChild, i*2+(i+1), lang);
                        fileTagsSigs[0] += temp[0];
                        fileTagsSigs[1] += temp[1];                        
                    }                    
                }
                sigFilePath = getPathToCix("sigs/"+lang+".sig");
                File sigFile = new File(sigFilePath);
                sigFile.delete();
                try {
                    writeStringToFile(sigFile, fileTagsSigs[1]);
                } catch (IOException io) {
                    System.out.println("Error: "+io);
                }
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileTagsSigs[0];
    }

    private String[] cixEntryToTagSigs(Element el, Integer line, String lang) {
        String doc = el.getAttribute("doc").replace("\\n", "");
        String name = el.getAttribute("name");
        String kind = el.getAttribute("ilk");
        String returns = el.getAttribute("returns");
        if (returns.isEmpty()) 
            returns="void";                
        String sig = returns+" "+kind+" "+name;
        String args = "", comma = "";
        NodeList childNodes = el.getChildNodes();
        if( childNodes != null ) {            
            for( int i = 0; i < childNodes.getLength(); i++ ) {
                Node childNode = childNodes.item(i);                 
                if(childNode.getNodeName().matches("variable")) {
                    Element elChild = (Element) childNode;
                    args += comma+elChild.getAttribute("citdl")+" "+elChild.getAttribute("name");                
                    comma = ", ";
                }
            }            
            args = args.trim();
        }
        sig += "("+args+"){}";
        String tag  = name+"\t" // TODO: get suggestions with args in ctagsinterface
                    + sigFilePath+"\t"
                    + "/^"+sig+"$/;\"\t"
                    + "kind:"+kind+"\t"
                    + "line:"+line+"\t"
                    + "language:"+lang+"\t"
                    + "doctype:tag\n";
        sig = "// "+lang+": "+doc+"\n"+sig+"\n\n";
        String[] entryTagSigs = {tag,sig};
        return entryTagSigs;
    }

    private String getPathToCix(String file) {
        String settingsDir = "~/.jedit";
        if (jEdit.getSettingsDirectory()!=null) 
            settingsDir = jEdit.getSettingsDirectory();        
        return settingsDir + "/CixToCtags/" + file;
    }

    private void writeStringToFile(File file, String str) throws IOException {
        FileWriter fstream = new FileWriter(file, true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(str.replace("\\", ""));
        out.close();
    }
   
//    public static void main(String[] args)  {
//        CixParser cp = new CixParser();
//        System.out.println(cp.getPathToCix("tags"));
//        String tags = cp.cixFileToTagSigs("/home/tp/.jedit/CixToCtags/cix/text.cix")[0];
//        System.out.println(tags); 
//        //System.out.println(cp.cixFileToTagSigs("/home/tp/.jedit/CixToCtags/cix/test1.cix")[1]);         
//       
//        File cixFile = new File("/home/tp/.jedit/CixToCtags/tags");
//        cixFile.delete();
//        try {
//            cp.writeStringToFile(cixFile, tags);
//        } catch (IOException io) {
//            System.out.println(io);
//        }        
 //   }    
    
}
