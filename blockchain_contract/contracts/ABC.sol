//SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.8.0;

contract ABC {
    uint a;
    function setter(uint _a) public{
        a=_a;
    }
    function getter() view public returns(uint){
        return a;
    }
}