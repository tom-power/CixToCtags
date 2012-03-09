package cixtoctags;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.*;
import java.util.Vector;

import cixtoctags.PathHelper;

public class Parser
{

    private String sigFilePath;
    private String tagFilePath;
    private PathHelper ph;

    public Parser()
    {
        this.ph = new PathHelper();
    }

    public Vector<String> parseCixList(Vector<String> cixList)
    {
        Vector<String> tags = new Vector<String>();
        // cixfiles to string
        for (String cixFile : cixList) {
            tagFilePath = ph.getTagPath(cixFile);
            File tagFile = new File(tagFilePath);

            sigFilePath = ph.getSigPath(cixFile);
            File sigFile = new File(sigFilePath);

            if (!tagFile.exists() || !sigFile.exists()) {
                tags.add(cixFileToTagsSigs(ph.getCixPath(cixFile), tagFile, sigFile));
            }
        }
        // new tag files for ctagsinterfaceplugin to add
        return tags;
    }

    private String cixFileToTagsSigs(String cixFile, File tagFile, File sigFile)
    {
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

            // construct tag and sig from each entry
            int k = 1;
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elChild = (Element) nNode;
                    String ilk = elChild.getAttribute("ilk");
                    if (ilk.matches("function")) {
                        int line = k * 2 + (k - 2);
                        k++;
                        String[] temp = cixEntryToTagSigs(elChild, line, lang);
                        fileTagsSigs[0] += temp[0];
                        fileTagsSigs[1] += temp[1];
                    }
                }
            }
            // write files
            try {
                writeStringToFile(tagFile, fileTagsSigs[0]);
                writeStringToFile(sigFile, fileTagsSigs[1]);
            } catch (IOException io) {
                System.out.println("Error: " + io);
            }
            return tagFilePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String[] cixEntryToTagSigs(Element el, Integer line, String lang)
    {
        String doc = el.getAttribute("doc").replace(System.getProperty("line.separator"), "");
        String name = el.getAttribute("name");
        String kind = el.getAttribute("ilk");
        String returns = el.getAttribute("returns");
        if (returns.isEmpty()) {
            returns = "void";
        }

        String args = "", comma = "";
        NodeList childNodes = el.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeName().matches("variable")) {
                    Element elChild = (Element) childNode;
                    args += comma + elChild.getAttribute("citdl") + " " + elChild.getAttribute("name");
                    comma = ", ";
                }
            }
            args = args.trim();
        }
        String signature = "(" + args + ")";
        String pattern = returns + " " + name + " " + signature;
        String tag = name + "\t"
                + sigFilePath + "\t"
                + "/^" + pattern + "$/;\"\t"
                + "signature:" + signature + "\t"
                + "kind:" + kind + "\t"
                + "line:" + line + "\t"
                + "language:" + lang + "\t"
                + "doc:" + doc + "\t"
                + "doctype:tag"// doctype:origin +type:Misc +id:temp
                + "\n";
        String sig = "// " + doc + "\n" + returns + " " + kind + " " + name + " " + signature + "\n\n";
        String[] entryTagSigs = {tag, sig};
        return entryTagSigs;
    }

    private void writeStringToFile(File file, String str) throws IOException
    {
        FileWriter fstream = new FileWriter(file, true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(str.replace("\\", ""));
        out.close();
    }
}