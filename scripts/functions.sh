function getTests {
	if [[ $# -eq 0 ]] ; then
		echo "No paramater given, executing de.precision.AddTest_NoGC"
		tests="de.precision.AddTest_NoGC"
	else
		tests=$1
	fi
}

function getGlobalParameters {
	if [[ -z "$VMS" ]]; then
	  vms=250
	else
	  vms=$VMS
	fi
	
	if [[ ! -z "$REDIRECT" ]]; then
	    redirect=$REDIRECT
		sed -i "s/REDIRECT = [a-z]\+/REDIRECT = $REDIRECT/g" ../src/test/java/de/precision/Constants.java
	else
		redirect=true
	fi 
}

function setExecutions {
	sed -i "s/EXECUTIONS = [0-9]\+/EXECUTIONS = "$1"/g" ../src/test/java/de/precision/Constants.java
}

function getSum {
  awk '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}
