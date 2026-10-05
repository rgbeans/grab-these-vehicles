import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET


def adb(*args):
    return subprocess.check_output(['adb', *args])


adb('install', '-r', 'downloads/grab-these-vehicles-v1.3.apk')
adb('shell', 'pm', 'grant', 'com.grabthesevehicles.app', 'android.permission.POST_NOTIFICATIONS')
adb('shell', 'am', 'start', '-n', 'com.grabthesevehicles.app/.MainActivity')
time.sleep(3)
for attempt in range(12):
    adb('shell', 'uiautomator', 'dump', '/sdcard/window.xml')
    root = ET.fromstring(adb('shell', 'cat', '/sdcard/window.xml'))
    target = next((n for n in root.iter('node') if n.get('text') == 'Send a test notification'), None)
    if target is not None:
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', target.get('bounds')))
        if y2 > y1:
            adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
            break
    adb('shell', 'input', 'swipe', '540', '1500', '540', '600', '500')
    time.sleep(1)
else:
    raise RuntimeError('Test notification button was not found')
time.sleep(2)
adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
adb('shell', 'cmd', 'statusbar', 'expand-notifications')
time.sleep(2)
adb('shell', 'uiautomator', 'dump', '/sdcard/window.xml')
xml = adb('shell', 'cat', '/sdcard/window.xml')
if b'Simeon' not in xml:
    raise RuntimeError('Simeon notification did not appear')
root = ET.fromstring(xml)
for n in root.iter('node'):
    if 'expand' in n.get('content-desc', '').lower():
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
        adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
        time.sleep(1)
        break
out = pathlib.Path('play-release/graphics/03-notification.png')
out.write_bytes(adb('exec-out', 'screencap', '-p'))
