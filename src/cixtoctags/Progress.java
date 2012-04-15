package cixtoctags;

import javax.swing.JProgressBar;


@SuppressWarnings("serial")
public class Progress
{
    private JProgressBar bar;

    public Progress(JProgressBar bar)
	{
        this.bar = bar;
        setDefault();
    }

    public void setDefault()
    {
        bar.setVisible(true);
        bar.setStringPainted(true);
        bar.setString("Idle");
        bar.setIndeterminate(false);
        setProgressParams(0, 100);
    }

    public void setProgressParams(int min, int max)
    {
        bar.setVisible(true);
        bar.setMinimum(min);
        bar.setMaximum(max);
    }

    public void setProgress(int value)
    {
        bar.setValue(value);
    }

    public void setString(String str)
    {
        bar.setString(str);
    }

    public void setIndeterminate(Boolean set)
    {
        bar.setIndeterminate(set);
    }

    public void setVisible(Boolean set)
    {
        bar.setVisible(set);
    }
}
