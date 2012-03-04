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

    public String parseCixList(Vector<String> cixList) {
        String[] tagsSigs = {"", ""};
        String[] temp = {"", ""};
        // cixfiles to string
        if (cixList.size() > 0) {
            for (int i = 0; i < cixList.size(); i++) {
                temp = cixFileToTagSigs(cixList.get(i));// one call per cix file
                for (int j = 0; j < temp.length; j++) {
                    tagsSigs[j] += temp[j];
                }
            }
        }
        // write to tags/sigs files
        String[] tagsSigsNames = { "tags","sigs"};        
        for (int k = 0; k < tagsSigs.length; k++) {
            File cixFile = new File(getPathToCix(tagsSigsNames[k]));
            cixFile.delete();
            try {
                writeStringToFile(cixFile, tagsSigs[k]);
            } catch (IOException io) {
                System.out.println("Error: "+io);
            }
        }
        // path to tag file for ctagsinterfaceplugin
        return getPathToCix("tags");
    }

    private String[] cixFileToTagSigs(String cixFile) {
        String[] fileTagSigs = {"", ""};
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
                        String[] temp = cixEntryToTagSigs(elChild, i+(i-1), lang);
                        fileTagSigs[0] += temp[0];
                        fileTagSigs[1] += temp[1];                        
                    }                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileTagSigs;
    }

    private String[] cixEntryToTagSigs(Element el, Integer line, String lang) {
        String doc = el.getAttribute("doc");
        String name = el.getAttribute("name");
        String kind = el.getAttribute("ilk");
        String returns = el.getAttribute("returns");
        if (returns.isEmpty()) 
            returns="void";                
        String sig = returns+" "+kind+" "+name;
        String args = "";
        NodeList childNodes = el.getChildNodes();
        if( childNodes != null ) {            
            for( int i = 0; i < childNodes.getLength(); i++ ) {
                Node childNode = childNodes.item(i);                 
                if(childNode.getNodeName().matches("variable")) {
                    Element elChild = (Element) childNode;
                    args += " "+elChild.getAttribute("citdl")+" "+elChild.getAttribute("name");                
                }
            }            
            args = args.trim();
        }
        sig += "("+args+"){}";
        String tag = name+" "+getPathToCix("sig") +" /^"+sig+"$/;\" kind:"+kind+" line:"+line+" language:"+lang+"\n";
        sig = "//"+doc+"/n"+sig;
        String[] entryTagSigs = {tag,sig};
        return entryTagSigs;
    }

    private String getPathToCix(String file) {
        return jEdit.getSettingsDirectory() + "/CixToCtags/" + file;
    }

    private void writeStringToFile(File file, String str) throws IOException {
        FileWriter fstream = new FileWriter(file, true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(str.replace("\\", ""));
        out.close();
    }
    
//    public static void main(String[] args) {
//        CixParser cp = new CixParser();
//        String tags = cp.cixFileToTagSigs("/home/tp/.jedit/CixToCtags/cix/test.cix")[0];
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
//    }    
    
}
