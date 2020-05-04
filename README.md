This App allows user to choose Video Files on Android Board & selects best available decoder to render video onto Surface. Code uses Asynchrnous Mode & user shall get callback when Input & Output buffers are available . 
Few ideas (related to Surface APIs) taken from : https://github.com/cedricfung
CAUTION:
This is to test decoders without using MediaMuxer APIs & thus playback goes fast due to lack of presentation timestamps from Audio Track. Please refer to https://stackoverflow.com/questions/36114808/android-setting-presentation-time-of-mediacodec for MediaMuxer related advise. 
