from java.lang import System, Runtime
uri = 'http://gaggle.systemsbiology.net/2005-11/echo/echo.jnlp'
path = '%s/bin/%s' % (System.getProperty ('java.home'), 'javaws')
rt = Runtime.getRuntime ()
libPath = 'java.library.path=%s' % System.getProperty ('java.library.path')
print libPath
rt.exec ([path, uri], [libPath])
