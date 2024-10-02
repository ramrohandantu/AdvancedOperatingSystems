#!/bin/bash


# Change this to your netid

#
netid=$2

cwd=$(pwd)
# Root directory of your project
PROJDIR=$cwd
#pwd


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
PROG=$cwd/Project3

n=0

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    nodes=$( echo $i | awk '{ print $1 }')

    while [ $n -lt $nodes ] 
    do
        read line
	host=$( echo $line | awk '{ print $2 }' )
	host1=$( echo $line | awk '{ print $2 }' )
	host="$host.utdallas.edu"
	echo $host
	port=$( echo $line | awk '{ print $3 }' )
	echo $port
	ssh -l "$netid" "$host" "cd $PROJDIR;java Project3 $n $port $nodes $host1 $CONFIG" &
        n=$(( n + 1 ))
    done
)
