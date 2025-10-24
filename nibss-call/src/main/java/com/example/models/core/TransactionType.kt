package com.interswitchng.smartpos.shared.models.core

enum class TransactionType {
    // NOTE: Do not change the order of these values, because of DB queries

    Purchase, PreAuth, Completion, Refund, Transfer, Payments, IFIS, CashBack, RewardPurchase, Reversal, Balance
// There is a hack done here, the RewardPurchase enum item  is used to represent the More Home transaction button
// in the app module, since it is not a user-facing transaction type. It is used to show the More button on the home screen.
}