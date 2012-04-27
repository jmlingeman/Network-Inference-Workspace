from java.util import *
from java.lang import *
import java

import java.awt.Color
import java.text.SimpleDateFormat
from javax.swing import *
from java.awt import *

from org.jfree.chart import *
from org.jfree.chart.plot import *
from org.jfree.chart.renderer.xy import *
from org.jfree.chart.axis import *

from org.jfree.data import *
from org.jfree.data.xy import *

import org.jfree.ui.RectangleInsets
import org.jfree.ui.RefineryUtilities


#------------------------------------------------------------------------------------------
def createDataSet ():

   series = XYSeries (java.lang.Double (0.0))
   series.add (Double (0.0), Double (0.0))
   series.add (Double (1.0), Double (1.0))
   series.add (Double (2.0), Double.NaN)
   series.add (Double (3.0), Double (9.0))
   series.add (Double (4.0), Double (16.0))

   ds = XYSeriesCollection (series)
   return ds
   
#------------------------------------------------------------------------------------------
def createChart (dataset):

  renderer = DefaultXYItemRenderer ()
  domain = NumberAxis ("x")
  range  = NumberAxis ("y")
  xyPlot = XYPlot (dataset, domain, range, renderer)
  chart = JFreeChart ("SQUARES", JFreeChart.DEFAULT_TITLE_FONT, xyPlot, 1)
  return chart

#------------------------------------------------------------------------------------------
def createChartPanel (chart):

  chartPanel = ChartPanel (chart)
  chartPanel.setSize (300, 300)
  chartPanel.setPreferredSize (Dimension (300, 300))
  return chartPanel
#------------------------------------------------------------------------------------------
def exit (event):

  java.lang.System.exit (0)

#------------------------------------------------------------------------------------------
f = JFrame ('JFreeChart TimeSeries demo', size=Dimension(600,600))
p = JPanel ()
p.setBackground (Color.RED);
p.setLayout (BorderLayout ())
exitButton = JButton ('Exit', actionPerformed = exit)
buttonPanel = JPanel ()
buttonPanel.add (exitButton)
p.add (buttonPanel, BorderLayout.SOUTH);
p.add (createChartPanel (createChart (createDataSet ())))
f.getContentPane().add (p)
f.pack ()
f.show ()
