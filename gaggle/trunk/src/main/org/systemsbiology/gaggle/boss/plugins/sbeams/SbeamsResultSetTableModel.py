from java.io import *
from javax.swing import *
from java.awt import *
from java.awt.event import *
import java
from javax.swing.table import *
from javax.swing.event import *
from java.util import *

class SbeamsResultSetTableModel (AbstractTableModel):

   #---------------------------------------------------------
   def __init__ (self):
     self.columnNames = ['ORF', 'Gene', 'EC Numbers', 'Gene Name/Function']
     self.data = []

   #---------------------------------------------------------
   def clearData (self):
     self.data = []

   #---------------------------------------------------------
   def addSearchResult (self, orf, geneSymbol, ecNumbers, geneFunction ):

     self.data.append ([orf, geneSymbol, ecNumbers, geneFunction])

   #---------------------------------------------------------
   def setData (self, newValue):
     self.data = newValue;

   #---------------------------------------------------------
   def getData (self):
     return self.data; 

   #---------------------------------------------------------
   def getColumnCount (self):
     return len (self.columnNames)

   #---------------------------------------------------------
   def  getRowCount (self):
     return len (self.data)

   #---------------------------------------------------------
   def getColumnName (self, col):
     return self.columnNames[col]

   #---------------------------------------------------------
   def getValueAt (self, row, col):
       return self.data [row][col]

   #---------------------------------------------------------
   def setValueAt (self, value, row, col):

     self.data [row][col] = value

   #---------------------------------------------------------
   def isCellEditable (self, row, col):

     return 0

   #---------------------------------------------------------
   #def getColumnClass (self, c):
   #   return java.lang.String

#------------------------------------------------------------------------
