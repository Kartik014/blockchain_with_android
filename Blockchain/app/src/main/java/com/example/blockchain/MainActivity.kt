package com.example.blockchain

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.blockchain.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.Properties

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var web3j: Web3j
    private lateinit var credentials: Credentials
    private lateinit var contractInteractor: ContractInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val envFile = assets.open("config.env")
        val envProperties = Properties()
        envProperties.load(envFile)

        val INFURA_URL = "https://eth-sepolia.g.alchemy.com/v2/oLnh_W1cbrzoaSa9gk3Bqpwiw4fvyPfN"
        val PRIVATE_KEY = envProperties.getProperty("PRIVATE_KEY")
        val CONTRACT_ADDRESS = envProperties.getProperty("CONTRACT_ADDRESS")

        web3j = Web3j.build(HttpService(INFURA_URL))
        credentials = Credentials.create(PRIVATE_KEY)
        contractInteractor = ContractInteractor(web3j, credentials, CONTRACT_ADDRESS)

        binding.setButton.setOnClickListener {
            val value = binding.valueEditText.text.toString().toBigInteger()
            setContractValue(value)
            Log.d("START", "Function called")
        }

        binding.getButton.setOnClickListener {
            getContractValue()
        }
    }

    private fun setContractValue(value: BigInteger) {
        try {
            GlobalScope.launch(Dispatchers.IO) {
                val receipt = contractInteractor.setA(value)
                Log.d("REC", "rec: $receipt")
                val transactionHash = receipt.transactionHash
                Log.d("TransactionHash", "Transaction Hash: $transactionHash")
            }
        } catch (e: Exception) {
            Log.e("Error", "Error setting contract value: ${e.message}", e)
        }
    }

    private fun getContractValue() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("called1", "Function called")
                val value = contractInteractor.getA()
                Log.d("called", "Function called")
                withContext(Dispatchers.Main) {
                    binding.resultTextView.text = "Value of a: $value"
                    Log.d("VALUE", "value: $value")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error getting contract value: ${e.message}", e)
            }
        }
    }

}
