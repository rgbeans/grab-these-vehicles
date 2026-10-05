"""Capture the installed app and Android notification UI without repainting pixels."""
import pathlib, re, subprocess, time, xml.etree.ElementTree as ET
OUT = pathlib.Path('build/phone-previews')
OUT.mkdir(parents=True, exist_ok=True)
PACKAGE = 'com.grabthesevehicles.app'

def adb(*args):
    return subprocess.check_output(['adb', *map(str,args)])

def shell(*args):
    return adb('shell', *args)

def tree():
    shell('uiautomator', 'dump', '/sdcard/window.xml')
    raw = adb('exec-out', 'cat', '/sdcard/window.xml')
    return ET.fromstring(raw[raw.index(b'<?xml'):])

def find(label, partial=False, description=False):
    for node in tree().iter('node'):
        value=node.get('content-desc' if description else 'text','')
        if (label in value if partial else label==value):
            return node
    return None

def tap_node(node):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',node.get('bounds')))
    shell('input','tap',(x1+x2)//2,(y1+y2)//2)
    time.sleep(1)

def tap(label, partial=False, scroll=False):
    for _ in range(12 if scroll else 1):
        node=find(label,partial)
        if node is not None:
            tap_node(node)
            return
        shell('input','swipe',540,1850,540,650,500)
        time.sleep(.5)
    raise RuntimeError('Could not locate '+label)

def capture(name):
    time.sleep(3)
    (OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
    (OUT/(name+'.xml')).write_bytes(adb('exec-out','cat','/sdcard/window.xml'))
    print('Captured',name,flush=True)

def top():
    for _ in range(8):shell('input','swipe',540,600,540,2000,200)
    time.sleep(1)

shell('wm','size','1080x2400')
shell('wm','density','420')
shell('settings','put','system','screen_off_timeout','1800000')
shell('settings','put','global','window_animation_scale','0')
shell('settings','put','global','transition_animation_scale','0')
shell('settings','put','global','animator_duration_scale','0')
shell('settings','put','secure','ui_night_mode','2')
shell('cmd','uimode','night','yes')
shell('input','keyevent','82')
shell('pm','grant',PACKAGE,'android.permission.POST_NOTIFICATIONS')
shell('am','start','-W','-n',PACKAGE+'/.MainActivity')
time.sleep(3)
tree();capture('01-main')
tap('Contacts ·',partial=True)
tree();capture('02-contacts')
tap('Contacts ·',partial=True)
tap('Customize app icon',scroll=True)
tree();capture('04-icon-picker')
shell('input','swipe',540,1580,540,1000,450)
time.sleep(1);tree();capture('05-icon-picker-more')
shell('input','keyevent','4')
top()
tap('Send a test notification',scroll=True)
tap('Warstock Cache & Carry')
time.sleep(4)
shell('cmd','statusbar','expand-notifications')
time.sleep(3)
# Expand the real System UI notification, if initially collapsed.
node=find('Expand',partial=True,description=True)
if node is not None:tap_node(node)
else:shell('input','swipe',540,420,540,1050,500)
time.sleep(2)
assert find('Warstock Cache & Carry') is not None, 'Warstock notification is not visible'
tree();capture('06-notification')
shell('cmd','statusbar','collapse')
time.sleep(1)
top()
tap('Customize app icon',scroll=True)
shell('input','keyevent','4')
tree();capture('03-settings')
print('All screenshots captured from the running app and Android System UI.',flush=True)
