package cixtoctags;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
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
import org.gjt.sp.jedit.Macros;

import ctagsinterface.main.CtagsInterfacePlugin;

import cixtoctags.PathHelper;

@SuppressWarnings("serial")
public class CixToCtags extends JPanel
{

    private View view;
    JList cixList;
    DefaultListModel cixModel;
    private PathHelper ph;
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
                new Thread(new Runnable() {
                    public void run() {
                        VFSFileChooserDialog chooser = new VFSFileChooserDialog(
                                GUIUtilities.getParentDialog(view),
                                jEdit.getActiveView(), System.getProperty("user.home"),
                                VFSBrowser.OPEN_DIALOG, false, false
                                );
                        chooser.setTitle("Select .cix file");
                        chooser.setVisible(true);
                        String cixAdd = chooser.getSelectedFiles()[0];
                        if (cixAdd == null) {
                            Macros.message(view, "Nothing selected");
                            return;
                        }
                        if (!ph.getFileNameExt(cixAdd).equals("cix")) {
                            Macros.message(view, "Selected file not a .cix file");
                            return;
                        }
                        progress.setIndeterminate(true);
                        progress.setString("Adding");
                        String cixFileName = ph.getFileName(cixAdd);
                        if (cixModel.toString().lastIndexOf(cixFileName) < 0) {
                            ph.copy(cixAdd, ph.getCixPath(ph.getFileNamePre(cixFileName)));
                            cixModel.addElement(cixFileName);
                        }
                        progress.setDefault();
                    }
                }).start();
            }
        });

        remove.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                new Thread(new Runnable() {
                    public void run() {
                        int i = cixList.getSelectedIndex();
                        if (i < 0)
                            return;
                        progress.setIndeterminate(true);
                        progress.setString("Removing");
                        try {
                            String cixFileNamePre = ph.getFileNamePre(cixList.getSelectedValue().toString());
                            CtagsInterfacePlugin.deleteTagsFromTagFile(view, ph.getTagPath(cixFileNamePre));
                            ph.deleteFiles(cixFileNamePre);
                            cixModel.removeElementAt(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        progress.setDefault();
                    }
                }).start();
            }


        });

        tag.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                new Thread(new Runnable() {
                    public void run() {
                        progress.setIndeterminate(true);
                        Vector<String> tagFilePaths = new Vector<String>();
                        Parser cixParser = new Parser(view);
                        for (String cixFile : ph.getCixNames()) {
                            progress.setString("Parsing "+cixFile);
                            tagFilePaths.add(cixParser.parseCixFile(cixFile));
                        }
                        for(String tagFilePath:tagFilePaths) {
                            if(!tagFilePath.isEmpty()) {
                                progress.setString("Inserting "+ph.getFileName(tagFilePath));
                                CtagsInterfacePlugin.addTagFile(view, tagFilePath);
                            }
                        }
                        progress.setDefault();
                    }
                }).start();
            }
        });

    }

}
