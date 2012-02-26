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

@SuppressWarnings("serial")
public class CixToCtags extends JPanel
{
    private View view;
	JList cixList;
	DefaultListModel cixModel;    
	
	public CixToCtags(View thisView)
	{
        this.view = thisView;
		setLayout(new BorderLayout());
		cixModel = new DefaultListModel();
		Vector<String> trees = getCix();
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
                
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
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
                // only copy if not there already
                if (cixModel.toString().lastIndexOf(cixFileName)< 0) {
                    VFSHelper.copy(cixAdd, getCixDirectory()+"/"+cixFileName);                
                    cixModel.addElement(cixFileName);
                }
			}
		});
                
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
                int i = cixList.getSelectedIndex();
				if (i < 0)
                    return;
                String cixFileName = cixList.getSelectedValue().toString();                                
                try { 
                    new File(getCixDirectory()+"/"+cixFileName).delete();
                } catch (Exception e) {
                }
                cixModel.removeElementAt(i);                                    
			}
		});
                
		tag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
                CixParser cixParser = new CixParser();
                String pathToTagFile = cixParser.parseCixList(getCix());
                if (pathToTagFile == null) 
                    return;                
                CtagsInterfacePlugin.addTagFile(view, pathToTagFile);
			}
		});          
	}
    
  	public Vector<String> getCix()
	{
		Vector<String> cix = new Vector<String>();
        File dir = new File(getCixDirectory());
        String[] children = dir.list();
        if (children != null) {
            for (int i=0; i<children.length; i++) 
                cix.add(children[i]);           
        }                
		return cix;
	}	
        
    private String getCixToCtagsDirectory() 
    {
		return jEdit.getSettingsDirectory() + "/CixToCtags/";
	}
    
    private String getCixDirectory() 
    {
		return getCixToCtagsDirectory() + "cix/";
	}    
}