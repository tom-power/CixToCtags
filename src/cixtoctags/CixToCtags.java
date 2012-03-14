package cixtoctags;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.util.Vector;
import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import javax.swing.JScrollPane;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.gui.RolloverButton;

import ctagsinterface.main.CtagsInterfacePlugin;
import ctagsinterface.main.VFSHelper;

import cixtoctags.PathHelper;
import java.awt.Color;

@SuppressWarnings("serial")
public class CixToCtags extends JPanel
{

    private View view;
    JList cixList;
    DefaultListModel cixModel;
    private PathHelper ph;
//    private JLabel status;
    private Progress progress;

    public CixToCtags(View thisView)
    {
        this.view = thisView;
        this.ph = new PathHelper();
        setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.setBorder(new EmptyBorder(5,5,5,5));
        top.setPreferredSize(new Dimension(400, 40));
        add(top, BorderLayout.NORTH);
        top.setLayout(new BorderLayout(10, 5));
//        Vector<String> tagList = ph.getTagPaths();
        //JLabel status = new JLabel("Status: idle");
        //top.add(status, BorderLayout.CENTER);
        JProgressBar bar = new JProgressBar();
        this.progress = new Progress(bar);
        this.progress.setDefault();
        top.add(bar, BorderLayout.CENTER);
        cixModel = new DefaultListModel();
        Vector<String> cix = ph.getCixNames();
        for (int i = 0; i < cix.size(); i++)
        cixModel.addElement(cix.get(i));
        cixList = new JList(cixModel);
        JScrollPane scroller = new JScrollPane(cixList);
        add(scroller, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        add(bottom, BorderLayout.SOUTH);
        JPanel buttons = new JPanel();
        JButton add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
        buttons.add(add);
        JButton remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
        buttons.add(remove);
        JButton tag = new JButton("Tag");
        buttons.add(tag);
        bottom.add(buttons);

        add.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                VFSFileChooserDialog chooser = new VFSFileChooserDialog(
                        GUIUtilities.getParentDialog(view),
                        jEdit.getActiveView(), System.getProperty("user.home"),
                        VFSBrowser.OPEN_DIALOG, false, false);
                chooser.setTitle("Select .cix file");
                chooser.setVisible(true);
                if (chooser.getSelectedFiles() == null)
                    return;
                String cixAdd = chooser.getSelectedFiles()[0];
                String cixFileName = VFSHelper.getFileName(cixAdd);
                if (cixModel.toString().lastIndexOf(cixFileName) < 0) {
                    VFSHelper.copy(cixAdd, ph.getCixPath(cixFileName));
                    cixModel.addElement(cixFileName);
                }
                progress.setProgress(0);
            }
        });

        remove.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int i = cixList.getSelectedIndex();
                if (i < 0)
                    return;

                try {
                    String cixFileName = cixList.getSelectedValue().toString();
                    new File(ph.getCixPath(cixFileName)).delete();
                    new File(ph.getSigPath(cixFileName)).delete();
                    new File(ph.getTagPath(cixFileName)).delete();
                    CtagsInterfacePlugin.deleteTagsFromSourceFile(ph.getSigPath(cixFileName));
                    cixModel.removeElementAt(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

//        tag.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent ae)
//            {
//                new Thread(new Runnable() {
//                    public void run() {
//                        progress.setIndeterminate(true);
//                        progress.setString("Parsing");
//                        Parser cixParser = new Parser(progress);
//                        Vector<String> tagFiles = cixParser.parseCixList(ph.getCixNames());                        progress.setDefault();
        //                        for (String tagFile : tagFiles)
//                            CtagsInterfacePlugin.addTagFile(view, tagFile);
//                    }
//                }).start();
//            }
//        });

        tag.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                new Thread(new Runnable() {
                    public void run() {
                        progress.setIndeterminate(true);
                        Vector<String> tagFiles = new Vector<String>();
                        Parser cixParser = new Parser(progress);
                        for (String cixFile : ph.getCixNames()) {
                            progress.setString("Parsing "+cixFile);
                            tagFiles.add(cixParser.parseCixFile(cixFile));
                        }
                        for(String tagFile:tagFiles) {
                            if(!tagFile.isEmpty()) {
                                progress.setString("Inserting "+tagFile);
                                CtagsInterfacePlugin.addTagFile(view, tagFile);
                            }
                        }
                        progress.setDefault();
                    }
                }).start();
            }
        });

    }

}
