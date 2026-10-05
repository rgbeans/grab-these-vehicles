#!/usr/bin/env python3
"""Dependency-free Android SDK build, matching the Gradle application's settings.

Requires JDK 17 and Android SDK platform 36 / build-tools 36.0.0.
Usage: ANDROID_HOME=/path/to/sdk python3 tools/build_apk.py
Release signing uses the four GTV_KEYSTORE_* / GTV_KEY_ALIAS environment variables.
Without them, a local development key is generated in the ignored signing folder.
"""
import os
import pathlib
import shutil
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
SDK = pathlib.Path(os.environ.get('ANDROID_HOME', os.environ.get('ANDROID_SDK_ROOT', '')))
TOOLS = SDK / 'build-tools' / '36.0.0'
ANDROID = SDK / 'platforms' / 'android-36' / 'android.jar'
BUILD = ROOT / 'app' / 'build' / 'standalone'
SOURCE = ROOT / 'app' / 'src' / 'main'
PACKAGE = 'com.grabthesevehicles.app'

def run(*args):
    subprocess.run([str(a) for a in args], check=True)

def main():
    if not ANDROID.is_file() or not (TOOLS / 'aapt2').is_file():
        raise SystemExit('Set ANDROID_HOME to an SDK with platforms;android-36 and build-tools;36.0.0.')
    if BUILD.exists(): shutil.rmtree(BUILD)
    for d in ['resources','generated','classes','dex','outputs']: (BUILD / d).mkdir(parents=True)
    # The Gradle Android plugin supplies the manifest package; provide it for aapt2 here.
    tree = ET.parse(SOURCE / 'AndroidManifest.xml')
    tree.getroot().set('package', PACKAGE)
    manifest = BUILD / 'AndroidManifest.xml'
    tree.write(manifest, encoding='utf-8', xml_declaration=True)
    run(TOOLS/'aapt2', 'compile', '--dir', SOURCE/'res', '-o', BUILD/'resources')
    run(TOOLS/'aapt2', 'link', '-o', BUILD/'resources.apk', '-I', ANDROID,
        '--manifest', manifest, '--java', BUILD/'generated',
        '--min-sdk-version', '26', '--target-sdk-version', '36',
        '--version-code', '8', '--version-name', '1.6', '--auto-add-overlay',
        *sorted((BUILD/'resources').glob('*.flat')))
    sources = sorted((SOURCE/'java').rglob('*.java')) + sorted((BUILD/'generated').rglob('*.java'))
    run('java','-m','jdk.compiler/com.sun.tools.javac.Main','-encoding','UTF-8',
        '-source','8','-target','8','-classpath',ANDROID,'-d',BUILD/'classes',*sources)
    run('java','-cp',TOOLS/'lib/d8.jar','com.android.tools.r8.D8','--lib',ANDROID,
        '--min-api','26','--output',BUILD/'dex',*sorted((BUILD/'classes').rglob('*.class')))
    unsigned = BUILD/'unsigned.apk'
    shutil.copyfile(BUILD/'resources.apk', unsigned)
    with zipfile.ZipFile(unsigned,'a',compression=zipfile.ZIP_DEFLATED) as z:
        for dex in (BUILD/'dex').glob('*.dex'): z.write(dex,dex.name)
    aligned = BUILD/'aligned.apk'
    run(TOOLS/'zipalign','-f','4',unsigned,aligned)
    signing_names = ['GTV_KEYSTORE_FILE','GTV_KEYSTORE_PASSWORD','GTV_KEY_ALIAS','GTV_KEY_PASSWORD']
    supplied = [bool(os.environ.get(name)) for name in signing_names]
    if any(supplied) and not all(supplied):
        raise SystemExit('Provide all four GTV signing variables, or none for a development build.')
    if all(supplied):
        signing = pathlib.Path(os.environ['GTV_KEYSTORE_FILE'])
        if not signing.is_absolute(): signing = ROOT/signing
        alias = os.environ['GTV_KEY_ALIAS']
        store_password, key_password = 'env:GTV_KEYSTORE_PASSWORD', 'env:GTV_KEY_PASSWORD'
        output = BUILD/'outputs'/'grab-these-vehicles.apk'
    else:
        signing = ROOT/'signing'/'debug.keystore'
        if not signing.exists():
            signing.parent.mkdir(exist_ok=True)
            run('keytool','-genkeypair','-keystore',signing,'-storepass','android',
                '-keypass','android','-alias','androiddebugkey','-keyalg','RSA','-keysize','2048',
                '-validity','10000','-dname','CN=Android Debug, O=Android, C=US')
        alias, store_password, key_password = 'androiddebugkey','pass:android','pass:android'
        output = BUILD/'outputs'/'grab-these-vehicles-debug.apk'
    run(TOOLS/'apksigner','sign','--ks',signing,'--ks-key-alias',alias,
        '--ks-pass',store_password,'--key-pass',key_password,'--out',output,aligned)
    run(TOOLS/'apksigner','verify','--verbose',output)
    run(TOOLS/'zipalign','-c','4',output)
    print(output)

if __name__ == '__main__': main()
