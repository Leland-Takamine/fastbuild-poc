# Minimal incremental dex POC

#### Instructions

1. Connect a device via adb
2. Run `buck install -r :apk`
3. Make a change to `fastbuild.foo.Foo`
4. Run `./fastbuild.sh`

#### What's happening at build time

1. Generate `:foo` jar
2. Generate custom `:foo-dex` dex file
3. Push dex file to `/data/local/tmp/fastbuild`
4. Restart the app

#### What's happening at runtime

At app startup, add all dex files in the following directories to the system class loader:
* `/data/local/tmp/fastbuild/`
* `/data/local/tmp/exopackage/<package-name>/secondary-dex`

The first directory is where we push the per-target dex file. That directory takes precedence which allows us to overlay changes on top of existing dex files (exo's secondary dex(s)).

#### Notes

* Same functionality be achieved simply and without reflection on Android 28+ via AppComponentFactory and DelegateLastClassLoader
* SystemClassLoaderAdder is taken from [buck](https://github.com/facebook/buck/tree/master/android/com/facebook/buck/android/support/exopackage)
* Exopackage is still required in the `android_binary` rule to enable a minimal primary dex