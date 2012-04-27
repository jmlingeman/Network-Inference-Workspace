import urllib
#---------------------------------------------------------------------------------------------------
def getPage (url):

  remoteFile = urllib.urlopen (url)
  text = remoteFile.read ()
  remoteFile.close ()

  return text

#---------------------------------------------------------------------------------------------------
url = 'https://db.systemsbiology.net/sbeams/cgi/ProteinStructure/GetAnnotations?search_scope=All&search_key=iron&action=GO&biosequence_set_id=2&SBEAMSentrycode=DF45jasj23jhapply_action=QUERY'

result = getPage (url)
print result