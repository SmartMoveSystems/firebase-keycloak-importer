# Firebase User export to Keycloak import

Utility for importing exported Firebase users to an appropriately configured Keycloak instance.

To be used on a Keycloak instance configured with with the [Keycloak Firebase Scrypt](https://github.com/SmartMoveSystems/keycloak-firebase-scrypt) extension.

## Build

```bash
./gradlew build
```

## Download latest release

```
curl -L https://github.com/SmartMoveSystems/keycloak-firebase-scrypt/releases/download/0.0.1/firebase-export-to-keycloak-import-0.0.1.jar > firebase-export-to-keycloak-import-0.0.1.jar
```

## Usage

Export your Firebase user database to a JSON file using the [Firebase CLI](https://firebase.google.com/docs/cli/auth)

Export your Firebase project's hash parameters to a JSON file with the format:

```
"hash_config" {
  "algorithm": "SCRYPT",
  "base64_signer_key": "jxspr8Ki0RYycVU8zykbdLGjFQ3McFUH0uiiTvC8pVMXAn210wjLNmdZJzxUECKbm0QsEmYUSDzZvpjeJ9WmXA==",
  "base64_salt_separator": "Bw==",
  "rounds": 8,
  "mem_cost": 14,
}
```

Run the following (the args `"--clientId", "--roles", "--clientSecret", "--default"` are optional):

The `default` argument specifies that the imported hash parameters will be the ones used for future users. 
If you are only importing from one Firebase project, you must set this argument to `true`.

```bash
java -jar firebase-export-to-keycloak-import-0.0.1.jar --usersFile exported_firebase_users.json \
                                                       --hashParamsFile hash_params.json \
                                                       --adminUser admin \
                                                       --adminPassword admin \
                                                       --realm master \
                                                       --serverUrl http://localhost:8080/auth \
                                                       --clientId your_keycloak_client_app_id \
                                                       --roles oneRole,anotherRole \
                                                       --clientSecret 0d61686d-57fc-4048-b052-4ce74978c468 \
                                                       --default true
```

The client with the specified `clientId` and all specified `roles` must already exist in your Keycloak configuration.
