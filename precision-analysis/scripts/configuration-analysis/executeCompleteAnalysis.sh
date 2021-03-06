function extractAll {
	start=$(pwd)
	cd $1
	for file in AddTest RAMTest SysoutTest
	do
		echo "Extracting data from $file"
		cd $file
		for repetitionCount in 100 1000 10000 100000 1000000
		do
			if [ -f precision_$repetitionCount.tar ]
			then
				if [ ! -d precision_$repetitionCount ]
				then
					mkdir precision_$repetitionCount
					tar -xvf precision_"$repetitionCount".tar -C precision_$repetitionCount &> extract_"$file".txt
				fi
			fi
		done
		cd ..
	done
	cd $start
}

function analyzeNoOutlierRemoval {
	minVMs=$1
	maxVMs=$2
	vmResolution=$3
	THREADS=$4
	
	java -Xmx22g \
		-cp $start/../../build/libs/precision-analysis-all-2.13.jar \
		de.precision.analysis.repetitions.GeneratePrecisionPlot \
		-threads $THREADS \
		--statisticalTests ALL_NO_BIMODAL \
		--iterationResolution 100 \
		--vmResolution $vmResolution \
		--minVMs $minVMs \
		--maxVMs $maxVMs \
		-data $file > "$file"_analysis_noOutlierRemoval_$minVMs.txt 
}

function analyzeOutlierRemoval {
	minVMs=$1
	maxVMs=$2
	vmResolution=$3
	THREADS=$4
	
	java -Xmx22g \
		-cp $start/../../build/libs/precision-analysis-all-2.13.jar \
		de.precision.analysis.repetitions.GeneratePrecisionPlot \
		-threads $THREADS \
		--statisticalTests ALL_NO_BIMODAL \
		--iterationResolution 100 \
		--vmResolution $vmResolution \
		--minVMs $minVMs \
		--maxVMs $maxVMs \
		--outlierRemoval \
		-data $file > "$file"_analysis_outlierRemoval_$minVMs.txt 
}

function analyze {
	start=$(pwd)
	cd $1
	echo "Starting analysis (will take at least 30 minutes)"
	THREADS=8
	for file in AddTest RAMTest SysoutTest
	do
		echo "Analyzing $file"
		
		if [ ! -d $file/results_noOutlierRemoval ]
		then
			echo "... without outlier removal"
			analyzeNoOutlierRemoval 0 100 100 $THREADS
			mv $file/results_noOutlierRemoval/precision.csv $file/results_noOutlierRemoval/precision_0.csv 
			analyzeNoOutlierRemoval 100 -1 20 $THREADS
			cat $file/results_noOutlierRemoval/precision_0.csv >> $file/results_noOutlierRemoval/precision.csv
		fi
		
		if [ ! -d $file/results_outlierRemoval ]
		then
			echo "... with outlier removal"
			analyzeOutlierRemoval 0 100 100 $THREADS
			mv $file/results_outlierRemoval/precision.csv $file/results_outlierRemoval/precision_0.csv 
			analyzeOutlierRemoval 100 -1 20 $THREADS
			cat $file/results_outlierRemoval/precision_0.csv >> $file/results_outlierRemoval/precision.csv
		fi
	done
	wait
	cd $start
}

if [ $# -eq 1 ]
then
	echo "Assuming that the passed parameter $1 contains the default folders basic-parameter-comparison and parallel-sequential-comparison"
	basicParameterComparison=$1/basic-parameter-comparison
	parallelSequentialComparison=$1/parallel-sequential-comparison
else
	if [ $# -lt 2 ]
	then
		echo "Arguments missing, please specify 2 folders (sequentiel, parallel)"
		exit 1
	else
		basicParameterComparison=$1
		parallelSequentialComparison=$2
	fi
fi

if [ ! -d $basicParameterComparison ]
then
	echo "Assumed $basicParameterComparison is a directory, but wasn't"
	exit 1
fi
if [ ! -d $parallelSequentialComparison ]
then
	echo "Assumed $parallelSequentialComparison is a directory, but wasn't"
	exit 1 
fi


#extractAll $basicParameterComparison
#analyze $basicParameterComparison
#extractAll $parallelSequentialComparison
#analyze $parallelSequentialComparison

./createHeatmaps.sh $basicParameterComparison $parallelSequentialComparison

