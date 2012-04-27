# MetaDataXmlParser
#---------------------------------------------------------------------------------
# RCSid = '$Version: $   $Date: 2004/10/25 23:22:52 $'
#---------------------------------------------------------------------------------
import org.jdom as jdom
import java.io as io
from Variable import *
from Condition import *
from MetaData import *
from DataSetDescription import *
import sys

from csplugins.isb.pshannon.dataMatrix.readers import *
#------------------------------------------------------------------------
class MetaDataXmlParser:

  def __init__ (self, filename):

    self.originalFilename = filename

    builder = jdom.input.SAXBuilder ()
    if (filename.find ('http://') == 0):
      doc = builder.build (filename)
      self.documentSource = 'web'
    elif (filename.find ('file://') == 0):
      filename = filename [7:]
      doc = builder.build (io.FileInputStream (filename))
      self.documentSource = 'localFile'
    elif (filename.find ('httpIndirect://') == 0):
      reader = TextHttpIndirectFileReader (filename)
      reader.read ()
      text = reader.getText ()
      filename = 'junk99.xml'
      f = open (filename, 'w')
      f.write (text)
      f.close ()
      doc = builder.build (io.FileInputStream (filename))
      self.documentSource = 'localFile'
    else: 
      raise NameError, 'unrecognized data source protocol in filename: %s' % filename

    self.parseDocument (doc)

  #------------------------------------------------------------------------
  def parseDocument (self, jdomDoc):
      
    root = jdomDoc.rootElement
    name = root.getAttribute ("name").value
    date = root.getAttribute ("date").value
    self.metaData = MetaData (name, date)
 
    self.metaData.setPredicates (self.parsePredicates (root))
    self.metaData.setDataSetDescriptions (self.parseDataSetDescriptions (root))

#    variableDefinitions = self.parseVariableDefinitions (root)
#    self.metaData.setVariableDefinitions (variableDefinitions)

    conditions = self.parseConditions (root)
    for condition in conditions:
      self.metaData.addCondition (condition)
  
  #------------------------------------------------------------------------
  def getDataFileUri (self):

    return self.dataFileUri

  #------------------------------------------------------------------------
  def getMetaData (self):

    return self.metaData

  #------------------------------------------------------------------------
  def parsePredicates (self, root):
  
    result = {}
    children = root.getChildren ('predicate')
    for child in children:
      category = child.getAttribute ('category').value
      value = child.getAttribute ('value').value
      result [category] = value

    return result
  
  #------------------------------------------------------------------------
  def parseDataSetDescriptions (self, root):
  
    result = []
    children = root.getChildren ('dataset')
    for child in children:
      status = child.getAttribute ('status').value
      type = child.getAttribute ('type').value
      uri = child.getChild ('uri').textTrim
      dataSetDescription = DataSetDescription (uri, status, type)
      result.append (dataSetDescription)

    return result
  
  #------------------------------------------------------------------------
  def createFullUrlFromRelativeFilename (self, filename):

    assert (self.originalFilename.count ('http://') == 1)
    tokens = self.originalFilename.split ('/')
    assert (len (tokens) > 3)
    max = len (tokens)
    baseUrl = ''
    for t in range (max - 1):
      baseUrl += '%s/' % tokens [t]
    return '%s%s' % (baseUrl, filename)

  #------------------------------------------------------------------------
  def parseOrganism (self, root):
  
    organismElement = root.getChild ('organism')
    species=organismElement.getChild('species').textTrim
    strain=organismElement.getChild('strain').textTrim
  
    return (species, strain)
  
  #------------------------------------------------------------------------
  def parsePredications (self, root):
    """
     return a hash, keyed on predication name
  
     for instance
  
     <predicate category='species'       value='Halobacterium NRC-1'/>
     <predicate category='strain'        value='afsQ2 knockout'/>
     <predicate category='perturbation'  value='genetic'/>

     becomes one element in a hash:
  
     {'species':       'Halobacterium NRC-1',
      'strain':        'afsQ2 knockout',
      'perturbation':  'genetic'}
    """
  
    predicationElements = root.getChildren ('predicate')
    result = {}
    for p in predicationElements:
      name = p.getAttribute ('category').value
      value = p.getChild ('value')
      result [name] = value
  
    return result
  
  
  #------------------------------------------------------------------------
  def parseVariableDefinitions (self, root):
  
    """
     return a hash keyed by definition name, where each hash value
     is a (nested) hash, containing all element attributes, and
     an array of strings caputring the range of values permitted
     in variables of this type.
  
     for example:
  
       <variableDefinition name='time' units='minutes'>
         <value>0</value>
         <value>30</value>
         <value>60</value>
         <value>90</value>
       </variableDefinition>
  
     becomes 
     {'time': {'values': ['0', '30', '60', '90'], 
               'units': 'minutes', 
               'name': 'time'}}
  
     note that the 'name' attribute is plucked out and used as the
     definition's key, but also appears in the full list.
  
    """
  
    variableDefinitionElements = root.getChildren ('variableDefinition')
    result = {}
    for definition in variableDefinitionElements:
      defHash = {} # eg, {'name':'time', 'units'='minutes', 'values'=[0,30,60,90]
      variableDefitionName = None
      for attribute in definition.getAttributes ():
        name = attribute.name
        value = attribute.value
        defHash [name] = value
      valueElements = definition.getChildren ('value')
      values = []
      for v in valueElements:
        values.append (v.textTrim)
      assert (len (values) > 0)
      defHash ['values'] = values
      assert (defHash.has_key ('name'))
      defName = defHash ['name']
      result [defName] = defHash
  
    #print 'p: %s' % result
    return result
  
  
  #------------------------------------------------------------------------
  def parseConditions (self, root):
    """
     return an array of Condition objects
     for example, from
  
       <condition alias='C30'>
         <variable name='gamma irradiation' value='false'/>
         <variable name='time' value='30' units='minutes'/>
       </condition>
    
       <condition alias='G0'>
         <variable name='gamma irradiation' value='true'/>
         <variable name='time' value='0' units='minutes'/>
       </condition>
  
     string representation of the two resulting condition objects:
       [note that no units ('None') are specified for irradiation]
    
       condition: C30, gamma irradiation:  false, None,  time:  30, minutes
       condition: G0   gamma irradiation:  true,  None   time:   0, minutes

  
    """
  
    conditionElements = root.getChildren ('condition')
    result = [] # use an array so that conditions can be stored in the
                # order in which they appear in the file, which may
                # reflect an order the user carefully created
    for c in conditionElements:
      alias = c.getAttribute ('alias').value
      assert (alias)
      condition = Condition (alias)
      for v in c.getChildren ('variable'):
        name = v.getAttribute ('name').value
        value = v.getAttribute ('value').value
        unitsAttribute = v.getAttribute ('units')
        units = None
        if (unitsAttribute):
          units = unitsAttribute.value
        condition.addVariable (Variable (name, value, units))

      result.append (condition)
  
    return result

#------------------------------------------------------------------------
