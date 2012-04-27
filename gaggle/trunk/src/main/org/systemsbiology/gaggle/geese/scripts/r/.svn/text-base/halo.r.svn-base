function (m)
{
  result <- m
  for (r in 1:dim (m)[1]) {
    #cat ("row ", r, " has length ", length (m [r,]), "\n")
    avg <- mean (m [r,], na.rm=T)
    variance <- var (m [r,], na.rm=T)
    result [r,] <- result [r,] - avg
    #result [r,] <- result [r,] / variance
    result [r,] <- result [r,] / sqrt (variance)
    }

  result

}

