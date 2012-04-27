##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
## May 2010 Dream3/4 pipeline (MCZ,tlCLR,Inferelator)
## Bonneau lab - "Aviv Madar" <am2654@nyu.edu>, 
##  		     "Alex Greenfield" <ag1868@nyu.edu> 
## NYU - Center for Genomics and Systems Biology
##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
Please join the inferelator google group at http://groups.google.com/group/inferelator-announcements?lnk=srg&hl=en&ie=UTF-8&oe=utf-8&pli=1

1. sign up for our google inferelator group to post questions in the discussion board and read useful information. 
2. extract the dream4_pipeline.zip.
3. install the mi library from source. 
You need to be in the directory containing the package to be installed (if you're installing
from a zip-file) or the parent directory of a package (the one above the package itself). From here, run 'R CMD INSTALL pkg'
where 'pkg' is the name of the package. It is important to note the capitalized INSTALL, as 'install' will not work on 
Linux machines. 
For Windows machines, a binary of the package is required to install it (use unix emulator). 
4. if you want to run the script using multiple processors install package multicore (available from CRAN).
5. open r_scripts and review main.R (also review init.R).  After you are happy with the parameters set in init.R (e.g. number of prcessors to use, tau, max delta t, etc.)
run main.R line by line.  running init.R will prompt you to choose a dataset, design and response.  Consult our DREAM3 paper to understand what 
the response and design matrix mean.  DREAM4 paper will be out soon in plosOne as part of the DREAM4 best performers collection.

dependencies (R packages that must be installed prior to running the dream4-pipeline):
elasticnet
MI (see above)
multicore (if you want to run on multiple processors)


To get you started here are our suggested parameters for each run (set these params inside init.R):
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
	for a typical DREAM4-100 gene run set:
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
PARAMS[["lars"]][["max_single_preds"]] = 15
PARAMS[["general"]][["delT_max"]] = 110
PARAMS[["general"]][["tau"]] = 50
From interactive menu (after running source("r_scripts/main.R") ):
choose 1 of 5 possible 100 gene networks.
choose 5 for response CLR and LARS
choose 2 for design matrix CLR and LARS
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
	for a typical DREAM4-10 gene run set:
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
PARAMS[["lars"]][["max_single_preds"]] = 10
PARAMS[["general"]][["delT_max"]] = 110
PARAMS[["general"]][["tau"]] = 50
From interactive menu (after running source("r_scripts/main.R") ):
choose 1 of 5 possible 100 gene networks.
choose 5 for response CLR and LARS
choose 2 for design matrix CLR and LARS
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
	for a typical DREAM3-100 gene run set:
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
PARAMS[["lars"]][["max_single_preds"]] = 20
PARAMS[["general"]][["delT_max"]] = 45
PARAMS[["general"]][["tau"]] = 15
From interactive menu (after running source("r_scripts/main.R") ):
choose 1 of 5 possible 100 gene networks.
choose 4 for response CLR and LARS
choose 2 for design matrix CLR and LARS
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
	for a typical DREAM3-50 gene run set:
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
PARAMS[["lars"]][["max_single_preds"]] = 15
PARAMS[["general"]][["delT_max"]] = 45
PARAMS[["general"]][["tau"]] = 15
From interactive menu (after running source("r_scripts/main.R") ):
choose 1 of 5 possible 100 gene networks.
choose 4 for response CLR and LARS
choose 2 for design matrix CLR and LARS
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
	for a typical DREAM3-10 gene run set:
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
PARAMS[["lars"]][["max_single_preds"]] = 10
PARAMS[["general"]][["delT_max"]] = 45
PARAMS[["general"]][["tau"]] = 15
From interactive menu (after running source("r_scripts/main.R") ):
choose 1 of 5 possible 100 gene networks.
choose 4 for response CLR and LARS
choose 2 for design matrix CLR and LARS
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
	for a typical DREAM2-50 gene run set:
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
PARAMS[["lars"]][["max_single_preds"]] = 15
PARAMS[["general"]][["delT_max"]] = 25
PARAMS[["general"]][["tau"]] = 10
From interactive menu (after running source("r_scripts/main.R") ):
choose 1 of 5 possible 100 gene networks.
choose 4 for response CLR and LARS
choose 2 for design matrix CLR and LARS


