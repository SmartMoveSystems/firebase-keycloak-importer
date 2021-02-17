# Firebase User export to Keycloak import

Utility for importing exported Firebase users to an appropriately configured Keycloak instance.

To be used on a Keycloak instance configured with with the [Keycloak Firebase Scrypt](https://github.com/SmartMoveSystems/keycloak-firebase-scrypt) extension.

## Build

```bash
./gradlew build
```

## Download latest release

```
curl -L https://github.com/SmartMoveSystems/firebase-keycloak-importer/releases/download/1.0.0/firebase-keycloak-importer-1.0.0.jar > firebase-keycloak-importer-1.0.0.jar
```

## Usage

Export your Firebase user database to a JSON file using the [Firebase CLI](https://firebase.google.com/docs/cli/auth)

Export your Firebase project's hash parameters to a JSON file with the format:

```
{
    "hash_config": {
      "algorithm": "SCRYPT",
      "base64_signer_key": "jxspr8Ki0RYycVU8zykbdLGjFQ3McFUH0uiiTvC8pVMXAn210wjLNmdZJzxUECKbm0QsEmYUSDzZvpjeJ9WmXA==",
      "base64_salt_separator": "Bw==",
      "rounds": 8,
      "mem_cost": 14,
    }
}
```

Run the following (the args `"--clientId", "--roles", "--clientSecret", "--default", "--debug"` are optional):

```bash
java -jar build\libs\firebase-keycloak-importer-1.0.0.jar --usersFile example_users.json --hashParamsFile example_hash_config.json --adminUser support@smartmovetaxis.com --adminPassword admin --realm smartmove --serverUrl http://localhost:8080/auth --default true
```

The `default` argument specifies that the imported hash parameters will be the ones used for future users.
If you are only importing from one Firebase project, you must set this argument to `true`.

The client with the specified `clientId` and all specified `roles` must already exist in your Keycloak configuration.

## Importing from multiple projects

If there is a user with the same email address between multiple imported projects, the first imported user record wins.

The only difference is that the `phone_verified` claim is set to false if the imported phone numbers differ between projects.