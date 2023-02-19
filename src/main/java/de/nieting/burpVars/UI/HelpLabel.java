package de.nieting.burpVars.UI;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class HelpLabel extends JLabel {

    int DEFAULT_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
    int DEFAULT_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();

    public HelpLabel(String tooltipText) {
        super("?", null, CENTER);

        this.setBorder(new RoundedBorder());
        this.setToolTipText(tooltipText);

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(0);
                ToolTipManager.sharedInstance().setDismissDelay(30000);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_INITIAL_DELAY);
                ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_DISMISS_DELAY);
            }
        });
    }
}
