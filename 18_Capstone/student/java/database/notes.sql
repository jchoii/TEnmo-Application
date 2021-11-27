BEGIN TRANSACTION;
UPDATE accounts
SET balance = balance - 500
WHERE account_id = 2001 AND balance > 500;

UPDATE accounts
SET balance = balance + 500 
WHERE account_id = 2002;

COMMIT;

-- if this works, status is approved
INSERT INTO transfers(transfer_type_id,transfer_status_id,account_from,account_to,amount)
VALUES(2, 2, ?, ?, ?)


--otherwise (failed), status is rejected
INSERT INTO transfers(transfer_type_id,transfer_status_id,account_from,account_to,amount)
VALUES(2, 3, ?, ?, ?)

SELECT account_from || ', '||  account_to AS account_from_and_account_to
FROM transfers

SELECT transfer_id, (
                     SELECT username || ', ' || account_from AS logged_in_user
                     FROM transfers
                     INNER JOIN accounts ON transfers.account_from = accounts.account_id
                     INNER JOIN users ON accounts.user_id = users.user_id
                     WHERE account_from = 2002
                     ),  account_from || ', ' || username AS sender, account_to || ', ' || 
                                                                                          username
                                                                                           AS receiver, amount, transfer_type_id
FROM transfers
INNER JOIN accounts ON transfers.account_from = accounts.account_id
INNER JOIN users ON accounts.user_id = users.user_id
WHERE account_from = 2002 OR account_to = 2002; -- username is the sender

SELECT transfer_id, account_from, account_to, amount, transfer_status_id, transfer_type_id 
FROM transfers
INNER JOIN accounts ON transfers.account_from = accounts.account_id
WHERE user_id = 1002;

SELECT * , (SELECT username FROM accounts INNER JOIN users ON accounts.user_id = users.user_id WHERE accounts.account_id = account_from) AS from_name, 
(SELECT username FROM accounts INNER JOIN users ON accounts.user_id = users.user_id WHERE accounts.account_id = account_to) AS to_name
FROM transfers
WHERE account_from = 2002 OR account_to = 2002;

BEGIN TRANSACTION;
UPDATE accounts 
SET balance = balance - 50
WHERE user_id = 1002 AND balance > 50;
UPDATE accounts
SET balance = balance + 50
WHERE user_id = 1001;

commit;


ROLLBACK;


UPDATE transfers 
SET transfer_status_id = 2
FROM accounts -- this FROM replaces an Inner Join accounts.
WHERE transfers.transfer_id = 3075 AND balance > 50 ;

UPDATE transfers 
SET transfer_status_id = 1
WHERE transfer_id = 3081 AND 
                            (SELECT balance 
                             FROM accounts
                             WHERE account_id = 2002) > 50;