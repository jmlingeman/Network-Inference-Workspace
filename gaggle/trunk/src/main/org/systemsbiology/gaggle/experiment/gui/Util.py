#class Util:
#-------------------------------------------------------------------------------
def isArrayOfNumbers (s):
 
  for element in s:
    if (not isNumber (element)):
      return 0

  return 1

#-------------------------------------------------------------------------------
def isNumber (s):
 

  minusCount = s.count ('-')
  #print 'minusCount: %d' % minusCount
  if (minusCount > 1):
    return 0

  if (minusCount == 1):
    if (s.find ('-') > 0):
       return 0
    s = s.replace ('-', '')

  tokens = s.split ('.')
  #print 'tokens: %s' % tokens

  if (len (tokens) > 2):
    return 0

  for token in tokens:
    for char in token:
      if (not char.isdigit ()):
        return 0

  return 1
#-------------------------------------------------------------------------------
