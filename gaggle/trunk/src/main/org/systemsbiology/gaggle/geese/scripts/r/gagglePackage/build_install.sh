#!/bin/sh
rm *.gz
rm *.zip
make build
sudo R --no-init-file CMD INSTALL *.gz
pushd /Library/Frameworks/R.framework/Versions/Current/Resources/library/
# edit this path if you're not Dan
#working_dir="/Users/dtenenbaum/dev/gaggle-incremental"
working_dir="/Users/cbare/Documents/work/eclipse-workspace-isb/gaggle/"
zip -r9 $working_dir/src/main/org/systemsbiology/gaggle/geese/scripts/r/gagglePackage/gaggle_1.13.0.zip gaggle
popd


