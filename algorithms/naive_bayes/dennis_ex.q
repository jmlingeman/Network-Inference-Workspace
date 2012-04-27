/ NAIVE BAYES

/ Naive Bayes is a classifier scheme that is an alternative
/ http://en.wikipedia.org/wiki/Naive_Bayes_classifier
/ to decision trees where the input is continuous (but the output
/ is not).
/ For example, given temperatures and the like, try to figure out
/ whether someone is sick.
/ The basic idea is, given innames (e.g. height, weight, footsize)
/ inlists (e.g. lists of values of height, weight and footsize)
/ and outlist (e.g. gender)
/ construct a model such that if you know the height, weight and footsize
/ of a new individual, you can find that person's gender.
/ The idea is to find the prediction based on bayes' theorem with
/ the strong independence assumption that the independent variables
/ (e.g. height, weight, and footsize) are mutually independent.
/ This is manifestly wrong in many cases (as in this example), but
/ apparently works surprisingly well.

/ Steps: find the a priori probability of each class of outlist (based
/ on its initial proportion of the population in the training set) unless
/ you know better.

/ For each inlist, form a function that can give the probability
/ density for a given specific value of the
/ outlist, e.g. find the probability that the given height is the height
/ of a man.
/ probdensity[givenval, listofvals]. listofvals will be values for a given
/ outlist value (e.g. males).
/ So now the prediction goes like this: given a set of values
/ for a new instance, predict the member x of outlist based on
/ the product P(x) * probdensity[heightval of instance; list of height vals
/ associated with x] * probdensity[someotherval of instance; list of
/ someothervals associated with x]
/ Then we compare the results we calculate for each x and find the max.

phi: ((-3.0; 0.0013);(-2.5;0.0062);(-2.3; 0.011); (-2.0; 0.023); (-1.8; 0.036) )
phi,: ((-1.5; 0.067); (-1.3; 0.097); (-1.0; 0.16); (-0.8;0.21); (-0.5; 0.31))
phi,: ((-0.3;0.38);(-0.2;0.42); (-0.1;0.46);(0.0; 0.5);(0.1;0.54);(0.2;0.58))
phi,:((0.3;0.62);(0.4;0.66);(0.5; 0.69); (0.7;0.77);(1.0; 0.84))
phi,: ((1.2;0.88);(1.5; 0.93);(1.7;0.95);(2.0; 0.98); (2.5; 0.99); (3.0; 0.999))

/ This gives the cumulative distribution of a normal
/ density function with mean 0 and variance 1
interpolatephi:{[x]
 num: x + 0.0;
 z: phi[;0];
 i: z bin num; / at or above
 if[i =  count z; :1.0]; / certainty
 if[(i = 0) & (num = z[0]); :phi[0;1]]; / very unlikely
 if[(i = 0) & (num < z[0]); :0]; / basically impossible
 j: i-1; / must be greater than zero
 bignum: z[i];
 bigval: phi[i;1];
 smallnum: z[j];
 smallval: phi[j;1];
 ratio: (num - smallnum) % (bignum - smallnum);
 (ratio * bigval) + ((1 - ratio) * smallval) }



inlistssorted: ();
outuniqs: ();
outcounts: ();
priorprobs: ();

naivebayesreset:{[]
 inlistssorted:: ();
 outuniqs:: ();
 outcounts:: ();
 priorprobs:: () }


/ This fills the above three vectors.
/ Thus this has only side effects.
naivebayesprep:{[innames; inlists; outlist]
 part: value group outlist;
 outuniqs:: distinct outlist;
 outcounts:: count each part;
 priorprobs:: outcounts % count outlist;
 inlistssorted:: ();
 i: 0;
 while[i < count innames;
       mygroups: ();
       x: inlists[i];
       inlistssorted,: enlist asc each x[part];
               / this is sorted per member of outlist
       i+: 1 ] }

/ invals are the set of values of some new instance, just a vector
/ as long as the number of elements in innames.
/ inlists is a list of length count innames each of whose members is a list
/ (e.g. one member is all heights)
/ outlist is the target decision variable
/ Output in non-verbose mode: most likely class
/ Output in verbose mode: most likely class its score and
/ then other classes and their scores.
naivebayes:{[verbose; innames; inlists; outlist; invals]
 if[0 = count outuniqs; naivebayesprep[innames; inlists; outlist]];
 / ` 0: ,"      new invals        "
 probs: ();
 i: 0;
 while[i < count outuniqs;
       x: priorprobs[i];
       j: 0;
       while[j < count invals;
               myp: probdensity[invals[j]; inlistssorted[j;i]];
               x*: myp;
               j+: 1 ];
       probs,: x;
       i+: 1 ];
 maxprob: max probs;
 if[verbose = 0;
       :(outuniqs[probs ? maxprob]) ];
 / verbose mode
 ii: idesc probs;
 (outuniqs[ii]),'(probs[ii]) }

/ This approach says average the listval, compute the std
/ Compute the number of z scores givenval is away from the average
/ 1 - number
/ Uses a normal distribution assumption but around every point.
probdensity:{[givenval; listofvals]
  s: sqrt var listofvals;
  diffs: abs each givenval - listofvals;
  zscores: (diffs % s);
  his: interpolatephi each zscores+0.1;
  lows: interpolatephi each zscores;
  (avg (his-lows)) |\: 0.000001 }

/ BAYES SIMULATION


/ Given a Bayes network along with a probability table, we want to construct
/ an appropriately weighted set of assignment to all variables.
/ Given that, we can then ask questions like prob(condition x|condition y).
/ e.g. given conclusion what is prob of evidence.
/ We would do is find all examples in the generated sample that have the
/ conclusion and generate the evidence.
/ The question is how to generate a sample.
/ Not so hard.
/ Start with the roots of the Bayesian network.
/ Assign based on their probabilities. Then for any non-root, use the
/ probabilities based on what the root values are.
/ So we will find a topological sort of the network and then generate
/ data from that.

/ compute the min of the middle 95% and the max of the middle 95%
computeminmax:{[mylist]
 x: asc mylist;
 len: floor 0.025 * count x;
 x: len _ x;
 x: (neg len) _ x;
 (first x; first reverse x) }

/ with replacement
bootsamp:{[x]
       mycount: count x;
       ii: mycount ? mycount;
       x[ii] }


/ trying to get constraints on sources and assignments
spitequal:{[pair]
 source: pair[0];
 assignment: pair[1];
 ("(bayessamp."),(string source), (" = "), (string assignment), (") & ")}




/ create a sample according to the bayesian net, starting with roots (having
/ no parents and going on from there.
/ relationships is the bayesian network and size is the size of the sample
/ sideeffect is the table mybayessamp having fields equal to all the nodes
/ in relationships and each row is a sample.
createsample:{[relationships; size]
 vars: distinct raze (relationships[;0]),(relationships[;1]);
 vars: formtoposort[relationships];
 / initialize
 varsready: "`" sv string vars;
 valinitialize: "( ";
 do[count vars; valinitialize,: "`int$();";];
 valinitialize: ((-1) _ valinitialize), (")");
 myassign: ("mybayessamp:`"),varsready,("!"),valinitialize;
 value myassign;
 / go in the order of vars
 i: 0;
 while[i < size;
       myvarvals: (); / the values for this sample element in order of toposort
       j: 0;
       while[j < count vars;
               myvar: vars[j];
               k: (relationships[;0]) ? myvar;
               mypars: relationships[k;1];
               if[0 = count mypars;
                       / no parents
                       thresh: relationships[k;2];
                       myvarvals,: probgen[thresh] ];
               if[0 < count mypars;
                       ii: vars ?/: mypars;
                       parvals: myvarvals[ii]; / interpret as binary value
                       index: 0;
                       power: 1;
                       r: reverse parvals;
                       while[0 < count r;
                               index+: (first r) * power;
                               power*: 2;
                               r: 1 _ r ];
                       thresh: (relationships[k;2])[index];
                       myvarvals,: probgen[thresh] ];
               x: ("mybayessamp."),(string vars[j]),(",: "),(string first reverse myvarvals);
               value x;
               j+: 1; ];
       i+: 1; ]; mybayessamp }

/ go from roots to most dependent variables
/ relationships[;0] has targets and relationships[;1] has the parents
formtoposort:{[relationships]
       myedges: relationships[;0 1];
       out: ();
       while[0 < count myedges;
               ii: where 0 = count each myedges[;1];
               new: myedges[ii;0];
               out,: new;
               myedges: myedges[(til count myedges) except ii];
               myedges[;1]: myedges[;1] except\: new];
       out }

/ given a sample, generate a conditional probability
/ prob(target|assignment to sources)
/ So for each source we assign it a particular value
/ we form a string of the form
/ bayessamp.target[& bayessamp.source1 = assignment1 & bayessamp.source2...]
findcond:{[target ; sources; assignment]
 x: (-3) _ raze spitequal each sources,'assignment;
 s: ("bayessamp."),(string target),("[where  ");
 alltargvalues: value (s),(x),("]");
 (sum alltargvalues) % (count alltargvalues) }


findcondboot:{[target ; sources; assignment]
 x: (-3) _ raze spitequal each sources,'assignment;
 s: ("bayessamp."),(string target),("[where ");
 alltargvalues: value (s),(x),("]");
 out: ();
 do[numboots;
       x: bootsamp[alltargvalues];
       out,:(sum x) % (count x) ];
 y: computeminmax[out];
 (avg[out]; y[0]; y[1]) }

spitequal:{[pair]
 source: pair[0];
 assignment: pair[1];
 ("(bayessamp."),(string source), (" = "), (string assignment), (") & ")}

/ given thresh, generate a number between 0 and 1.
/ If below thresh then output 1, else 0.
probgen:{[thresh] x: first 1 ? 1.0; x < thresh}



/ DATA

/ naive bayes
/ if doing this for a recommendation system site, what we would do
/ is look at the average participation of people in projects
/ i.e. each person's chance to participate in a given project is
/ how many he participates in divided by total number of projects
/ Then take all kinds of measures as inputs and estimate outlist which is
/ will he partipate or not.

innames: `height`weight`footsize;
outlist: `male`male`male`male`female`female`female`female;
inlists:();
inlists,: enlist 6 5.92 5.58 5.92 5 5.5 5.42 5.75;
inlists,: enlist 180 190 170 165 100 150 130 150;;
inlists,: enlist 12 11 12 10 6 8 7 9;
invalslist:(6.3 172 10;
 6 130 8;
 5.3 160 11;
 5.3 140 9;
 6.3 120 10);

numboots: 1000

/ EXECUTION
naivebayes[1; innames; inlists; outlist; ] each invalslist


/ sampling from a full bayesian network

/ Format of Byesnet will be nodeid, parentids, 2^(|parentids|) probabilities
/ e.g. for page 84 of Andrew Moore's Bayes Net notes
/ Note that the order is all falses, then all false plus last one is true,
/ all the way up to all are true.
relationships: ((`S; (); (0.3));
 (`L; `M`S; (0.2 0.1 0.1 0.05));
 (`T; enlist `L; (0.8 0.3));
 (`M; (); (0.6));
 (`R; enlist `M; (0.6 0.3)))

/ creates the table bayessamp
/ This can be as large as we like.
bayessamp: createsample[relationships; 38]

/ We can then ask any conditional probability question we like
/ e.g. cond prob of M given T is 1 etc.
/ findconfboot does bootstapping to give a confidence interval
findcond[`M ; enlist `T; enlist 1]
findcondboot[`M ; enlist `T; enlist 1]
findcond[`M ; `T`R; 1 0]
findcondboot[`M ; `T`R; 1 0]

