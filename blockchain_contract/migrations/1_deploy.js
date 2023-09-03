var MyContract = artifacts.require("ABC");

module.exports = function (deployer) {
  deployer.deploy(MyContract);
};