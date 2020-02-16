# Minimal incremental dex POC

Proof of concept demonstrating the ability to build/install/run code on a per-target basis.

#### Instructions

1. Connect a device via adb
2. Run `buck install -r :apk`
3. Make a change to `app.foo.Foo`
4. Run `./fastbuild.sh`

#### What's happening at build time ([fastbuild.sh](https://github.com/Leland-Takamine/fastbuild-poc/blob/cf5914bdd3bf109639c31208f8fe9f607c26352d/fastbuild.sh#L1))

1. Generate `:foo` jar ([BUCK](https://github.com/Leland-Takamine/fastbuild-poc/blob/cf5914bdd3bf109639c31208f8fe9f607c26352d/BUCK#L49-L52))
2. Generate custom `:foo-dex` dex file ([BUCK](https://github.com/Leland-Takamine/fastbuild-poc/blob/cf5914bdd3bf109639c31208f8fe9f607c26352d/BUCK#L20-L24))
3. Push dex file to `/data/local/tmp/fastbuild`
4. Restart the app

#### What's happening at runtime

At app startup, add all dex files in the following directories to the system class loader ([Fastbuild.java](https://github.com/Leland-Takamine/fastbuild-poc/blob/cf5914bdd3bf109639c31208f8fe9f607c26352d/src/fastbuild/Fastbuild.java#L15-L18)):
* `/data/local/tmp/fastbuild/`
* `/data/local/tmp/exopackage/<package-name>/secondary-dex`

The first directory is where we push the per-target dex file. That directory takes precedence which allows us to overlay changes on top of existing dex files (exo's secondary dex(s)).

#### Notes

* Same functionality be achieved simply and without reflection on Android 28+ via [AppComponentFactory.instantiateClassLoader](https://developer.android.com/reference/android/app/AppComponentFactory.html#instantiateClassLoader(java.lang.ClassLoader,%20android.content.pm.ApplicationInfo)) and [DelegateLastClassLoader](https://developer.android.com/reference/dalvik/system/DelegateLastClassLoader)
* SystemClassLoaderAdder is a slightly modified version of [this class](https://github.com/facebook/buck/blob/master/android/com/facebook/buck/android/support/exopackage/SystemClassLoaderAdder.java)
* Exopackage is still required in the `android_binary` rule to enable a minimal primary dex ([BUCK](https://github.com/Leland-Takamine/fastbuild-poc/blob/cf5914bdd3bf109639c31208f8fe9f607c26352d/BUCK#L11-L16))
* The [d8 genrule](https://github.com/Leland-Takamine/fastbuild-poc/blob/cf5914bdd3bf109639c31208f8fe9f607c26352d/BUCK#L20-L24) is hardcoded. It would be more robust to match the d8 configuration of an existing `android_binary` rule.
