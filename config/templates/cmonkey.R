## Get the default params and then override some of them here...
initializeParams <- function() {

  source( "R_scripts/initializeDefaultParams.R" )
  params <- initializeDefaultParams()

  params$organism <- "{{organism_name}}"
  params$species <- "{{organism_species}}"
  params$is.eukaryotic <- {{is_eukaryotic}} #TRUE


####################### DATA FILES ###############################

  params$output.dir <- paste( "{{output_dir}}",sep="" )
  params$data.dir <- paste( "{{data_dir}}", sep="" )
  params$log.dir <- paste( "{{log_dir}}", sep="" )


  ##  REQUIRED FILES
  ## file w/expression data.  NOTE:  MUST BE GZIP'D
  params$marray.file <- paste( params$data.dir, "/{{ratio_file}}", sep="" )

  ## file with upstream sequence data - can be retrieved from RSAT (rsa-tools):
  ##         http://rsat.ulb.ac.be/rsat/ - select "retrieve sequence" from menu on left
  ##   NOTE:  MUST select "full identifier" for label when retrieving sequence
  params$seqs.fname <- paste( params$data.dir, "/{{upstream_data}}", sep="" )

  ##  gene coordinates file(s) - can be retrieved from NCBI:
  ##         ftp://ftp.ncbi.nih.gov/genomes/
  ##        (ftp://ftp.ncbi.nih.gov/genomes/Bacteria/ for microbial genomes)
  params$gene.coords.file <- c( {{gene_coord_files}} )

  ## OPTIONAL files
  ##  metabolic interactions from kegg (user provided) - sif format
  params$met.net.file <- paste( params$data.dir, "/{{metabolic_file}}", sep="" )

  ##  OPTIONAL network files
  ##  prolinks files - can be found from http://mysql5.mbi.ucla.edu/public/Genomes/
  params$prolinks.file <- paste( params$data.dir, "/{{prolinks_file}}", sep="" )

  params$predictome.code <- "{{organism_code}}" ## Code for species



####################### INITIALIZATION/SEEDING ###########################

  ## number of iterations to optimize clusters
  params$n.iters <- {{n_iters}} #120 #

  ## Max # of clusters
  params$kmax <- {{max_clusters}} #300

  ## initial size of clusters (when seeded)
  params$semirnd.clust.size <- {{init_clust_size}}


  ## Mean expected cluster rows
  params$expected.cluster.rows <- {{expected_clust_rows}}
  params$mean.clusters.per.gene <- {{mean_clust_rows}}

  params$operon.trim.thresh <- {{operon_trim_thresh}}
  params$operon.trim.distance <- {{operon_trim_distance}}

  params$ratios.thresh <- {{ratios_thresh}}
  params$ratios.n.thresh <- {{ratios_n_thresh}}
  params$lambdas.thresh <- {{lambdas_thresh}}
  params$lambdas.n.thresh <- {{lambdas_n_thresh}}



####################### MOTIF FINDING #############################

####################### NETWORKS ##################################

  ##  maximum prior weight for each network type
  params$net.max.weights <- unlist( list( operons={{operons_weight}}, # 2         # predicted operon links
                                          met={{metabolic_weight}}, # 0.6           # metabolic links
                                          COG.code={{cog_weight}},            # COG.code "links"
                                          cond.sims={{sim_cond_weight}},         # network of similar conditions - keep set to 0
                                          prolinks.RS={{prolinks_rs_weight}},# 0.4    # network of prolinks RS edge type
                                          prolinks.PP={{prolinks_pp_weight}},# 0.2    # network of prolinks PP edge type
                                          prolinks.GC={{prolinks_gc_weight}},# 0.6    # network of prolinks GC edge type
                                          prolinks.GN={{prolinks_gn_weight}},# 0.4    # network of prolinks GN edge type
                                          predictome.CP={{predictome_cp_weight}},       # network of predictome CP edge type
                                          predictome.GF={{predictome_gf_weight}},       # network of predictome GF edge type
                                          predictome.PP={{predictome_pp_weight}}        # network of predictome PP edge type
                                         )
                                   )


####################### SMD DATA FILES ############################

  params$data.col <- c( "LOG_RAT2N_MEAN", "Log.base2..of.R.G.Normalized.Ratio..Mean." )
  params$gene.col <- c( "TIGRID", "NAME", "Name" )
  params$desc.name <- "DESCRIPTION" # Not used yet
  params$short.name <- "GENE.NAME" # Not used yet

  params$valid.gene.regexp <- "{{valid_gene_regex}}" # Use this to rule out bad spots in SMD data
  #params$invalid.condition.regexp <- c( "Category=RNA decay", "Category=DNA metabolism", "Category=Amino Acid metabolism" )



  invisible( params )
}
