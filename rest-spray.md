# Notary Service REST API with Spray

This documentation attempts to list all relevant information for the __Notary Service__ REST API.

* [Configuration](#config)
* [Available Methods](#methods)
* [Curl Calls](#curl)

## Configuration {#config}

The configuration is done in the `application[.*].conf` files and always loaded through `com.ubirch.notary.config.AppConfig.config`.
By default `application.conf` is used when we want another one we can configure a system property (`-Dubirch.env=application.dev`)
or an environment variable (`export ubirch.env=application.dev`). If using neither system properties nor enviroment variables
is an option the deployed jar may contain an `application.conf` matching the environment.

In the future we might extend this mechanism to load the config from an external source (e.g. DynamoDB; see 
com.ubirch.notary.config.AppConfig.dynamicConf for the current test implementation).

The following configuration key are available.

### Network

    notaryService.interface
    notaryService.port

### Bitcoin

Bitcoin connects to a network (`org.bitcoin.regtest`, `org.bitcoin.test` or `org.bitcoin.production`).

    bitcoin.network

We can tell bitcoinj to use a Tor proxy.

    bitcoin.tor.enabled = true (defaults to false)

There's also a wallet. If none is found a new one is generated in the specified location.

    bitcoin.wallet.directory
    bitcoin.wallet.prefix

The amount sent along with the `OP_RETURN` has to cover the transaction fees and if wanted we can configure it, too. The
higher it is the sooner our transaction will be included in the next block but if it's too low it might never be
processed see [https://bitcoinj.github.io/working-with-the-wallet#using-fees](https://bitcoinj.github.io/working-with-the-wallet#using-fees) for details.

    bitcoin.feePerKb

### Spray

There should not be much of a need to change the spray configuration but if there is `application.base.conf` currently
contains it.


## Available Methods {#methods}

The service has three main routes.

**curl calls are listed in the next chapter**

### Wallet Information

    GET /v1/notaryService/wallet-info

### Transactions

    GET /v1/notaryService/transactions
    GET /v1/notaryService/transactions/unspent
    GET /v1/notaryService/transactions/pending

### Publish Data on Blockchain

    POST /v1/notaryService/notarize // (with `Header: Content-Type: application/json`)

This method can be used to publish a string as we send it in or treat the string as a hex-encoded hash. BitcoinJ limits
the information to 40 bytes.

To notarize a simple string (not a hash) we may POST:

```{.json}
{
  "data": "ubirch-test"
  "signature": "$SIGNATURE", // (optional) signature of "data" (will become mandatory at some point)
  "publicKey": "$PUBLIC_KEY" // (optional) publicKey allowing us to verify the signature; we also have to trust it (will become mandatory at some point)
}
```

To notarize a hex-encoded hash we may POST:

```{.json}
{
  "data": "8e2cffc1287d06c7631f036fbd1634d5cbf015acf345f1f37566c00014b20add"
  "dataIsHash": true,
  "signature": "$SIGNATURE", // (optional) signature of "data" (will become mandatory at some point)
  "publicKey": "$PUBLIC_KEY" // (optional) publicKey allowing us to verify the signature; we also have to trust it (will become mandatory at some point)
}
```

In both cases the response is the resulting transaction hash (txHash) in the Bitcoin blockchain.


## `curl` Calls {#curl}

### Wallet Information

    curl http://localhost:8080/v1/notaryService/wallet-info

### Transactions

    curl http://localhost:8080/v1/notaryService/transactions
    curl http://localhost:8080/v1/notaryService/transactions/unspent
    curl http://localhost:8080/v1/notaryService/transactions/pending

### Publish Data on Blockchain

    For clear text data:
    
        curl -XPOST localhost:8080/v1/notaryService/notarize -H "Content-Type: application/json" -d '{"data":"ubirch-test"}'
    
    If data is a hex-encoded string:
    
        curl -XPOST localhost:8080/v1/notaryService/notarize -H "Content-Type: application/json" -d '{"data": "8e2cffc1287d06c7631f036fbd1634d5cbf015acf345f1f37566c00014b20add", "dataIsHash": true}'
