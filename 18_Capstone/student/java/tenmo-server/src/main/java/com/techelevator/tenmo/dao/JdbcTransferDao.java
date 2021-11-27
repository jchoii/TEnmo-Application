package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.exception.TransferNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {
    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional // allows us to make changes but will rollback changes if our sql statements fail; allows us to remove the "commit" from our sql statement, so we can test if any rows were changed.
    @Override
    public boolean transferTo(Transfer transfer) {

        /***
         *  1. check to see if balance is greater than amount
         *  2. Complete transfer (update accounts tables)
         *  3. Update transfer table
         */
        String sql =
                     "UPDATE accounts " +
                     "SET balance = balance - ? " +
                     "WHERE account_id = ? AND balance > ?; " +
                     "UPDATE accounts " +
                     "SET balance = balance + ? " +
                     "WHERE account_id = ?; ";

        boolean success = jdbcTemplate.update(sql,transfer.getAmount(),transfer.getAccountFrom(),transfer.getAmount(),transfer.getAmount(),transfer.getAccountTo()) == 1;
        insertIntoTransferTable(success,transfer.getAccountFrom(),transfer.getAccountTo(),transfer.getAmount());
        return success;
    }

    @Override
    public boolean requestTransfer(BigDecimal amount, int moneySenderAccountId, int requesterAccountId) {
        String sql = "INSERT INTO transfers(transfer_type_id,transfer_status_id,account_from,account_to,amount) " +
                     "VALUES(1,1,?,?,?);";
        boolean success = (jdbcTemplate.update(sql,moneySenderAccountId,requesterAccountId,amount)) == 1;
        return success;
    }

    @Override
    public List<Transfer> getTransfersByAccountId(int id) {
        List<Transfer> transferList = new ArrayList<>();
        /***
         *  1. get all transfers where current user is in the account_from or account_to columns of the transfers table
         *  2. Get the From username and the To username -- is there a way to get this in java?
         */
        String sql = "SELECT transfer_id, account_from, account_to, amount, transfer_status_id, transfer_type_id , " +
                     "(SELECT username FROM accounts INNER JOIN users ON accounts.user_id = users.user_id WHERE accounts.account_id = account_from) " +
                     "AS from_name, (SELECT username FROM accounts INNER JOIN users ON accounts.user_id = users.user_id WHERE accounts.account_id = account_to) AS to_name " +
                     "FROM transfers " +
                     "WHERE account_from = ? OR account_to = ?;";
        // we need to return something to the client side from which the client can get the usernames of the from and to accounts.
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,id,id);
        while (results.next()) {
            transferList.add(mapRowToTransfers(results));
        }
        return transferList;
    }

    @Override
    public Transfer getTransferById(int id) throws TransferNotFoundException {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, account_from, account_to, amount, transfer_status_id, transfer_type_id , " +
                "(SELECT username FROM accounts INNER JOIN users ON accounts.user_id = users.user_id WHERE accounts.account_id = account_from) " +
                "AS from_name, (SELECT username FROM accounts INNER JOIN users ON accounts.user_id = users.user_id WHERE accounts.account_id = account_to) AS to_name " +
                "FROM transfers " +
                "WHERE transfer_id = ? ;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,id);

        if (results.next()) {
            transfer = mapRowToTransfers(results);
        } else {
            throw new TransferNotFoundException();
        }

        return transfer;
    }

    @Override
    public Transfer updateTransferStatusId(Transfer transfer) {
        boolean success = false;
        String sql =
                "UPDATE transfers " +
                "SET transfer_status_id = ? " +
                "FROM accounts " +
                "WHERE transfers.transfer_id = ? AND balance >= ?;";
        success = jdbcTemplate.update(sql,transfer.getTransferStatusId(),transfer.getTransferId(),transfer.getAmount()) == 1;
        return transfer;
    }

    private Transfer mapRowToTransfers(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getLong("transfer_id"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setFromName(results.getString("from_name"));
        transfer.setToName(results.getString("to_name"));
        return transfer;
    }

    public void insertIntoTransferTable(boolean success, int senderId, int receiverId, BigDecimal amount) {
        String sql = "";
        if (success) { // if success == true, insert into transfers the following sql:
            sql = "INSERT INTO transfers(transfer_type_id,transfer_status_id,account_from,account_to,amount) " +
                    "VALUES(2, 2, ?, ?, ?) ;";
        } else {  // else if success == false, insert into transfers the following sql:
            sql = "INSERT INTO transfers(transfer_type_id,transfer_status_id,account_from,account_to,amount) " +
                    "VALUES(2, 3, ?, ?, ?) ;";
        }
        jdbcTemplate.update(sql,senderId,receiverId,amount);
    }


}
