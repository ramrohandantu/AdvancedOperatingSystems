#!/bin/bash


# Change this to your netid
netid=$2

cwd=$(pwd)
#
# Root directory of your project
PROJDIR=$cwd

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$cwd/$1

#
# Directory your java classes are in
#
#BINDIR=$PROJDIR/bin

#
# Your main project class
#
PROG=Project1

n=0

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read line
    nodes=$( echo $line | awk '{ print $1 }' )
    echo $n
    echo $nodes
    
    while [ $n -lt $nodes ] 
    do
        read line
	echo $line
	host=$( echo $line | awk '{ print $2 }' )
        host="$host.utdallas.edu"
        echo $host
        ssh -l "$netid" "$host" "killall -u $netid" &
        sleep 1
        n=$(( n + 1 ))
    done   
)
echo "Cleanup complete"
