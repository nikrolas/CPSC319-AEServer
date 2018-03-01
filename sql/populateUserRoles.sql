# For testing purpose
# User Id <= 479 -> Admin
# User Id > 479 -> RMC

use recordr;

INSERT INTO userroles (UserId, RoleId)
(SELECT users.Id,1
FROM users
WHERE users.Id <= 479);

INSERT INTO userroles (UserId, RoleId)
(SELECT users.Id,2
FROM users
WHERE users.Id > 479)