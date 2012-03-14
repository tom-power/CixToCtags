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
    private Progress progress;

    public Parser(Progress progress)
    {
        this.ph = new PathHelper();
        this.progress = progress;
    }

    public Vector<String> parseCixList(Vector<String> cixList)
    {
        Vector<String> tagPaths = new Vector<String>();
        // cixfiles to tags/sigs
        for (String cixFile : cixList) {
            tagFilePath = ph.getTagPath(cixFile);
            File tagFile = new File(tagFilePath);
            sigFilePath = ph.getSigPath(cixFile);
            File sigFile = new File(sigFilePath);
            if (!tagFile.exists() || !sigFile.exists()) {
                tagPaths.add(cixFileToTagsSigs(ph.getCixPath(cixFile), tagFile, sigFile));
            }
        }
        // tag file paths for ctagsinterfaceplugin to add
        return tagPaths;
    }

    public String parseCixFile(String cixFile)
    {
            tagFilePath = ph.getTagPath(cixFile);
            File tagFile = new File(tagFilePath);
            sigFilePath = ph.getSigPath(cixFile);
            File sigFile = new File(sigFilePath);
            if (!tagFile.exists() || !sigFile.exists())
                return cixFileToTagsSigs(ph.getCixPath(cixFile), tagFile, sigFile);
            else
                return "";
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

            // construct tag and sig string for file from each entry
            int lineSync = 1;
            int parsed = 1;
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elChild = (Element) nNode;
                    int line = lineSync * 2 + (lineSync - 2);
                    lineSync++;
                    String[] temp = cixEntryToTagSigs(elChild, line, lang);
                    fileTagsSigs[0] += temp[0];
                    fileTagsSigs[1] += temp[1];
                    parsed++;
                }
            }

            // write tag and sig files
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
        if (returns.isEmpty())
            returns = "void";
        String signature = el.getAttribute("signature").replace(name+"(", "(");
        String args = "", comma = "";
        NodeList childNodes = el.getChildNodes();
        if (childNodes.getLength() != 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeName().matches("variable")) {
                    Element elChild = (Element) childNode;
                    args += comma + elChild.getAttribute("citdl") + " " + elChild.getAttribute("name");
                    comma = ", ";
                }
            }
            args = args.trim();
            signature = "(" + args + ")";
        }
        String pattern = returns + " " + name + " " + signature;
        String tag = name + "\t"
                + sigFilePath + "\t"
                + "/^" + pattern + "$/;\"\t"
                + "signature:" + signature + "\t"
                + "kind:" + kind + "\t"
                + "line:" + line + "\t"
                + "language:" + lang + "\t"
                + "doc:" + doc + "\t"
                + "doctype:tag"
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