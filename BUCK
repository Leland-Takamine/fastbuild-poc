android_binary(
    name = 'apk',
    keystore = ':keystore',
    aapt_mode = 'aapt2',
    dex_tool = 'd8',
    manifest = 'AndroidManifest.xml',
    manifest_entries = {
        'target_sdk_version': 29,
        'min_sdk_version': 29,
    },
    use_split_dex = True,
    exopackage_modes = ['secondary_dex'],
    primary_dex_patterns = [
        '^app/Application^',
        '^fastbuild',
    ],
    deps = [ ':app' ],
)

genrule(
    name = 'foo-dex',
    out = 'dex.jar',
    cmd = '$ANDROID_HOME/build-tools/29.0.2/d8 --output ${OUT} $(location :foo)'
)

keystore(
    name = 'keystore',
    store = 'debug.keystore',
    properties = 'keystore.properties',
)

android_library(
    name = 'app',
    srcs = ['src/app/Application.java'],
    deps = [
        ':fastbuild',
        ':activity'
    ]
)

android_library(
    name = 'activity',
    srcs = glob(['src/app/activity/**']),
    deps = [
        ':foo',
    ]
)

android_library(
    name = 'foo',
    srcs = glob(['src/app/foo/**']),
)

android_library(
    name = 'fastbuild',
    srcs = glob(['src/fastbuild/**']),
)
