package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.exception.TransferNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {
    private TransferDao transferDao;
    private UserDao userDao;
    private AccountDao accountDao;

    public TransferController(TransferDao transferDao, UserDao userDao, AccountDao accountDao){
        this.transferDao = transferDao;
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/accounts/transfers", method = RequestMethod.POST)
    public boolean transferTo(@Valid @RequestBody Transfer transfer) {
        return transferDao.transferTo(transfer);
    }

    @RequestMapping(path = "/accounts/transfers", method = RequestMethod.GET)
    public List<Transfer> listTransfers(Principal principal) {
        int userId = userDao.findIdByUsername(principal.getName());
        int accountId = accountDao.findAccountIdByUserId(userId);
        return transferDao.getTransfersByAccountId(accountId);
    }

    @RequestMapping(path = "/accounts/transfers/{id}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable Integer id) throws TransferNotFoundException {
        return transferDao.getTransferById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/accounts/transfers/request", method = RequestMethod.POST)
    public boolean requestTransfer(@Valid @RequestBody Transfer transfer) {
        return transferDao.requestTransfer(transfer.getAmount(),transfer.getAccountFrom(),transfer.getAccountTo());
    }

    @RequestMapping(path = "/accounts/transfers/update/{id}",method = RequestMethod.PUT)
    public Transfer updateTransferStatusId(@Valid @RequestBody Transfer transfer, @PathVariable Long id) throws TransferNotFoundException {
        return transferDao.updateTransferStatusId(transfer);
    }



}

