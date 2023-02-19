package de.nieting.burpVars.UI;

import burp.api.montoya.ui.Theme;
import de.nieting.burpVars.API;

import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class RoundedBorder implements Border {

    public Insets getBorderInsets(Component c) {
        return new Insets(3,8,3,8);
    }


    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        float offs = 1.5F;
        float size = offs + offs;
        float arc = .2f * offs;
        var outer = new Ellipse2D.Float(x, y, width, height);
        var inner = new Ellipse2D.Float(x + offs, y + offs, width - size, height - size);

        var g2d = (Graphics2D)g;
        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        path.append(outer, false);
        path.append(inner, false);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (!c.isEnabled()) {
            g2d.setColor(new Color(213,213,213));
        } else {
            g2d.setColor(new Color(166,166,166));
        }

        if (API.getInstance() != null && API.getInstance().getApi().userInterface().currentTheme() == Theme.DARK) {
            if (!c.isEnabled()) {
                g2d.setColor(new Color(81,81,81));
            } else {
                g2d.setColor(new Color(96,96,96));
            }
        }

        g2d.fill(path);



//        g.drawOval(x, y, 20, 20);
    }
}
