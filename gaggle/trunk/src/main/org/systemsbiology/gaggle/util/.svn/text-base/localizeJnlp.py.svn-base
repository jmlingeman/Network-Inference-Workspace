import sys
from os import getcwd

if (not len (sys.argv) == 2):
  sys.stderr.write ('usage: python prepareJnlp.py <jnlpFileName>\n')
  sys.exit (1)

rawFileName = sys.argv [1]

text = open(rawFileName).read()
codebase = 'file://%s' % getcwd ()
newText = text.replace ('$CODEBASE$', codebase)
print newText



