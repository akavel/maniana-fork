#!/bin/bash
#
# A script to patch the Android SDK library that contains ViewPager.java
# Run each time upgrading to a newer version of the SDK.
#
# As of Aug 2012, the stock ViewPager class does not allow to control the
# animation speed and results in a too fast stratup animation (the goal is
# that the user will have a chance to notice the Tomorow page before 
# scrolling horizontally to Today page).

source ./bash_lib.sh

# Definitions
sdk="${HOME}/util/android-sdk-macosx"
tmproot="/tmp"
tmp="${tmproot}/maniana_tmp"

function init() {
  # Create an empty temp working dir
  # Note: rm -rf is a risky command so we use 'maniana_tmp' explicitly.
  rm -rf ${tmproot}/maniana_tmp
  check_last_cmd "Removing old temp directory"
  mkdir -p ${tmp}
  check_last_cmd "Creating new temp directory"
}

function patch_jar() {
  # Get original jar
  cp ${sdk}/extras/android/support/v4/android-support-v4.jar ${tmp}
  check_last_cmd "Copying original jar file"

  # Extract jar
  pushd ${tmp}
  mkdir jar
  cd jar
  jar -xvf ../android-support-v4.jar
  check_last_cmd "Extracting original jar file"

  # Remove ViewPager classes
  rm ./android/support/v4/view/ViewPager*.class
  check_last_cmd "Removing ViewPager classes"

  # Rejar 
  jar -cvf ../android-support-v4-minus-ViewPager.jar *
  check_last_cmd "Jar'ing modified jar"

  # Copy to Maniana project
  popd
  cp  ${tmp}/android-support-v4-minus-ViewPager.jar ../Maniana/libs
  check_last_cmd "Copying modified jar"
}

function patch_source() {
  cp \
    ${sdk}/extras/android/support/v4/src/java/android/support/v4/view/ViewPager.java \
    ../Maniana/src/android/support/v4/view/ViewPager.java 
  check_last_cmd "Copying ViewPager.java"

  echo
  echo "WANRING: must diff merge ViewPager.java manuall..."

  sleep 3 
  echo

  git difftool ../Maniana/src/android/support/v4/view/ViewPager.java
}

function main() {
  init
  patch_jar
  patch_source

  echo
  echo "All done ok."
}

main




