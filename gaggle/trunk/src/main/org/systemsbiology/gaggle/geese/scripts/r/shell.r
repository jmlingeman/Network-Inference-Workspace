# shell-init.r:  gaggle-related functions specific to 'shell R' -- that is, any instance of
# R except the 'Java Gui for R' (JGR, or jaguar), whose specific gaggle-related functions
# will be found in jgr-init.r
#--------------------------------------------------------------------------------------------------
library (rJava)
cat ("calling .jinit (gaggleRShell.jar)\n")
.jinit ("./gaggleRShell.jar")
cat ('  os version: ', .jcall ("java/lang/System", "S", "getProperty", "os.name"),'\n')
cat (' jvm version: ', .jcall ("java/lang/System", "S", "getProperty", "java.version"), '\n')
goose <- .jnew ("org/systemsbiology/gaggle/geese/rShell/RShellGoose")
print (.jcall (goose, "S", "getName"))
url = 'http://gaggle.systemsbiology.net/current/r/utilities.r'
cat (  'about to source ', url, '\n')
source (url)
#--------------------------------------------------------------------------------------------------
scriptVersion <- function ()
{
  return ("shell-init.r $Revision: 345 $   $Date: 2005-10-10 17:28:01 -0400 (Mon, 10 Oct 2005) $");
}
#--------------------------------------------------------------------------------------------------
getNameList <- function ()
{
  nameList <- .jcall (goose, "[S", "getNameList")
  return (nameList)

}
#--------------------------------------------------------------------------------------------------
getMatrix <- function ()
{
  rowCount <- .jcall (goose, "I", "getMatrixRowCount")
  columnCount <- .jcall (goose, "I", "getMatrixColumnCount")
  matrixRowNames <- .jcall (goose, "[S", "getMatrixRowNames")
  matrixColumnNames <- .jcall (goose, "[S", "getMatrixColumnNames")
  #cat ("rows: ", rowCount, "\n");
  #cat ("cols: ", columnCount, "\n");
  #cat ("row names ", matrixRowNames, "\n");
  #cat ("col names ", matrixColumnNames, "\n");

  data <- .jcall (goose, "[D", "getAllData");
  
  m <- matrix (data, nrow=rowCount, ncol=columnCount, byrow=T,
                dimnames = list (matrixRowNames, matrixColumnNames))
  
  return (m)

}
#--------------------------------------------------------------------------------------------------
broadcast <- function (x, name='')
{
  if (is.matrix (x)) {
    cat ('broadcasting matrix, name: ', name, '\n')
    if (name == '')
      name = 'from R'
       # java stores matrices in row-major order, R uses column-major, so be sure to
       # transpose the actual data before sending it to java
    .jcall (goose, "V", "createAndBroadcastMatrix", rownames (x), colnames (x),
                          as.numeric (as.vector (t(x))), name)
    }
  else if (is.vector (x)) {
    if (length (x) == 1)  # 
      x <- as.vector (c (x, x))
    .jcall (goose, "V", "broadcastList", x)
    }
  else {
    cat ("no support yet for broadcasting variables of type ", typeof (x), "\n")
    }
  invisible (NULL)
  
}
#--------------------------------------------------------------------------------------------------
gaggleShow <- function (name)
{
  .jcall (goose, "V", "show", name);
  invisible (NULL)
  
}
#--------------------------------------------------------------------------------------------------
