package ru.abch.acceptgoods6.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import ru.abch.acceptgoods6.App;
import ru.abch.acceptgoods6.Database;
import ru.abch.acceptgoods6.MainActivity;
import ru.abch.acceptgoods6.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    final String TAG = this.getClass().getSimpleName();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Button btCancel, btOk;
    private TextureView mImageView = null;
    SurfaceHolder holder;
//    HolderCallback holderCallback;
    private final int CAMERA1   = 0;
    private final int CAMERA2   = 1;
//    String[] myCameras = null;
//    CameraService[] cameras = null;
    CameraService cService = null;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler = null;
    private CameraManager mCameraManager = null;
    public PhotoFragment() {
        // Required empty public constructor
    }
    static MainActivity activity;
    static int width, height;
    public static PhotoFragment newInstance() {
        PhotoFragment fragment = new PhotoFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        mImageView = view.findViewById(R.id.tv_photo);
        btCancel = view.findViewById(R.id.bt_cancel);
        btOk = view.findViewById(R.id.bt_ok);
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cService.isOpen()) cService.makePhoto();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.setCurrentBox(null);
                Database.clearGoods();
                Database.clearData();
                MainActivity.mViewModel.loadGoodsData();
                App.setPackMode(false);
                App.setCurrentPackId("");
                App.setCurrentPackNum("");
                ((MainActivity) requireActivity()).gotoMainFragment();
            }
        });
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCameraManager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        activity = (MainActivity) requireActivity();
    }
    @Override
    public void onPause() {
        if(cService != null) {
            cService.closeCamera();
        }
        stopBackgroundThread();
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        StreamConfigurationMap configurationMap;
        try{
            for (String cameraID : mCameraManager.getCameraIdList()) {
                Log.i(TAG, "cameraID: " + cameraID);
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraID);
                int faceing = cc.get(CameraCharacteristics.LENS_FACING);
                if (faceing ==  CameraCharacteristics.LENS_FACING_BACK) {
                    cService = new CameraService(mCameraManager,cameraID);
                    configurationMap = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] sizesJPEG = configurationMap.getOutputSizes(ImageFormat.JPEG);
                    if (sizesJPEG != null) {
                        for (Size item:sizesJPEG) {
                            width = item.getWidth();
                            height = item.getHeight();
                            if (width * height < 6000000) {
                                Log.i(TAG, "w:" + item.getWidth() + " h:" + item.getHeight());
                                break;
                            }
                        }
                    }  else {
                        Log.i(TAG, "camera don`t support JPEG");
                    }
                }
            }
        }
        catch(CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
        if(cService != null) {
            if (!cService.isOpen()) cService.openCamera();
            startBackgroundThread();
        }
    }
    public class CameraService {
        private final String mCameraID;
        private CameraDevice mCameraDevice = null;
        private CameraCaptureSession mCaptureSession;
        final String TAG = this.getClass().getSimpleName();
        private ImageReader mImageReader;
        private File mFile;// = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "test1.jpg");
        public CameraService(CameraManager cameraManager, String cameraID) {

            mCameraManager = cameraManager;
            mCameraID = cameraID;
        }
        public boolean isOpen() {
            return mCameraDevice != null;
        }

        public void openCamera() {
            try {
                if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mCameraManager.openCamera(mCameraID,mCameraCallback,null);
                }
            }
            catch (CameraAccessException e) {
                Log.i(TAG,e.getMessage());
            }
        }
        public void closeCamera() {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
        public String makePhoto (){
            String filename = "photo" + System.currentTimeMillis() + ".jpg";
            mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), filename);
            String ret = null;
            try {
                // This is the CaptureRequest.Builder that we use to take a picture.
                final CaptureRequest.Builder captureBuilder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(mImageReader.getSurface());
                CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                   @NonNull CaptureRequest request,
                                                   @NonNull TotalCaptureResult result) {
                    }
                };
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
                mCaptureSession.capture(captureBuilder.build(), CaptureCallback, mBackgroundHandler);
                ret = filename;
            }
            catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }
            return ret;
        }
        private final CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                Log.i(TAG, "Open camera  with id:" + mCameraDevice.getId());
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                mCameraDevice.close();
                Log.i(TAG, "disconnect camera  with id:" + mCameraDevice.getId());
                mCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                Log.i(TAG, "error! camera id:" + camera.getId() + " error:" + error);
            }
        };
        private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
                = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            }
        };

        private void createCameraPreviewSession() {
            mImageReader = ImageReader.newInstance(1920,2560, ImageFormat.JPEG,1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
            SurfaceTexture texture = mImageView.getSurfaceTexture();
            texture.setDefaultBufferSize(1920,2560);
            Surface surface = new Surface(texture);
            try {
                final CaptureRequest.Builder builder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(surface);
                mCameraDevice.createCaptureSession(Arrays.asList(surface,mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mCaptureSession = session;
                                try {
                                    mCaptureSession.setRepeatingRequest(builder.build(),null,mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) { }}, mBackgroundHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    private static class ImageSaver implements Runnable {
        final String TAG = this.getClass().getSimpleName();
        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            Bitmap loadedBitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
            Matrix matrix = new Matrix();

//            matrix.postRotate(90);
            Bitmap scaledBitmap;
            if (loadedBitmap.getWidth() >= loadedBitmap.getHeight()){
                matrix.setRectToRect(new RectF(0, 0, loadedBitmap.getWidth(), loadedBitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
                matrix.postRotate(90);
                scaledBitmap = Bitmap.createBitmap(loadedBitmap, 0, 0, loadedBitmap.getWidth(), loadedBitmap.getHeight(), matrix, true);
            } else{
                matrix.setRectToRect(new RectF(0, 0, loadedBitmap.getWidth(), loadedBitmap.getHeight()), new RectF(0, 0, height, width), Matrix.ScaleToFit.CENTER);
                matrix.postRotate(90);
                scaledBitmap = Bitmap.createBitmap(loadedBitmap, 0, 0, loadedBitmap.getWidth(), loadedBitmap.getHeight(), matrix, true);
            }
            try {
                output = new FileOutputStream(mFile);
//                output.write(bytes);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.gotoMainFragment();
                }
            });
        }
    }
}