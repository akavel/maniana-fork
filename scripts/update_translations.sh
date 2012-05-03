#!/bin/bash

# A script to fetch the updated translations from crowdin.net.
# Tested on Max osx.
#
# TODO: add assertions (dir/files exists, exist status ok, etc)
# TODO: convert file formats from DOS to unix 

# Definitions
tmproot="/tmp"
tmp="${tmproot}/maniana_tmp"
url="http://crowdin.net/download/project/maniana.zip"

# List of two letter codes of languages to update
languages="it ja"

# Create an empty temp working dir
# Note: rm -rf is a risky command so we use 'maniana_tmp' explicitly.
rm -rf ${tmproot}/maniana_tmp
mkdir -p ${tmp}

# Fetch translation package
echo
echo "Fetching translation package"
curl -o ${tmp}/maniana.zip ${url}
#ls -al ${tmp}

# Unzip translation package
unzip -d ${tmp} ${tmp}/maniana.zip

# Normalize some country codes
echo 
echo "Normalizing language directories"
mv -v ${tmp}/es-ES ${tmp}/es

echo
ls -al ${tmp}

for lang in ${languages}
do
  echo
  echo "--- Language: ${lang}"
  cp -v ${tmp}/${lang}/local_strings.xml ../Maniana/res/values-${lang}/local_strings.xml

  for name in help new_user_welcome restore_backup
  do
    cp -v ${tmp}/${lang}/${name}-${lang}.html ../Maniana/assets/help/${name}-${lang}.html
  done
done

echo
echo "All done."




