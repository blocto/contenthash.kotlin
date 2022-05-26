# ContentHash
[![Maven Central](https://img.shields.io/maven-central/v/com.portto.ethereum/contenthash.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.portto.ethereum%22%20AND%20a:%22contenthash%22)
[![CircleCI](https://img.shields.io/circleci/build/github/portto/contenthash.kotlin/master)](https://circleci.com/gh/portto/contenthash.kotlin/tree/master)
![GitHub](https://img.shields.io/github/license/portto/contenthash.kotlin)

Kotlin implementation of EIP-1577 contenthash.

**ContentHash that is currently under development, alpha builds are available in the [Sonatype staging repository](https://s01.oss.sonatype.org/content/repositories/staging/com/portto/ethereum/contenthash/).**

## How to
```gradle
repositories {
    mavenCentral()
    
    // If you need to get ContentHash versions that are not uploaded to Maven Central.
    maven { url "https://s01.oss.sonatype.org/content/repositories/staging/" }
}

dependencies {
    implementation 'com.portto.ethereum:contenthash:0.1.0'
}
```

### Developed By
Kihon, <kihon@portto.com>

### License
ContentHash is maintained by [portto](https://github.com/portto/). Licensed under the MIT license.
