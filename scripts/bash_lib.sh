# Common bash functions

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

