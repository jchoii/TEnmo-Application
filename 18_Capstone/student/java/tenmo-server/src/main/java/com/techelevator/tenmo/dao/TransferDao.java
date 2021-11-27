package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.exception.TransferNotFoundException;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    boolean transferTo(Transfer transfer);

    boolean requestTransfer(BigDecimal requested, int senderId, int receiverId);

    List<Transfer> getTransfersByAccountId(int id);

    Transfer getTransferById(int id) throws TransferNotFoundException;

    Transfer updateTransferStatusId(Transfer transfer);

}
