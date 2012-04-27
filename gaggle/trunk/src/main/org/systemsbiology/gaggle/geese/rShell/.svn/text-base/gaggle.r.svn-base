# gaggle.r
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
broadcast <- function (x, name=Null)
{
  if (is.matrix (x)) {
    .jcall (goose, "V", "createAndBroadcastMatrix", rownames (x), colnames (x),
                          as.numeric (as.vector (x)))
    }
  else if (is.vector (x)) {
    .jcall (goose, "V", "broadcastList", x)
    }
  else {
    cat ("no support yet for broadcasting variables of type ", typeof (x), "\n")
    }
  invisible (NULL)
  
}
#--------------------------------------------------------------------------------------------------
show <- function (name)
{
  .jcall (goose, "V", "show", name);
  invisible (NULL)
  
}
#--------------------------------------------------------------------------------------------------
