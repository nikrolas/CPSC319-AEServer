# For testing purpose
# User Id > 479 -> 5 (edmonton)
# User 200 <Id <= 479 -> 8 (Burnaby)
# User Id <= 200 -> 51 (Toronto)

use recordr;

INSERT INTO userlocations (UserId, LocationId)
(SELECT users.Id,5
FROM users
WHERE users.Id > 479);

INSERT INTO userlocations (UserId, LocationId)
(SELECT users.Id,8
FROM users
WHERE users.Id <= 479 AND users.Id > 200);

INSERT INTO userlocations (UserId, LocationId)
(SELECT users.Id,51
FROM users
WHERE users.Id <= 200);

INSERT INTO userlocations(UserId, LocationId)
VALUES (500, 8);

# Give user 600 full privileges
DELETE FROM userlocations WHERE UserId=600;
INSERT INTO userlocations (UserId, LocationId)
(SELECT 600, locations.Id
FROM locations);
