package cn.zheft.www.zheft.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.zheft.www.zheft.util.CameraUtils;


/**
 * 自定义相机
 * Created by Lin on 2017/4/20.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private  static final String TAG = "CameraPreview";

    private int viewWidth = 0;
    private int viewHeight = 0;

    /**
     * 监听接口
     */
    private OnCameraStatusListener listener;

    private SurfaceHolder holder;
    private Camera camera;
    private FocusView mFocusView;
    //创建一个PictureCallback对象，并实现其中的onPictureTaken方法
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        // 处理拍摄后的照片数据
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            //停止照片拍摄
            try {
                camera.stopPreview();
            } catch (Exception e){

            }
            if (null != listener){
                listener.onCameraStopped(bytes);
            }
        }
    };



    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setOnTouchListener(onTouchListener);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "```surfaceCreated");
        if (!CameraUtils.checkCameraHardware(getContext())) {
            Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取Camera对象
        camera = getCameraInstance();

        try {
            //设置用于显示拍照摄像的SurfaceHolder对象
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            //释放手机摄像头
            camera.release();
            camera = null;
        }

        updateCameraParameters();
        if (camera != null) {
            camera.startPreview();
        }
        setFocus();

    }

    private void updateCameraParameters() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            setParameters(parameters);

            try {
                camera.setParameters(parameters);
            } catch (Exception e) {
                Camera.Size previewSize = findBestPreviewSize(parameters);
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                parameters.setPictureSize(previewSize.width, previewSize.height);
                camera.setParameters(parameters);
            }
        }
    }

    /**
     * 找到最合适的显示分辨率 （防止预览图像变形）
     * @param parameters
     * @return
     */
    private Camera.Size findBestPreviewSize(Camera.Parameters parameters) {
        //系统支持的所有预览分辨率
        String previewSizeValueString = null;
        previewSizeValueString = parameters.get("preview-size-values");
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        if (previewSizeValueString == null) {
            // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
            return camera.new Size(CameraUtils.getScreenWH(getContext()).widthPixels,
                    CameraUtils.getScreenWH(getContext()).heightPixels);
        }

        float bestX = 0;
        float bestY = 0;

        float tmpRadio = 0;
        float viewRadio = 0;
        if (viewWidth != 0 && viewHeight != 0) {
            viewRadio = Math.min((float) viewWidth, (float) viewHeight)
                    / Math.max((float) viewWidth, (float) viewHeight);
        }
        String[] COMMA_PATTERN = previewSizeValueString.split(",");
        for (String previewsizeString:
             COMMA_PATTERN) {
            previewsizeString = previewsizeString.trim();
            int dimPosition = previewsizeString.indexOf('x');
            if (dimPosition == -1) {
                continue;
            }

            float newX = 0;
            float newY = 0;

            try {
                newX = Float.parseFloat(previewsizeString.substring(0, dimPosition));
                newY = Float.parseFloat(previewsizeString.substring(dimPosition + 1));
            } catch (NumberFormatException e) {
                continue;
            }

            float radio = Math.min(newX, newY) / Math.max(newX, newY);
            if (tmpRadio == 0) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            } else if (tmpRadio != 0 && (Math.abs(radio - viewRadio)) < (Math.abs(tmpRadio - viewRadio))) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            }

        }
        if (bestX > 0 && bestY > 0) {
            return camera.new Size((int) bestX, (int) bestY);
        }
        return null;
    }

    private void setParameters(Camera.Parameters parameters) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        long time = new Date().getTime();
        parameters.setGpsTimestamp(time);
        //设置照片格式
        parameters.setPictureFormat(PixelFormat.JPEG);
        Camera.Size previewSize = findPreviewSizeByScreen(parameters);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setPictureSize(previewSize.width, previewSize.height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        if (getContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(90);
            parameters.setRotation(90);
        }

    }

    /**
     * 将预览大小设置为屏幕大小
     * @param parameters
     * @return
     */
    private Camera.Size findPreviewSizeByScreen(Camera.Parameters parameters) {
        if (viewHeight != 0 && viewWidth != 0){
            return camera.new Size(Math.max(viewWidth, viewHeight),Math.min(viewWidth,viewHeight));
        } else {
            return camera.new Size(CameraUtils.getScreenWH(getContext()).heightPixels, CameraUtils.getScreenWH(getContext()).widthPixels);
        }

    }

    /**
     * 设置自动聚焦 并且聚焦的圈圈显示在屏幕中间位置
     */
    public void setFocus() {
        if (!mFocusView.isFocusing()) {
            try {
                camera.autoFocus(this);
                mFocusView.setX((CameraUtils.getWidthInPx(getContext()) - mFocusView.getWidth()) / 2);
                mFocusView.setY((CameraUtils.getHeightInPx(getContext()) - mFocusView.getHeight()) / 2);
                mFocusView.beginFocus();
            } catch (Exception ew){}
        }
    }

    /**
     * 获取摄像头实例
     * @return
     */
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int camIndex = 0; camIndex < cameraCount; camIndex++) {
                Camera.getCameraInfo(camIndex, cameraInfo);
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        //打开后置摄像头
                        c= Camera.open(camIndex);
                    } catch (RuntimeException e) {
                        Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if (c == null) {
                c= Camera.open(0);
            }
        } catch (Exception e){
            Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
        }

        return c;
    }

    /**
     * 在surface的大小发生改变时激发
     * @param holder
     * @param format
     * @param w
     * @param h
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            // stop preview before making changes
            camera.startPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        updateCameraParameters();
        // start preview with new settings
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
        setFocus();
    }

    // 在surface销毁时激发
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "==surfaceDestroyed==");
        //释放手机摄像头
        camera.release();
        camera = null;
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {

    }

    /**
     * 设置聚焦图片
     * @param focusView
     */
    public void setFocusView(FocusView focusView) {
        this.mFocusView = focusView;
    }

    //设置监听事件
    public void setOnCameraStatusListener(OnCameraStatusListener listener) {
        this.listener = listener;
    }

    /**
     * 相机拍照监听接口
     */
    public interface OnCameraStatusListener {
        //相机拍照结束事件
        void onCameraStopped(byte[] data);
    }

    /**
     * 点击显示焦点区域
     */
    OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                int width = mFocusView.getWidth();
                int height = mFocusView.getHeight();
                mFocusView.setX(motionEvent.getX() - width/2);
                mFocusView.setY(motionEvent.getY() - height/2);
                mFocusView.beginFocus();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                focusOnTouch(motionEvent);
            }
            return true;
        }
    };

    /**
     * 设置焦点和测光区域
     * @param event
     */
    private void focusOnTouch(MotionEvent event) {
        int[] location = new int[2];
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        relativeLayout.getLocationOnScreen(location);

        Rect focusRect = CameraUtils.calculateTapArea(mFocusView.getWidth(),
                mFocusView.getHeight(),1f,event.getRawX(),event.getRawY(),
                location[0], location[0]+relativeLayout.getWidth(),location[1],
                location[1]+relativeLayout.getHeight());
        Rect meteringRect = CameraUtils.calculateTapArea(mFocusView.getWidth(),
                mFocusView.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
                location[0], location[0] + relativeLayout.getWidth(), location[1],
                location[1] + relativeLayout.getHeight());

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.ANTIBANDING_AUTO);

        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 1000));
            parameters.setMeteringAreas(focusAreas);
        }
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));

            parameters.setMeteringAreas(meteringAreas);
        }

        try {
            camera.setParameters(parameters);
        } catch (Exception e){}

        camera.autoFocus(this);
    }

    // 进行拍照，并将拍摄的照片传入PictureCallback接口的onPictureTaken方法
    public void takePicture(){
        if (camera != null) {
            try {
                camera.takePicture(null, null, pictureCallback);
            } catch (Exception e){}
        }
    }

    public void start(){
        if (camera!= null){
            camera.startPreview();
        }
    }

    public void stop(){
        if (camera != null){
            camera.stopPreview();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        super.onMeasure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }
}
