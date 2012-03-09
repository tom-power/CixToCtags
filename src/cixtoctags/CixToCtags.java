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
import org.gjt.sp.jedit.Macros;

@SuppressWarnings("serial")
public class CixToCtags extends JPanel
{

    private View view;
    JList cixList;
    DefaultListModel cixModel;
    private PathHelper ph;

    public CixToCtags(View thisView)
    {
        this.view = thisView;
        this.ph = new PathHelper();
        setLayout(new BorderLayout());
        cixModel = new DefaultListModel();
        Vector<String> trees = ph.getCixNames();
        for (int i = 0; i < trees.size(); i++)
            cixModel.addElement(trees.get(i));
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
            }
        });

        remove.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int i = cixList.getSelectedIndex();
                if (i < 0) {
                    return;
                }
                try {
                    String cixFileName = cixList.getSelectedValue().toString();
                    new File(ph.getCixPath(cixFileName)).delete();
                    new File(ph.getSigPath(cixFileName)).delete();
                    new File(ph.getTagPath(cixFileName)).delete();
                    Macros.message(view, ph.getSigPath(cixFileName));
                    CtagsInterfacePlugin.deleteTagsFromSourceFile(ph.getSigPath(cixFileName));
                    cixModel.removeElementAt(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        tag.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                Parser cixParser = new Parser();
                Vector<String> tagFiles = cixParser.parseCixList(ph.getCixNames());
                for (String tagFile : tagFiles)
                    CtagsInterfacePlugin.addTagFile(view, tagFile);
            }
        });
    }
}
