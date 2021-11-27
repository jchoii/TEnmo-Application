package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao{
    private JdbcTemplate jdbcTemplate;

    /***** constructor *****/
    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getAccountBalance(int id) {
        BigDecimal accountBalance = null;
        String sql = "SELECT balance FROM accounts WHERE account_id = ?;";
        accountBalance = jdbcTemplate.queryForObject(sql, BigDecimal.class, id);
        return accountBalance;
    }

    @Override
    public int findAccountIdByUserId(int id) {
        String sql = "SELECT account_id FROM accounts WHERE user_id = ?;";
        Integer accountId = jdbcTemplate.queryForObject(sql,Integer.class,id);
        if (accountId != null) {
            return accountId;
        } else {
            return -1; // this will send an account number of -1 to the RequestMapping method, which will throw a 500 error because account by -1 does not exist.
        }
    }

    @Override
    public Account updateBalance(Account account, int id) {
        boolean success = false;
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?; ";
        success = jdbcTemplate.update(sql, account.getBalance(), id) == 1;
        return account;
    }

}
