      ============================================================
      *   Banjo (Bayesian Network Inference with Java Objects)   *
      *                       Version 2.2.0                      *
      *                        15 Apr 2008                       *
      *                                                          *
      *          Banjo is licensed from Duke University.         *
      *      Copyright (c) 2005-08 by Alexander J. Hartemink.    *
      *                   All rights reserved.                   *
      ============================================================


Thank you for your interest in Banjo!  Version 2.2.0 represents the latest
feature update to our second major release, version 2.0.  As we continue to
improve Banjo, updated versions will be made available on the Banjo website, 
whose URL in April 2008 is:

http://www.cs.duke.edu/~amink/software/banjo/

Banjo 2.2 mainly adds execution capabilities on cluster environments, by 
exporting and harvesting results files in XML format.
For additional information, please refer to the Banjo User Guide.

If you are reading this file, you have probably successfully unzipped the
banjo.zip file.  Here's what we've included:

  README.txt    what you are currently reading

  LICENSE.txt   an overview of how Banjo may be licensed, along with the
                full text of the Non-Commercial Use License Agreement;
                please read this carefully before proceeding to use Banjo

  history.txt   a version history of the Banjo releases.

  banjo.jar     a Java archive file containing all the compiled Banjo code,
                which you can use to run Banjo (see below)

  data/         a directory structure containing two subdirectories, 
                release1.0 and release2.0, each of which has a directory 
                structure containing two subdirectories, one with an example
                settings file and data for learning static Bayesian networks;
                the other similarly for learning dynamic Bayesian networks;
                these will help you understand how Banjo works.
                (see below for information on how to run these two examples)

  doc/          a directory with documentation describing Banjo; within
                this folder are PDF versions of the Banjo User Guide, the
                Banjo Developer Guide, and a Javadoc directory containing a
                description of the Banjo class APIs in browseable HTML

  src/          a directory containing Banjo's full Java source tree 

  template.txt  a template that can be filled in to create a settings file
                for using Banjo with your own data

If you have a properly installed JVM, you can see Banjo in operation by
running it with the two provided examples.  Within this directory, simply
type these two commands:

  java -jar banjo.jar settingsFile=data/release2.0/static/static.settings.txt

  java -jar banjo.jar settingsFile=data/release2.0/dynamic/dynamic.settings.txt

In the subdirectories static and dynamic you will also find two "minimal" 
settings files based on the same parameters used by their respective verbose
counterparts.

For users of Banjo version 1.0.x we have also included the settings files
from the previous distribution, for convenient comparison and lookup of
changes, etc.  You can find the version 1.0.x-style settings files in the
subdirectories data/release1.0/static and data/release1.0/dynamic.

Note: Although Banjo version 2.1 was created to be backwards compatible
with settings files created for Banjo version 1.0.x, several setting names
have been deprecated, notably settings for controlling the feedback
intervals.

Enjoy!
