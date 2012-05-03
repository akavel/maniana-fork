#!/bin/bash
#
# A script to fetch the updated translations from crowdin.net.
# Tested on Max osx.
#
# TODO: convert file formats from DOS to unix 
# TODO: allow to override language list from command line

# List of two letter codes of languages to update
languages="it ja"

# Definitions
tmproot="/tmp"
tmp="${tmproot}/maniana_tmp"
url="http://crowdin.net/download/project/maniana.zip"

# Assert that the last command terminated with OK status
function check_last_cmd() {
  status=$?
  if [ "$status" -ne "0" ]; then
    echo "$1 failed (status: $status)"
    echo "ABORTED"
    exit 1
  fi  
  echo "$1 was ok"
}

function init() {
  # Create an empty temp working dir
  # Note: rm -rf is a risky command so we use 'maniana_tmp' explicitly.
  rm -rf ${tmproot}/maniana_tmp
  check_last_cmd "Removing old temp directory"
  mkdir -p ${tmp}
  check_last_cmd "Creating new temp directory"
}

function fetch() {
  # Fetch translation package
  curl -o ${tmp}/maniana.zip ${url}
  check_last_cmd "Downloading translation package"
  #ls -al ${tmp}

  # Unzip translation package
  unzip -d ${tmp} ${tmp}/maniana.zip
  check_last_cmd "Unzipping translation package"

  # Normalize some country codes
  mv -v ${tmp}/es-ES ${tmp}/es
  check_last_cmd "Normalizing es-ES dir"

  ls -al ${tmp}
  check_last_cmd "Data ls"
}

function update() {
  for lang in ${languages}
  do
    echo
    echo "--- Language: [${lang}]"
    cp -v ${tmp}/${lang}/local_strings.xml ../Maniana/res/values-${lang}/local_strings.xml
    check_last_cmd "Copying local_strings.xml"
  
    for base_name in help new_user_welcome restore_backup
    do
      name="${base_name}-${lang}.html"
      cp -v ${tmp}/${lang}/${name} ../Maniana/assets/help/${name}
      check_last_cmd "Copying ${name}"
    done
  done
}

function main() {
  init
  fetch
  update
  echo
  echo "All done ok."
}

main




