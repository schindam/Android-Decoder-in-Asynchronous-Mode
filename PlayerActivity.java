package com.public.asyncdec;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.nio.ByteBuffer;
import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.BUFFER_FLAG_SYNC_FRAME;
import static android.media.MediaCodecList.ALL_CODECS;


public class PlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private MediaExtractor extractor = new MediaExtractor();
    private SurfaceView sv;
    private SurfaceHolder playerViewHolder;
    private Surface surf;
    private String[] foundFormats = null;
    //status flag for End of Stream
    private boolean isEOS = false;
    private static final String TAG = "codecexperi";
    private MediaCodec codec;
    private MediaFormat mOutputFormat;
    private String filepath;
    private MediaFormat mvidformat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_player);
        SurfaceView sv = new SurfaceView(this);
        sv.getHolder().addCallback(this);
        setContentView(sv);


        Intent receive = getIntent();
        filepath = receive.getStringExtra("FILEPATH");

       /* playerView = (SurfaceView) findViewById(R.id.playerView);
        playerViewHolder = playerView.getHolder().addCallback(getApplicationContext());
        //playerViewHolder.setFormat(PixelFormat.RGBA_8888);
        surf = playerViewHolder.getSurface();*/

        /*new Thread(new Runnable() {
            public void run() {
                extractMediaData(filepath);
            }
        }).start();*/

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"surfaceCreated method is called");
        surf = holder.getSurface();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG,"surfacechanged method is called with width : "+width+" & height : "+height);
        new Thread(new Runnable() {
            public void run() {
                extractMediaData(filepath);
            }
        }).start();

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }



    //this method selects available FreeScale Hardware Codec to play given mimetype
    private static String scanFsHWCodec(String mimeType) {
        int totalCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo;
        MediaCodecInfo.CodecCapabilities capabilities;


        for (int i = 0; i < totalCodecs; i++) {
            codecInfo = MediaCodecList.getCodecInfoAt(i);
            if(!codecInfo.isEncoder() && codecInfo.getName().contains("Freescale") && codecInfo.getName().contains("hw-based")) {
                //Log.d(TAG, "Detected : " + codecInfo.getName());
                String[] types = codecInfo.getSupportedTypes();
                for (int j = 0; j < types.length; j++) {
                    if(types[j].equalsIgnoreCase(mimeType)){
                        capabilities = codecInfo.getCapabilitiesForType(mimeType);
                        Log.d(TAG, "This decoder supports "+mimeType+" : "+codecInfo.getName());
                        if(checkColorFormats(capabilities) == 0){
                            Log.e(TAG,"This codec got no recognized color formats");
                        }
                        return codecInfo.getName();
                    }
                    //Log.d(TAG, "with Media type supported : " + codecInfo);
                }
            }
        }
        return null;
    }

    private static String scanallCodec(MediaFormat format) {

        Log.d(TAG,"******************* Running ALL QUERY USAGE ROUTINE *********************");

        String MIME_TYPE = format.getString(MediaFormat.KEY_MIME);
        Log.d(TAG,"observed MIME_TYPE is "+MIME_TYPE);

        MediaCodecInfo.CodecCapabilities allcapabilities = null;
        MediaCodecInfo.VideoCapabilities videocapabilities=null;
        String[] supportTypes = null;

        MediaCodecList allCodeclist = new MediaCodecList(ALL_CODECS);
        //Returns the list of MediaCodecInfo objects for the list of media-codecs
        MediaCodecInfo[] allCollection=allCodeclist.getCodecInfos ();

        Log.d(TAG,"find decoder for format : "+allCodeclist.findDecoderForFormat(format));
        Log.d(TAG,"find encoder for format : "+allCodeclist.findEncoderForFormat(format));
        Log.d(TAG,"****************************************************************");


        for(int i = 0;i<allCollection.length;++i){
            Log.d(TAG,"Name of allCollection codec "+" "+i+" "+allCollection[i].getName());
            Log.d(TAG, "is Encoder ?? "+allCollection[i].isEncoder());
            supportTypes = allCollection[i].getSupportedTypes();
            try {
                allcapabilities = allCollection[i].getCapabilitiesForType(MIME_TYPE);
                Log.d(TAG,"This codec supports default format :"+allcapabilities.getDefaultFormat().getString(MediaFormat.KEY_MIME));

                if(allcapabilities.isFormatSupported(format)){
                    Log.d(TAG, "This codec supports given file format");

                    //check colorformats supported by this codec
                    //refer link for colorformat codes
                    // https://developer.android.com/reference/android/media/MediaCodecInfo.CodecCapabilities#COLOR_FormatYUV420Flexible
                    checkColorFormats(allcapabilities);

                    videocapabilities = allcapabilities.getVideoCapabilities();
                    //Returns the alignment requirement for video width (in pixels). This is a power-of-2 value that video width must be a multiple of
                    Log.d(TAG,"Has : "+videocapabilities.getWidthAlignment()+" pixels width alignment");
                    //Returns the alignment requirement for video height (in pixels). This is a power-of-2 value that video height must be a multiple of
                    Log.d(TAG,"Has : "+videocapabilities.getHeightAlignment()+" pixels height alignment");
                    //get the range of supported bitrates in bits/second
                    Log.d(TAG,"Supports bitrate range : "+videocapabilities.getBitrateRange().toString());
                    //get the range of supported frame rates
                    Log.d(TAG,"Supports framerate range : "+videocapabilities.getSupportedFrameRates().toString());
                    //get the range of supported video heights
                    Log.d(TAG,"Supports video height range : "+videocapabilities.getSupportedHeights().toString());
                    //get the range of supported video widths
                    Log.d(TAG,"Supports video weight range : "+videocapabilities.getSupportedWidths().toString());

                }

            } catch (IllegalArgumentException e){
                    Log.d(TAG,"Illegal Argument");
            }
            if(supportTypes.length>0) {
                Log.d(TAG, "Following is list of all supported types by this codec");
                for (int j = 0; i < supportTypes.length; ++i) {
                    Log.d(TAG, "Supported Types : " + supportTypes[j]);
                }
            }
            Log.d(TAG,"****************************************************");

        }

        return null;
    }


    private static int checkColorFormats(MediaCodecInfo.CodecCapabilities capabilities){
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            Log.d(TAG,"Found Color Format supported by this decoder : "+colorFormat);
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        return 0;
    }

    /**
     * Returns true if this is a color format that this test code understands (i.e. we know how
     * to read and generate frames in this format).
     */
    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    private void extractMediaData(String path){

        String MIME_TYPE = null;
        MediaFormat format;
        int trackCount=0;
        String selectedDecoder = null;
        try {
            extractor.setDataSource(path);
            trackCount = extractor.getTrackCount();
            foundFormats = new String[trackCount];
            Log.d(TAG,"Found trackcount from given file : "+trackCount);

            for (int i = 0; i < trackCount; i++) {
                format = extractor.getTrackFormat(i);
                MIME_TYPE = format.getString(MediaFormat.KEY_MIME);
                foundFormats[i] = MIME_TYPE;




                if (MIME_TYPE.startsWith("video/")) {

                    Log.d(TAG, "Track Format in MIME type : " +MIME_TYPE+" for track Index "+extractor.getSampleTrackIndex());
                    extractor.selectTrack(i);

                    //selectedDecoder = scanallCodec(format);
                    selectedDecoder = scanFsHWCodec(MIME_TYPE);

                    if( selectedDecoder == null){
                        Log.e(TAG,"Couldn't find required hardware codec to play your file");
                        return;
                    }
                    //note that below "MANDATORY" keys are already taken care by MediaExtractor.
                    Log.d(TAG,"Format's width : "+format.getInteger(MediaFormat.KEY_WIDTH));
                    Log.d(TAG,"Format's height : "+format.getInteger(MediaFormat.KEY_HEIGHT));
                    Log.d(TAG,"Format's duration: "+format.getLong(MediaFormat.KEY_DURATION));
                    mvidformat = format;
                    break;
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        startGame(selectedDecoder);
    }




    private void startGame(String name){
        try {
            codec = MediaCodec.createByCodecName(name);
        }catch (IOException e){
            Log.e(TAG,"Cannot create codec");
        }catch (IllegalArgumentException e){
            Log.e(TAG,"codec name not valid");
        }catch (NullPointerException e){
            Log.e(TAG,"codec name is null");
        }

        codec.setCallback( new MediaCodec.Callback () {


            public void onInputBufferAvailable(MediaCodec mc, int inputBufferId) {

                Log.d(TAG,"Input Buffer Callback from "+mc.getName());

                ByteBuffer inputBuffer = mc.getInputBuffer(inputBufferId);
                // fill inputBuffer with valid data

                MediaFormat bufferFormat = mc.getInputFormat();

                int sampleSize = extractor.readSampleData(inputBuffer,0);

                Log.d(TAG,"Current sample flag is :"+extractor.getSampleFlags());
                Log.d(TAG,"Current DRM info is "+extractor.getDrmInitData());
                Log.d(TAG,"Current sample time is "+extractor.getSampleTime());
                Log.d(TAG, "Current sample size : " +sampleSize+" for track Index "+extractor.getSampleTrackIndex());


                try {
                    if( sampleSize != -1 || !isEOS ){

                        if(( extractor.getSampleFlags() & MediaCodec.BUFFER_FLAG_END_OF_STREAM ) == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                            Log.wtf(TAG,"Found EOS Marker while you still got samples left");
                        }

                        // queue input buffer with sample metadata
                        mc.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.getSampleTime(), extractor.getSampleFlags());

                        //advance to next sample. This is a BLOCKING OPERATION & must be executed OUTSIDE main thread
                        if(!extractor.advance()){
                            isEOS = true;
                            Log.d(TAG,"No next sample to advance to");
                        }
                    } else {

                        isEOS = true;
                        Log.d(TAG,"All samples are consumed");

                        //submitting empty i/p buffer with EOS marker to let know decoder that we don't need i/p buffers further
                        mc.queueInputBuffer(0, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                } catch (IllegalStateException e){
                    Log.e(TAG,"IllegalStateException is observed"+e.toString()+e.getCause());

                    e.printStackTrace();
                }
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec mc, int outputBufferId, MediaCodec.BufferInfo bufferInfo) {

                Log.d(TAG,"onOutputBufferAvailable() called");

                if(( bufferInfo.flags == BUFFER_FLAG_END_OF_STREAM ) || ( bufferInfo.size <= 0 && isEOS )){
                    if(bufferInfo.size <=0){
                        Log.d(TAG,"we got buffer size zero");
                    }
                    if(bufferInfo.flags == BUFFER_FLAG_END_OF_STREAM){
                        Log.d(TAG,"we got EOS marker in o/p buffer");
                    }
                    codec.stop();
                    codec.release();
                    extractor.release();
                    return;
                }
                ByteBuffer outputBuffer = mc.getOutputBuffer(outputBufferId);
                MediaFormat bufferFormat = mc.getOutputFormat(outputBufferId);
                //render buffer with default timestamp
                mc.releaseOutputBuffer(outputBufferId, true);
            }

            @Override
            public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {
                Log.d(TAG, "onOutputFormatChanged called " + mc.getOutputFormat());
            }

            @Override
            public void onError(MediaCodec mc, MediaCodec.CodecException e) {
                Log.d(TAG,"onError() called");
                e.printStackTrace();
                codec.reset();
            }
        });

        codec.configure(mvidformat,surf,null,0);
        mOutputFormat = codec.getOutputFormat();
        //Log.d(TAG,"Format supported by "+name+" is "+mOutputFormat.getString(MediaFormat.KEY_MIME));
        codec.start();
    }

}
