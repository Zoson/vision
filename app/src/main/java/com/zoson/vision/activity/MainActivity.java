package com.zoson.vision.activity;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.zoson.vision.R;

public class MainActivity extends Activity implements Callback, PictureCallback {

	SurfaceView sView;
	SurfaceHolder surfaceHolder;
	RelativeLayout mButtonsLayout;
	RelativeLayout mMainLayout;
	Button mStartButton, mStopButton;
	ButtonsHandler mHandler;
	Camera camera;
	double mVisibityTime;
	boolean mIsVisibity;
	boolean mIsStartPre;
	final int MSG_CHECK_PROESS = 10001;// "msg_check_proess";
	final int MSG_CHECK_TOUCH = 10002;// "msg_check_touch";
	final int MSG_WRITE_YUVDATA = 10003;

	Spinner spinner;

	int width = 352;
	int height = 288;
	static {
		System.loadLibrary("avutil");
		System.loadLibrary("swresample");
		System.loadLibrary("avcodec");
		System.loadLibrary("avformat");
		System.loadLibrary("swscale");
		System.loadLibrary("postproc");
		System.loadLibrary("ffmpeg-jni");
		System.loadLibrary("ffmpeg-rtmp");
		System.loadLibrary("ffmpeg-live");
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.mainlayout);

		
		sView = (SurfaceView) this.findViewById(R.id.surfaceid);
		sView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_CHECK_TOUCH);
				AutoFocus();
				return false;
			}

		});
		spinner = (Spinner)findViewById(R.id.sp_reso);
		mButtonsLayout = (RelativeLayout) this.findViewById(R.id.buttonsid);
		mStartButton = (Button) this.findViewById(R.id.button1);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				String[] resolutions = getResources().getStringArray(R.array.resolution);
				String resolution = resolutions[i];
				width = Integer.parseInt(resolution.split("x")[0]);
				height = Integer.parseInt(resolution.split("x")[1]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		mStartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mIsStartPre == false) {
					Camera.Parameters p = camera.getParameters();

					p.setPreviewFrameRate(25);
					p.setPreviewSize(width, height);
					p.setPictureFormat(PixelFormat.JPEG); // Sets the image format for
					// picture �趨��Ƭ��ʽΪJPEG��Ĭ��ΪNV21
					p.setPreviewFormat(PixelFormat.YCbCr_420_SP); // Sets the image format
					// for preview
					// picture��Ĭ��ΪNV21
					// p.setRotation(90);
					camera.setPreviewCallback(new PreviewCallback() {

						@Override
						public void onPreviewFrame(byte[] arg0, Camera arg1) {
							// TODO Auto-generated method stub
							if (mIsStartPre == true) {
								Message msg = new Message();
								Bundle bl = new Bundle();
								bl.putByteArray("messageyuvdata", arg0);
								msg.setData(bl);
								msg.what = MSG_WRITE_YUVDATA;
								mHandler.sendMessage(msg);
							}
						}

					});
					camera.setParameters(p);
					try {
						camera.setPreviewDisplay(surfaceHolder);
					} catch (Exception E) {

					}
					camera.startPreview();
					mIsStartPre = true;
					AutoFocus();
					Calendar cc = Calendar.getInstance();
					cc.setTimeInMillis(System.currentTimeMillis());
					String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
							+ String.valueOf(cc.get(Calendar.YEAR))
							+ "-"
							+ String.valueOf(cc.get(Calendar.MONTH))
							+ "-"
							+ String.valueOf(cc.get(Calendar.DAY_OF_YEAR))
							+ "-"
							+ String.valueOf(cc.get(Calendar.HOUR_OF_DAY))
							+ "-"
							+ String.valueOf(cc.get(Calendar.MINUTE))
							+ "-"
							+ String.valueOf(cc.get(Calendar.SECOND))
							+ ".h264";
					System.out.println(filename);
					videoinit(filename.getBytes(),width,height);
				}

			}

		});
		mStopButton = (Button) this.findViewById(R.id.button2);
		mStopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mIsStartPre == true) {
					mIsStartPre = false;
//					camera.setPreviewCallback(null);
//					camera.stopPreview();
					System.out.println("toend+++++++++++++++=");
					System.out.println("close++java++"+videoclose());
					System.out.println("end+++");

				}

			}

		});
		mIsStartPre = false;
		surfaceHolder = sView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHandler = new ButtonsHandler();
		mHandler.sendEmptyMessage(MSG_CHECK_PROESS);
		mHandler.sendEmptyMessage(MSG_CHECK_TOUCH);

		Button startButton = (Button) this.findViewById(R.id.button_start);
		final EditText urlEdittext_input= (EditText) this.findViewById(R.id.input_url);
		final EditText urlEdittext_output= (EditText) this.findViewById(R.id.output_url);

		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0){
				new Thread(){
					@Override
					public void run() {
						String folderurl=Environment.getExternalStorageDirectory().getPath();

						String urltext_input=urlEdittext_input.getText().toString();
						String inputurl=folderurl+"/"+urltext_input;

						String outputurl=urlEdittext_output.getText().toString();

						Log.e("inputurl",inputurl);
						Log.e("outputurl",outputurl);
						String info="";

						stream("/storage/emulated/0/"+inputurl,outputurl);

						Log.e("Info",info);
					}
				}.start();

			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onPictureTaken(byte[] arg0, Camera arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stu
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	public void AutoFocus() {
		if (camera != null) {
			camera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean arg0, Camera arg1) {
					// TODO Auto-generated method stub
					//
				}

			});
		}
	}

	@SuppressLint("HandlerLeak")
	class ButtonsHandler extends Handler {

		public ButtonsHandler() {
			super();
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CHECK_PROESS:
				if (mIsVisibity
						&& (System.currentTimeMillis() - mVisibityTime > 7000)) {
					mButtonsLayout.setVisibility(View.INVISIBLE);
					// mLinerLaryout.setFocusable(false);
					mIsVisibity = false;
					mVisibityTime = 0;
				}
				sendEmptyMessageDelayed(MSG_CHECK_PROESS, 500);

				break;
			case MSG_CHECK_TOUCH:
				if (mButtonsLayout.getVisibility() != View.VISIBLE) {
					mButtonsLayout.setVisibility(View.VISIBLE);
					// mLinerLaryout.setFocusable(true);

				}
				mIsVisibity = true;
				mVisibityTime = System.currentTimeMillis();
				break;
			case MSG_WRITE_YUVDATA:
				byte[] bytedata = msg.getData().getByteArray("messageyuvdata");
				if (bytedata != null) {
					addVideoData(bytedata);
				}
				break;
			}
		};
	};

	public synchronized void addVideoData(byte[] data) {
		videostart(data);
	}

	public native int videoinit(byte[] filename,int w,int h);

	public native int videostart(byte[] yuvdata);

	public native int videoclose();

	public native int stream(String inputurl, String outputurl);

}
