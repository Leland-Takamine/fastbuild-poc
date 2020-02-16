#!/bin/bash

set -e
set -x

partial=$(buck build --show-output :foo-dex | awk '{print $2}')
adb push $partial /data/local/tmp/fastbuild/partial.zip
adb shell am force-stop fastbuild.app
adb shell am start fastbuild.app
