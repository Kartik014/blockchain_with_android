import React, { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { ethers, BigNumberish } from 'ethers';

const providerUrl = 'https://eth-sepolia.g.alchemy.com/v2/oLnh_W1cbrzoaSa9gk3Bqpwiw4fvyPfN'; // Replace with your Ethereum provider URL
const provider = new ethers.JsonRpcProvider(providerUrl);

const contractAddress = '0xa551F3FDEfDf7F66C8205Ba8Ca074bfEf6F0129b';
const contractABI = [
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "_a",
        "type": "uint256"
      }
    ],
    "name": "setter",
    "outputs": [],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [],
    "name": "getter",
    "outputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "stateMutability": "view",
    "type": "function",
    "constant": true
  }
];

const App = () => {

  const [value, setValue] = useState('');
  const [result, setResult] = useState('');

  const setContractValue = async () => {
    try {
      const wallet = new ethers.Wallet('c3ab60983a786b37decbbf6da84fadedb4c0d4e6e753baba82464f097ecb22cf', provider); // Replace with your private key
      const contract = new ethers.Contract(contractAddress, contractABI, wallet);

      // Convert the value to a BigNumber and send the transaction
      await contract.setter(value as BigNumberish);

      setResult('Value set successfully.');
    } catch (error) {
      console.error(error);
      setResult('Error setting value.');
    }
  };

  const getContractValue = async () => {
    try {
      const contract = new ethers.Contract(contractAddress, contractABI, provider);

      // Call the getter function
      const result = await contract.getter();
      setResult(`Value from contract: ${result}`);
    } catch (error) {
      console.error(error);
      setResult('Error getting value.');
    }
  };

  return (
    <View>
      <Text>SMART CONTRACT INTERACTOR</Text>
      <TextInput
        placeholder='Enter the value'
        onChangeText={(text) => setValue(text)}
        value={value} />
      <Button title='SET VALUE' onPress={setContractValue} />
      <Button title='GET VALUE' onPress={getContractValue} />
      <Text>{result}</Text>
    </View>
  );
};

export default App;
