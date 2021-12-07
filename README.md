[![Project Status](http://opensource.box.com/badges/active.svg)](http://opensource.box.com/badges)

# Box Java SDK

The Box Java SDK for interacting with the
[Box Content API](https://developers.box.com/docs/).

## Latest Release
Latest release can be found [here](https://github.com/box/box-java-sdk/tree/v2.58.0).

## Getting Started
Getting Started Docs: https://developer.box.com/guides/tooling/sdks/java/
API Reference: https://developer.box.com/reference/

## Quickstart
The SDK can be obtained by adding it as a [maven dependency](http://opensource.box.com/box-java-sdk/),
cloning the source into your project, or by downloading one of the precompiled JARs from the
[releases page on GitHub](https://github.com/box/box-java-sdk/releases).

**IF YOU USE THE JAR, you'll also need to include several dependencies:**

1. [minimal-json v0.9.5](https://github.com/ralfstx/minimal-json)
   Maven: `com.eclipsesource.minimal-json:minimal-json:0.9.5`
2. [jose4j v0.7.9](https://bitbucket.org/b_c/jose4j/wiki/Home)
   Maven: `org.bitbucket.b_c:jose4j:0.7.9`
3. [bouncycastle bcprov-jdk15on v1.60](http://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on)
   Maven: `org.bouncycastle:bcprov-jdk15on:1.60`
4. [bouncycastle bcpkix-jdk15on v1.60](http://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk15on)
   Maven: `org.bouncycastle:bcpkix-jdk15on:1.60`
5. [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 7](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
   If you don't install this, you'll get an exception about key length or exception about parsing PKCS private key for Box Developer Edition. This is not a Box thing, this is a U.S. Government requirement concerning strong encryption.
   The listed jar is for Oracle JRE. There might be other similar JARs for different JRE versions like the one below for IBM JDK
   [Java Cryptography Extension for IBM JDK](https://www14.software.ibm.com/webapp/iwm/web/preLogin.do?source=jcesdk)

An app has to be authorized by the admin of the enterprise before these tests. It's always good to begin with the
[Getting Started Section](https://developer.box.com/docs/setting-up-a-jwt-app) at Box's developer website

## Quick Test

**Following things work only if the app has been configured and authorized as mentioned [here](https://developer.box.com/docs/setting-up-a-jwt-app)**

Here is a simple example of how to authenticate with the API using a developer
token and then print the ID and name of each item in your root folder.

```java
BoxAPIConnection api = new BoxAPIConnection("developer-token");
BoxFolder rootFolder = BoxFolder.getRootFolder(api);
for (BoxItem.Info itemInfo : rootFolder) {
    System.out.format("[%s] %s\n", itemInfo.getID(), itemInfo.getName());
}
```

For more details on how to get started, check out the [overview guide](doc/overview.md).
It has a short explanation of how the SDK works and how you can get started using it.

### Sample Projects

Three sample projects can be found in `src/example`.

#### Main

This project will output your name and a list of the files and folders in your root directory.

To run the project, first provide a developer token in
`src/example/java/com/box/sdk/example/Main.java`. You can obtain a developer
token from your application's [developer console](https://app.box.com/developers/services).

```java
public final class Main {
    private static final String DEVELOPER_TOKEN = "<YOUR_DEVELOPER_TOKEN>";

    // ...
}
```

Then just invoke `gradle runExample` to run the Main example!

### Other projects

Below projects need app configurations stored in JSON format in `config.json` file at location `src/example/config/`.

This configuration file can be downloaded from your application's `Configuration` tab in the
[developer console](https://app.box.com/developers/console)

#### CreateAppUser

This project will output the user id of enterprise admin and will create a new App User for the enterprise.

To run the project, first provide the name of the app user in `src/example/java/com/box/sdk/example/CreateAppUser.java`.

```java
public final class CreateAppUser {

    private static final String APP_USER_NAME = "";
    private static final String EXTERNAL_APP_USER_ID = "";

    // ...
}
```

Then just invoke `gradle runCreateAppUser` to run the CreateAppUser example!

Note: The JCE bundled with oracle JRE supports keys upto 128 bit length only. To use larger cryptographic keys, install [JCE Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html).

#### AccessAsAppUser

This project will retrieve the information of the given App User and will list the files/folders under root folder.

To run the project, first provide the Id of the app user in `src/example/java/com/box/sdk/example/CreateAppUser.java`.

```java
public final class AccessAsAppUser {

    private static final String USER_ID = "";

    // ...
}
```

Then just invoke `gradle runAccessAsAppUser` to run the AccessAsAppUser example!

Note: The JCE bundled with oracle JRE supports keys upto 128 bit length only. To use larger cryptographic keys, install [JCE Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html).

#### BoxDeveloperEditionAPIConnectionAsEnterpriseUser

This example shows how to get tokens for an enterprise user, say admin of the enterprise and do actions on behalf of admin. 

To run the project, follow below steps

1. Turn on `Enterprise` in `Application Access` section in Developer Console for the app 

2. Turn on `Generate User Access Tokens` in `Advanced Features` section in Developer Console for the app

3. Provide the Id of the admin user (or any enterprise user) in `src/example/java/com/box/sdk/example/BoxDeveloperEditionAPIConnectionAsEnterpriseUser.java`.

```java
public final class BoxDeveloperEditionAPIConnectionAsEnterpriseUser {

    private static final String USER_ID = "";
    ...
    Reader reader = new FileReader("src/example/config/config.json");
    BoxConfig boxConfig = BoxConfig.readFrom(reader);

    api = new BoxDeveloperEditionAPIConnection(USER_ID, DeveloperEditionEntityType.USER, boxConfig,
        accessTokenCache);
```

## Compatibility

The Box Java SDK is compatible with Java 7 and up.

## Building

The SDK uses Gradle for its build system. SDK comes with Gradle wrapper. Running `./gradlew build` from the root
of the repository will compile, lint, and test the SDK.

```bash
$ ./gradlew build
```

The SDK also includes integration tests which make real API calls, and therefore
are run separately from unit tests. Integration tests should be run against a
test account since they create and delete data. To run the integration tests,
remove the `.template` extension from
`src/test/config/config.properties.template` and fill in your test account's
information. Then run:

```bash
$ ./gradlew integrationTest
```

## Documentation

You can find guides and tutorials in the `doc` directory.

* [BUILD ON BOX PLATFORM](https://developer.box.com/v2.0/docs/getting-started-box-platform)
* [Javadocs](http://box.github.io/box-java-sdk/javadoc/com/box/sdk/package-summary.html)
* [Overview](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/overview.md)
* [Logging](https://github.com/box/box-java-sdk/blob/main/doc/logging.md)
* [Authentication](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/authentication.md)
* [Files](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/files.md)
* [Folders](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/folders.md)
* [Comments](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/comments.md)
* [Collaborations](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/collaborations.md)
* [Events](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/events.md)
* [Search](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/search.md)
* [Users](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/users.md)
* [Groups](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/groups.md)
* [Tasks](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/tasks.md)
* [Trash](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/trash.md)
* [Collections](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/collections.md)
* [Devices](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/devices.md)
* [Retention Policies](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/retention_policies.md)
* [Legal Holds Policy](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/legal_holds.md)
* [Watermarking](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/watermarking.md)
* [Webhooks](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/webhooks.md)
* [Web Links](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/weblinks.md)
* [Metadata Templates](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/metadata_template.md)
* [Classifications](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/classifications.md)
* [Recent Items](https://github.com/box/box-java-sdk/blob/v2.58.0/doc/recent_items.md)


Javadocs are generated when `gradle javadoc` is run and can be found in
`build/doc/javadoc`.

Copyright and License
---------------------

Copyright 2019 Box, Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
