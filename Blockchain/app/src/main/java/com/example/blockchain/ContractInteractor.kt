package com.example.blockchain

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Uint
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

class ContractInteractor(
    private val web3j: Web3j,
    private val credentials: Credentials,
    private val contractAddress: String,
) {
    private val transactionManager: TransactionManager = RawTransactionManager(web3j, credentials)

    fun setA(value: BigInteger): TransactionReceipt {
        val function = org.web3j.abi.datatypes.Function(
            "setter",
            listOf(Uint(value)),
            emptyList()
        )

        return executeTransaction(function)
    }

    suspend fun getA(): Any? {
        return withContext(Dispatchers.IO) {
            val function = org.web3j.abi.datatypes.Function(
                "getter",
                emptyList(),
                listOf(object : TypeReference<Uint>() {})
            )

            try {
                val response = web3j.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        credentials.address,
                        contractAddress,
                        FunctionEncoder.encode(function)
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()

                val result = FunctionReturnDecoder.decode(response.value, function.outputParameters)
                if (result.isNotEmpty()) {
                    val latestValue = result[result.size - 1] as Uint
                    Log.d("LATVAL", "value: ${latestValue}")
                    return@withContext latestValue.value
                } else {
                    Log.d("EMPTY", "Empty result")
                    return@withContext BigInteger.ZERO
                }
            } catch (e: Exception) {
                Log.e("GETA_ERROR", "Error getting contract value: ${e.message}", e)
                return@withContext BigInteger.ZERO
            }
        }
    }

    private fun executeTransaction(function: org.web3j.abi.datatypes.Function): TransactionReceipt {
        val encodedFunction = FunctionEncoder.encode(function)
        val ethSendTransaction = transactionManager.sendTransaction(
            DefaultGasProvider.GAS_PRICE,
            DefaultGasProvider.GAS_LIMIT,
            contractAddress,
            encodedFunction,
            BigInteger.ZERO
        ) as EthSendTransaction

        return transactionReceipt(ethSendTransaction.transactionHash)
    }

    private fun transactionReceipt(transaction: String): TransactionReceipt {
        val pollingInterval = 1000
        val maxAttempts = 30

        var receipt: TransactionReceipt? = null
        var attempts = 0

        while (receipt == null && attempts < maxAttempts) {
            try {
                val ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(transaction).send()
                receipt = ethGetTransactionReceipt.transactionReceipt.orElse(null)

                if (receipt == null) {
                    Thread.sleep(pollingInterval.toLong())
                    attempts++
                }
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }

        if (receipt == null) {
            throw RuntimeException("Transaction not mined after $maxAttempts attempts")
        }

        Log.d("RECEIPT", "rec: ${receipt}")
        return receipt

    }
}
