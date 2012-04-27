// SelectableChartPanel.java
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.event.*;

import org.jfree.chart.*;
import org.jfree.ui.ExtensionFileFilter;

    public class SelectableChartPanel extends ChartPanel {
        Rectangle currentRect = null;
        Rectangle rectToDraw = null;
        Rectangle previousRectDrawn = new Rectangle();
        SelectableChartPanelController controller;
        MyListener myListener;
        boolean draw = false;
        Settings settings = Settings.getInstance();

    
        public SelectableChartPanel (JFreeChart chart, SelectableChartPanelController controller) {
            super(chart);
            
            this.controller = controller;
            setOpaque(true);
            setMinimumSize(new Dimension(10,10)); //don't hog space
    
            myListener = new MyListener();
        }
        
        public void setEnableCustomDrag(boolean enableCustomDrag) {
            this.draw = enableCustomDrag;
            if (enableCustomDrag) {
                addMouseListener(myListener);
                addMouseMotionListener(myListener);
            } else {
                removeMouseListener(myListener);
                removeMouseMotionListener(myListener);
            }
        }
        
        /**
         * override ChartPanel.soSaveAs to remember a working directory
         */
        public void doSaveAs() throws IOException {
          System.out.println("working directory = " + String.valueOf(settings.getWorkingDirectory()));
          JFileChooser fileChooser = new JFileChooser(settings.getWorkingDirectory());
          ExtensionFileFilter filter = new ExtensionFileFilter(
              localizationResources.getString("PNG_Image_Files"), ".png"
          );
          fileChooser.addChoosableFileFilter(filter);
  
          int option = fileChooser.showSaveDialog(this);
          if (option == JFileChooser.APPROVE_OPTION) {
              String filename = fileChooser.getSelectedFile().getPath();
              settings.setWorkingDirectory(
                  fileChooser.getSelectedFile().getParentFile().getPath());
              if (isEnforceFileExtensions()) {
                  if (!filename.endsWith(".png")) {
                      filename = filename + ".png";
                  }
              }
              ChartUtilities.saveChartAsPNG(
                  new File(filename), getChart(), getWidth(), getHeight()
              );
          }
        }

        private class MyListener extends MouseInputAdapter {
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                currentRect = new Rectangle(x, y, 0, 0);
                updateDrawableRect(getWidth(), getHeight());
                repaint();
            }
    
            public void mouseDragged(MouseEvent e) {
                updateSize(e);
            }
    
            public void mouseReleased(MouseEvent e) {
                controller.setDraggedRectangle(rectToDraw);
                updateSize(e);
            }
    
            /* 
             * Update the size of the current rectangle
             * and call repaint.  Because currentRect
             * always has the same origin, translate it
             * if the width or height is negative.
             * 
             * For efficiency (though
             * that isn't an issue for this program),
             * specify the painting region using arguments
             * to the repaint() call.
             * 
             */
            void updateSize(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (currentRect == null) 
                  return;
                currentRect.setSize(x - currentRect.x,
                                    y - currentRect.y);
                updateDrawableRect(getWidth(), getHeight());
                Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
                repaint(totalRepaint.x, totalRepaint.y,
                        totalRepaint.width, totalRepaint.height);
            }
        }
        public void clearSelection() {
            currentRect = null;
            repaint();
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g); //paints the background and image
    
            //If currentRect exists, paint a box on top.
            if (currentRect != null) {
                //Draw a rectangle on top of the image.
                g.setXORMode(Color.white); //Color of line varies
                                           //depending on image colors
                g.drawRect(rectToDraw.x, rectToDraw.y, 
                           rectToDraw.width - 1, rectToDraw.height - 1);
    
                //controller.setDraggedRectangle(rectToDraw);
            }
        }
    
        private void updateDrawableRect(int compWidth, int compHeight) {
            int x = currentRect.x;
            int y = currentRect.y;
            int width = currentRect.width;
            int height = currentRect.height;
    
            //Make the width and height positive, if necessary.
            if (width < 0) {
                width = 0 - width;
                x = x - width + 1; 
                if (x < 0) {
                    width += x; 
                    x = 0;
                }
            }
            if (height < 0) {
                height = 0 - height;
                y = y - height + 1; 
                if (y < 0) {
                    height += y; 
                    y = 0;
                }
            }
    
            //The rectangle shouldn't extend past the drawing area.
            if ((x + width) > compWidth) {
                width = compWidth - x;
            }
            if ((y + height) > compHeight) {
                height = compHeight - y;
            }
          
            //Update rectToDraw after saving old value.
            if (rectToDraw != null) {
                previousRectDrawn.setBounds(
                            rectToDraw.x, rectToDraw.y, 
                            rectToDraw.width, rectToDraw.height);
                rectToDraw.setBounds(x, y, width, height);
            } else {
                rectToDraw = new Rectangle(x, y, width, height);
            }
        }
    }
