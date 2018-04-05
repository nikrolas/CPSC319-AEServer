# USAGE : sh populate.sh <server> <username> <pass>
mysql -h $1 -u $2 -p$3 recordr < "toDeploy.sql"
mysql -h $1 -u $2 -p$3 recordr < "populateUserRoles.sql"
mysql -h $1 -u $2 -p$3 recordr < "populateUserLocations.sql"
mysql -h $1 -u $2 -p$3 recordr < "addContainersTableFields.sql"