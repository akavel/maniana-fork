#!/bin/bash
#
# A script to fetch the crowdin translation memory.
# Tested on Max osx.

source ./bash_lib.sh

local_tmx_file="../archive/translations.tmx"

function main() {
  echo "Downloading tmx file -> ${local_tmx_file}"
  curl -o ${local_tmx_file} http://api.crowdin.net/api/project/maniana/download-tm?key=$CIKEY
  check_last_cmd "Downloading tmx file"

  ls -al ${local_tmx_file}

  echo "All done ok."
}

main




