# #!/bin/bash

# #return "y" if there is a difference, "n" otherwise
# check_diff() {
#     result="$(diff $1 $2)"
#     if [ -n "$result" ]; then
#         echo y
#     else
#         echo n
#     fi
# }

# verbose=false
# noexecute=false

# if [ $# -lt 4 ] || [ $# -gt 6 ]; then
#     echo "Usage: $0 <submission_dir> <target_dir> <test_dir> <answer_dir> [OPTIONAL: -v -noexecute]"
#     exit 1
# fi

# if [ $# -eq 5 ]; then
#     if [ $5 != "-v" ] && [ $5 != "-noexecute" ]; then
#         echo "Usage: $0 <submission_dir> <target_dir> <test_dir> <answer_dir> [OPTIONAL: -v -noexecute]"
#         exit 1
#     elif [ $5 = "-v" ]; then
#         verbose=true
#     else
#         noexecute=true
#     fi
# fi

# if [ $# -eq 6 ]; then
#     verbose=true
#     noexecute=true
#     if [ $5 != "-v" ] && [ $5 != "-noexecute"]; then
#         echo "Usage: $0 <submission_dir> <target_dir> <test_dir> <answer_dir> [OPTIONAL: -v -noexecute]"
#         exit 1
#     fi
#     if [ $6 != "-v" ] && [ $6 != "-noexecute"]; then
#         echo "Usage: $0 <submission_dir> <target_dir> <test_dir> <answer_dir> [OPTIONAL: -v -noexecute]"
#         exit 1
#     fi
# fi

# if [ ! -d $1 ]; then
#     echo "Error: submission directory does not exist"
#     exit 1
# fi

# if [ -d $2 ]; then
#     rm -rf $2
# fi
# if [ ! -d $3 ]; then
#     echo "Error: test directory does not exist"
#     exit 1
# fi
# if [ ! -d $4 ]; then
#     echo "Error: answer directory does not exist"
#     exit 1
# fi

# submission_dir=$1
# target_dir=$2
# test_dir=$3
# answer_dir=$4
# rm -rf $target_dir
# mkdir $target_dir
# mkdir $target_dir/C
# mkdir $target_dir/Java
# mkdir $target_dir/Python
# if [ $noexecute != "true" ]; then
#     touch $target_dir/result.csv
#     echo "student_id,type,matched,not_matched" >$target_dir/result.csv
# fi
# cd $test_dir
# if [ $verbose = "true" ]; then
#     echo "Found $(find . -type f -name "*.txt" | wc -l) test files"
# fi

# cd - > /dev/null
# cd $submission_dir
# for i in *.zip; do
#     string=$i
#     string=${string: -11}
#     string2=${string:0:7}
#     if [ $verbose = "true" ]; then
#         echo "Organizing files of $string2"
#     fi
#     mkdir tempDir$string2
#     unzip -q "$i" -d tempDir$string2
#     j=$(find tempDir$string2 -type f -name "*.c")
#     if [ -n "$j" ]; then
#         cd - > /dev/null
#         mkdir $target_dir/C/$string2
#         mv $submission_dir/"$j" $target_dir/C/$string2/main.c
#         if [ $noexecute != "true" ]; then
#             if [ $verbose = "true" ]; then
#                 echo "Executing files of $string2"
#             fi
#             gcc $target_dir/C/$string2/main.c -o $target_dir/C/$string2/main.out
#             cd $test_dir
#             matchCnt=0
#             notMatchCnt=0
#             for k in *.txt; do
#                 number=${k:4: -4}
#                 cd - > /dev/null
#                 ./$target_dir/C/$string2/main.out <$test_dir/$k >$target_dir/C/$string2/out$number.txt
#                 diff_result="$(check_diff $target_dir/C/$string2/out$number.txt $answer_dir/ans$number.txt)"
#                 if [ $diff_result = "y" ]; then
#                     notMatchCnt=$(expr $notMatchCnt + 1)
#                 else
#                     matchCnt=$(expr $matchCnt + 1)
#                 fi
#                 cd $test_dir
#             done
#             cd - > /dev/null
#             echo "$string2,C,$matchCnt,$notMatchCnt" >>$target_dir/result.csv
#         fi
#         cd $submission_dir
#     fi
#     j=$(find tempDir$string2 -type f -name "*.java")
#     if [ -n "$j" ]; then
#         cd - > /dev/null
#         mkdir $target_dir/Java/$string2
#         mv $submission_dir/"$j" $target_dir/Java/$string2/Main.java
#         if [ $noexecute != "true" ]; then
#             if [ $verbose = "true" ]; then
#                 echo "Executing files of $string2"
#             fi
#             javac $target_dir/Java/$string2/Main.java
#             cd $test_dir
#             matchCnt=0
#             notMatchCnt=0
#             for k in *.txt; do
#                 number=${k:4: -4}
#                 cd - > /dev/null
#                 java -cp $target_dir/Java/$string2/ Main <$test_dir/$k >$target_dir/Java/$string2/out$number.txt
#                 diff_result="$(check_diff $target_dir/Java/$string2/out$number.txt $answer_dir/ans$number.txt)"
#                 if [ $diff_result = "y" ]; then
#                     notMatchCnt=$(expr $notMatchCnt + 1)
#                 else
#                     matchCnt=$(expr $matchCnt + 1)
#                 fi
#                 cd $test_dir
#             done
#             cd - > /dev/null
#             echo "$string2,Java,$matchCnt,$notMatchCnt" >>$target_dir/result.csv
#         fi
#         cd $submission_dir
#     fi
#     j=$(find tempDir$string2 -type f -name "*.py")
#     if [ -n "$j" ]; then
#         cd - > /dev/null
#         mkdir $target_dir/Python/$string2
#         mv $submission_dir/"$j" $target_dir/Python/$string2/main.py
#         if [ $noexecute != "true" ]; then
#             if [ $verbose = "true" ]; then
#                 echo "Executing files of $string2"
#             fi
#             cd $test_dir
#             matchCnt=0
#             notMatchCnt=0
#             for k in *.txt; do
#                 number=${k:4: -4}
#                 cd - > /dev/null
#                 python $target_dir/Python/$string2/main.py <$test_dir/$k >$target_dir/Python/$string2/out$number.txt
#                 diff_result="$(check_diff $target_dir/Python/$string2/out$number.txt $answer_dir/ans$number.txt)"
#                 if [ $diff_result = "y" ]; then
#                     notMatchCnt=$(expr $notMatchCnt + 1)
#                 else
#                     matchCnt=$(expr $matchCnt + 1)
#                 fi
#                 cd $test_dir
#             done
#             cd - > /dev/null
#             echo "$string2,Python,$matchCnt,$notMatchCnt" >>$target_dir/result.csv
#         fi
#         cd $submission_dir
#     fi
#     rm -rf tempDir$string2
# done


j="$(find . -type f)"

for i in $j; do
    echo $i
    cat "$i" | grep pico
done