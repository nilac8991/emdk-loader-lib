# EMDK Loader

[![Maven Central](https://img.shields.io/maven-central/v/dev.nilac/emdk-loader.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/dev.nilac/emdk-loader)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://developer.android.com/tools/releases/platforms)

A lightweight Kotlin wrapper around Zebra's [EMDK for Android](https://techdocs.zebra.com/emdk-for-android/) that takes the boilerplate out of initialising the EMDK Manager and processing MX profiles — including proper parsing of the XML response so you actually find out when a profile fails.

## Getting Started

EMDK is hosted in Zebra's Artifactory, so add that repository alongside Maven Central in your `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = "https://zebratech.jfrog.io/artifactory/EMDK-Android/" }
    }
}
```

Then add the dependency (the EMDK dependency is pulled in transitively):

```groovy
dependencies {
    implementation 'dev.nilac:emdk-loader:2.0.0'
}
```

## Usage

### Initialise the EMDK Manager

```kotlin
EMDKLoader.getInstance().initEMDKManager(context, object : EMDKManagerInitCallBack {
    override fun onSuccess() {
        // Ready to process profiles
    }

    override fun onFailed(message: String) {
        Log.e(TAG, "EMDK init failed: $message")
    }
})
```

### Process an MX profile

Every variant takes the same `ProfileLoaderResultCallback`:

```kotlin
val callBacks = object : ProfileLoaderResultCallback {
    override fun onProfileLoaded() {
        Log.i(TAG, "Profile applied")
    }

    override fun onProfileLoadFailed(message: String, errors: List<ProfileError>) {
        Log.e(TAG, "Profile failed: $message")
        // `errors` holds the structured <parm-error> / <characteristic-error>
        // entries parsed from the MX response, when available.
    }
}
```

**1. Apply a profile bundled in your assets** — pass `null` as the profile data and the named profile (from `assets/`) is processed as-is:

```kotlin
ProfileLoader().processProfile("MyProfile", null, callBacks)
```

**2. Apply an inline / dynamic profile** — pass an XML string as the profile data, useful when values are only known at runtime (package name, signature, etc.):

```kotlin
val profileXml = """
    <wap-provisioningdoc>
        ...
    </wap-provisioningdoc>
""".trimIndent()

ProfileLoader().processProfile("MyProfile", profileXml, callBacks)
```

**3. Apply after a delay** — same as above but waits `delayMillis` before processing (handy when a profile must settle after another change):

```kotlin
ProfileLoader().processProfileWithDelay("MyProfile", profileXml, 2000L, callBacks)
```

> `processProfile(...)` and `processProfileNow(...)` both apply the profile as soon as the Profile Manager is ready.

Because the response is parsed, a `CHECK_XML` result that hides a `<parm-error>` or `<characteristic-error>` is reported through `onProfileLoadFailed` as a real failure — not a false success.

### Release

```kotlin
EMDKLoader.getInstance().release()
```

## Migrating from JitPack (1.x)

`2.0.0` moved to Maven Central and includes breaking changes. If you were consuming the library through JitPack:

- **Dependency & repository** — drop the `jitpack.io` repository and swap the coordinate:

  ```diff
  - implementation 'com.github.nilac8991:emdk-loader-lib:1.1.1'
  + implementation 'dev.nilac:emdk-loader:2.0.0'
  ```

- **Package rename** — update your imports from `com.zebra.nilac.emdkloader` to `com.nilac.emdkloader`.

- **Callback change** — `ProfileLoaderResultCallback` now has a single failure method (the `EMDKResults` overload was removed):

  ```diff
  - override fun onProfileLoadFailed(message: String) { }
  - override fun onProfileLoadFailed(errorObject: EMDKResults) { }
  + override fun onProfileLoadFailed(message: String, errors: List<ProfileError>) { }
  ```

## Notice

The library only wraps the EMDK — it does not replace it. For how the EMDK and MX profiles actually work, please refer to Zebra's official Documentation on [TechDocs](https://techdocs.zebra.com/emdk-for-android/).

## License

Licensed under the [Apache License 2.0](LICENSE).
