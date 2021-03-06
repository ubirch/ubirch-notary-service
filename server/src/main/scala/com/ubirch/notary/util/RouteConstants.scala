package com.ubirch.notary.util

/**
  * author: cvandrei
  * since: 2016-06-08
  */
object RouteConstants {

  final val v1 = "v1"
  final val urlVersion = s"/$v1"

  final val notaryService = "notaryService"
  final val urlNotaryService = s"$urlVersion/$notaryService"

  final val pathNotarize = "notarize"
  final val urlNotarize = s"$urlNotaryService/$pathNotarize" // /v1/notaryService/notarize

  final val pathVerify = "verify"
  final val urlVerify = s"$urlNotaryService/$pathVerify" // /v1/notaryService/verify

  final val pathTransactions = "transactions"
  final val urlTransactions = s"$urlNotaryService/$pathTransactions" // /v1/notaryService/transactions

  final val pathUnspent = "unspent"
  final val urlTransactionsUnspent = s"$urlTransactions/$pathUnspent" // /v1/notaryService/transactions/unspent

  final val pathPending = "pending"
  final val urlTransactionsPending = s"$urlTransactions/$pathPending" // /v1/notaryService/transactions/pending

  final val pathWalletInfo = "wallet-info"
  final val urlWalletInfo = s"$urlNotaryService/$pathWalletInfo" // /v1/notaryService/wallet-info

}
