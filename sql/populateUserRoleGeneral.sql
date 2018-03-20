use recordr;

INSERT INTO roles (roles.Id, roles.NAME)
VALUE (3, 'General');


UPDATE userroles
SET RoleId = 3
WHERE userroles.UserId <= 100;