"""Restore bundled SDK device profiles when a system-image package omits them."""
import os
from pathlib import Path
from zipfile import ZipFile

sdk = Path(os.environ["ANDROID_SDK_ROOT"])
target = sdk / "system-images/android-35/google_apis/x86_64/devices.xml"
if not target.is_file():
    resource = "com/android/sdklib/devices/nexus.xml"
    for jar in (sdk / "cmdline-tools").rglob("*.jar"):
        with ZipFile(jar) as archive:
            if resource in archive.namelist():
                target.write_bytes(archive.read(resource))
                print("Restored official bundled SDK device profiles from", jar.name)
                break
    else:
        raise RuntimeError("Bundled SDK device profiles were not found")
