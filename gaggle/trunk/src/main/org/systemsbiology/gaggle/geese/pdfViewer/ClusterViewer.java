// ClusterViewer.java
//------------------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.pdfViewer;
//------------------------------------------------------------------------------------------------------

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.jpedal.PdfDecoder;

import java.rmi.*;

import org.systemsbiology.gaggle.core.Goose;

import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;

//------------------------------------------------------------------------------------------------------
public class ClusterViewer implements Goose {

    protected JFrame mainFrame;
    protected JTabbedPane tabbedPane;
    JMenuBar menubar;
    private String viewerTitle = "Jpanel Demo";
    private String currentFile = null;
    private final JLabel pageCounter1 = new JLabel("Page ");
    private JTextField pageCounter2 = new JTextField(4);//000 used to set prefered size
    private JLabel pageCounter3 = new JLabel("of");//000 used to set prefered size

    String[] currentSelection = new String[0];
    String myGaggleName = "ClusterInfo";
    Boss gaggleBoss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);

    String baseUrl;
    String imageFileNameBase = "cluster_";
    String imageFileNameSuffix = ".pdf";
    static final float ZOOM_INCREMENT = 1.1f;
    static final int PAGE_ROTATION = 90;

    //------------------------------------------------------------------------------------------------------
    public ClusterViewer(String baseUrl) {
        createGui();
        new GooseShutdownHook(connector);
        this.baseUrl = baseUrl;
        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("Browser failed to export remote object: " + ex0.getMessage());
        }

        // initializeViewer ();

    } // ctor

    //------------------------------------------------------------------------------------------------------
    protected void createGui() {
        mainFrame = new JFrame(myGaggleName);
        MiscUtil.setApplicationIcon(mainFrame);

        mainFrame.setJMenuBar(createMenuBar());
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel outerPanel = new JPanel();
        mainFrame.getContentPane().add(outerPanel);
        outerPanel.setLayout(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton bossButton = new JButton("Boss");
        bossButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    gaggleBoss.show("boss");
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
        });

        toolbar.add(bossButton);
        outerPanel.add(toolbar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new MyTabbedPaneUI());

        outerPanel.add(tabbedPane, BorderLayout.CENTER);

        mainFrame.pack();
        mainFrame.setSize(800, 800);
        mainFrame.setVisible(true);
        MiscUtil.placeInCenter(mainFrame);

    } // createGui

    //-------------------------------------------------------------------------------------
    protected JMenuBar createMenuBar() {
        menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("Application");
        menubar.add(fileMenu);

        JMenuItem m2 = new JMenuItem("Quit");

        fileMenu.add(m2);

        m2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doExit();
            }
        });

        return menubar;

    } // createMenuBar

    //-----------------------------------------------------------------------------------------------------
    class MyTabbedPaneUI extends BasicTabbedPaneUI {
        public MyTabbedPaneUI() {
            super();
        }

        protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
                                int tabIndex, Rectangle iconRect, Rectangle textRect) {

            super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
            Rectangle rect = rects[tabIndex];
            g.setColor(Color.black);
            g.drawRect(rect.x + 5, rect.y + 5, 10, 10);
            g.drawLine(rect.x + 5, rect.y + 5, rect.x + 15, rect.y + 15);
            g.drawLine(rect.x + 15, rect.y + 5, rect.x + 5, rect.y + 15);
        }

        protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
            return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 20;
        }

        protected MouseListener createMouseListener() {
            return new MyMouseHandler();
        }

        class MyMouseHandler extends MouseHandler {
            public MyMouseHandler() {
                super();
            }

            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int currentTabIndex = -1;
                int tabCount = tabPane.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    if (rects[i].contains(x, y)) {
                        currentTabIndex = i;
                        break;
                    } // if contains
                } // for i
                if (currentTabIndex >= 0) {
                    Rectangle tabRect = rects[currentTabIndex];
                    x = x - tabRect.x;
                    y = y - tabRect.y;
                    if ((x >= 5) && (x <= 15) && (y >= 5) && (y <= 15)) {
                        try {
                            tabbedPane.remove(currentTabIndex);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } // if
                } // if currentTabIndex >= 0
                System.gc();
            } // mouseClicked
        } // inner-inner class MyMouseHandler

    } // inner class MyTabbedPaneUi

    //------------------------------------------------------------------------------
    public void addNewTab(String clusterIdAs3DigitString) {
        tabbedPane.add(createScrolledPdfViewer(clusterIdAs3DigitString), clusterIdAs3DigitString);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    //-------------------------------------------------------------------------------------
    protected JPanel createScrolledPdfViewer(String clusterIdAs3DigitString) {
        final PdfDecoder pdfDecoder = new PdfDecoder();
        String urlString = baseUrl + "/" + imageFileNameBase + clusterIdAs3DigitString +
                imageFileNameSuffix;
        currentFile = urlString;

        System.out.println("url: " + urlString);
        try {
            pdfDecoder.openPdfFileFromURL(urlString);
            // System.out.println ("page count: " + pdfDecoder.getPageCount ());
            pdfDecoder.decodePage(1);
            pdfDecoder.setPageParameters(1.0f, 1, PAGE_ROTATION); //values scaling (1=100%). page number
            pdfDecoder.invalidate();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        final ViewerState viewerState = new ViewerState(pdfDecoder.getPageCount());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        panel.add(toolbar, BorderLayout.NORTH);


        JButton backButton = new JButton("<");
        backButton.setToolTipText("Back One page");
        toolbar.add(backButton);
        backButton.addActionListener(new BackButtonListener(pdfDecoder, viewerState));


        JButton forwardButton = new JButton(">");
        forwardButton.setToolTipText("Forward One page");
        toolbar.add(forwardButton);
        forwardButton.addActionListener(new ForwardButtonListener(pdfDecoder, viewerState));

        JButton zoomOutButton = new JButton("-");
        zoomOutButton.setToolTipText("Zoom Out");
        toolbar.add(zoomOutButton);
        zoomOutButton.addActionListener(new ZoomOutButtonListener(pdfDecoder, viewerState));

        JButton zoomInButton = new JButton("+");
        zoomInButton.setToolTipText("Zoom In");
        toolbar.add(zoomInButton);
        zoomInButton.addActionListener(new ZoomInButtonListener(pdfDecoder, viewerState));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        scrollPane.setViewportView(pdfDecoder);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;

    } // createScrolledPdfViewer

    //------------------------------------------------------------------------------------------------------
    class ViewerState {
        int pageCount;
        int currentPage = 1;
        float zoomLevel = 1.0f;

        public ViewerState(int pageCount) {
            this.pageCount = pageCount;
        }

        public void setPage(int newValue) {
            currentPage = newValue;
        }

        public int getPage() {
            return currentPage;
        }

        public float getZoom() {
            return zoomLevel;
        }

        public void storeZoom(float newValue) {
            zoomLevel = newValue;
        }
    }

    //------------------------------------------------------------------------------------------------------
    class ZoomInButtonListener implements ActionListener {

        PdfDecoder pdfDecoder;
        ViewerState viewerState;

        ZoomInButtonListener(PdfDecoder pdfDecoder, ViewerState viewerState) {
            this.pdfDecoder = pdfDecoder;
            this.viewerState = viewerState;
        }

        public void actionPerformed(ActionEvent e) {
            float newZoom = viewerState.getZoom() * ZOOM_INCREMENT;
            viewerState.storeZoom(newZoom);
            // System.out.println ("setting zoom at " + newZoom);
            pdfDecoder.setPageParameters(newZoom, viewerState.getPage(), PAGE_ROTATION);
            pdfDecoder.invalidate();
            mainFrame.repaint();
        }

    } // ZoomInButtonListener

    //------------------------------------------------------------------------------------------------------
    class ZoomOutButtonListener implements ActionListener {

        PdfDecoder pdfDecoder;
        ViewerState viewerState;

        ZoomOutButtonListener(PdfDecoder pdfDecoder, ViewerState viewerState) {
            this.pdfDecoder = pdfDecoder;
            this.viewerState = viewerState;
        }

        public void actionPerformed(ActionEvent e) {
            float newZoom = viewerState.getZoom() * (1.0f / ZOOM_INCREMENT);
            viewerState.storeZoom(newZoom);
            pdfDecoder.setPageParameters(newZoom, viewerState.getPage(), PAGE_ROTATION);
            pdfDecoder.invalidate();
            mainFrame.repaint();
        }

    } // ZoomOutButtonListener

    //------------------------------------------------------------------------------------------------------
    class BackButtonListener implements ActionListener {

        PdfDecoder pdfDecoder;
        ViewerState viewerState;

        BackButtonListener(PdfDecoder pdfDecoder, ViewerState viewerState) {
            this.pdfDecoder = pdfDecoder;
            this.viewerState = viewerState;
        }

        public void actionPerformed(ActionEvent e) {
            int currentPage = viewerState.getPage();
            if (currentPage > 1) {
                currentPage -= 1;
                viewerState.setPage(currentPage);
                try {
                    pdfDecoder.decodePage(currentPage);
                    pdfDecoder.invalidate();
                    mainFrame.repaint();
                }
                catch (Exception e1) {
                    System.err.println("back 1 page");
                    e1.printStackTrace();
                }
            }
        }

    } // inner class BackButtonListener

    //------------------------------------------------------------------------------------------------------
    class ForwardButtonListener implements ActionListener {

        PdfDecoder pdfDecoder;
        ViewerState viewerState;

        ForwardButtonListener(PdfDecoder pdfDecoder, ViewerState viewerState) {
            this.pdfDecoder = pdfDecoder;
            this.viewerState = viewerState;
        }

        public void actionPerformed(ActionEvent e) {
            int currentPage = viewerState.getPage();
            if (currentPage < pdfDecoder.getPageCount()) {
                currentPage += 1;
                viewerState.setPage(currentPage);
                try {
                    pdfDecoder.decodePage(currentPage);
                    pdfDecoder.invalidate();
                    mainFrame.repaint();
                }
                catch (Exception e1) {
                    System.err.println("forward 1 page");
                    e1.printStackTrace();
                }
            }
        }

    } // inner class ForwardButtonListener

    //------------------------------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("failed to connect to gaggle: "   + ex0.getMessage());
            ex0.printStackTrace();
        }
        gaggleBoss = connector.getBoss();
    } // connectToGaggle

    //------------------------------------------------------------------------------------------------------
    public void handleNameList(String source, Namelist nameList) {
        System.out.println("pdf viewer rec'd " + nameList.getNames().length + " names");
        for (int i = 0; i < nameList.getNames().length; i++)
            System.out.println("   " + nameList.getNames()[i]);


        int possibleClusterIdNumber = -1;

        for (int i = 0; i < nameList.getNames().length; i++) {
            try {
                possibleClusterIdNumber = Integer.parseInt(nameList.getNames()[i]);
            }
            catch (NumberFormatException ignore) {
                continue;
            }
            if (possibleClusterIdNumber > 0) {
                // System.out.println  ("want to display cluster number " + possibleClusterIdNumber);
                String fixedName = nameList.getNames()[i];
                if (possibleClusterIdNumber < 10)
                    fixedName = "00" + nameList.getNames()[i];
                else if (possibleClusterIdNumber < 100)
                    fixedName = "0" + nameList.getNames()[i];
                addNewTab(fixedName);
            } // if
        } // for i

    } // handleNameList

    //-------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) {
        handleNameList(myGaggleName, new Namelist(cluster.getSpecies(), cluster.getRowNames()));
    }

    //-------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
        // handleNameList  (matrix.getSpecies (), matrix.getRowTitles  ());
    }

    //-------------------------------------------------------------------------------------
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
    }

    public void handleNetwork(String source, Network network) {
    }

    //-------------------------------------------------------------------------------------
    public String getName() {
        return myGaggleName;
    }

    //-------------------------------------------------------------------------------------
    public void setName(String newName) {
        myGaggleName = newName;
        mainFrame.setTitle(myGaggleName);
    }

    //-------------------------------------------------------------------------------------
    public void doHide() {
        mainFrame.setVisible(false);
    }

    public void doShow() {
        mainFrame.setVisible(true);
    }

    public void clearSelections() {
    }

    public void setGeometry(int x, int y, int width, int height) {
    }

    public int getSelectionCount() {
        return currentSelection.length;
    }

    public void doBroadcastList() {
        ;
    }

    //-------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        System.exit(0);
    }

    public void update(String[] gooseNames) {
    }

    //-------------------------------------------------------------------------------------
    public static void main(String[] args) {
        ClusterViewer view;
        if (args.length != 1) {
            System.err.println("usage: clusterViewer <baseUrl>");
            System.exit(1);
        }

        view = new ClusterViewer(args[0].trim());

    } // main
//------------------------------------------------------------------------------------------------------
}
