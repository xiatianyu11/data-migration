#!/bin/ksh
. $(dirname $0)/setupProfile

allsqls="allsqls.txt"


if [ -f "$allsqls" ]
then 
	rm "$allsqls"
fi

find ./../data -type f -name "*.sql" |sort |while read filename
do
	echo "source "$filename >> "$allsqls"
done

echo "exit" >> "$allsqls"

mysql -u ${MARIA_DB_USERNAME} -p${MARIA_DB_PWD} ${MARIA_DB_SCHEMA} < ${allsqls} >> ./../logs/log.out 2>&1
	#echo $create_tables
	#$create_tables > /dev/null 2>&1
if [[ $? -ne 0 ]]; then
	print "Maria creating tables were fail"
	exit
else
	cd $(dirname $0)/../lib
	
	echo "current folder is changed `pwd`"
	
	COMMAND_LINE="java -Dcatalina.home=${BASE_DIR} com.my.miggration.DataSyncMainMultiple ${CONF_DIR}"
	
	${COMMAND_LINE}
	if [[ $? -ne 0 ]]; then
		print "Maria creating tables were fail"
	else
		print "Maria creating tables were success"
	fi
	
fi
