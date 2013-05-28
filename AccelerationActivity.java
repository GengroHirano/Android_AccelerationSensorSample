package com.example.accelerationsensorsample;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class AccelerationActivity extends Activity implements SensorEventListener{

	public final static String TAG = "SencerTest" ;
	private final static int MATRIX_SIZE = 16 ;
	protected final static double RAD2DEG = 180/Math.PI ;

	SensorManager sencManager ;

	//回転行列
	float[] rotationMatrix = new float[MATRIX_SIZE] ;
	float[] outRotation = new float[MATRIX_SIZE] ;
	float[] I = new float[MATRIX_SIZE] ;
	
	float[] gravity = new float[3] ;
	float[] geomagnetic = new float[3] ;
	float[] attitude = new float[3] ;

	TextView azimuthValue ;
	TextView pitchValue ;
	TextView rollValue ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_acceleration);

		azimuthValue = (TextView)findViewById(R.id.AzimuthValue) ;
		pitchValue = (TextView)findViewById(R.id.PitchValue) ;
		rollValue = (TextView)findViewById(R.id.RollValue) ;

		sencManager = (SensorManager)getSystemService(SENSOR_SERVICE) ;
	}

	@Override
	protected void onResume() {
		super.onResume();
		//センサーを受信するリスナーの登録
		//第一引数:受信するリスナーのインスタンス, 第二引数:センサーの種類, 第三引数:受信する頻度
		sencManager.registerListener(this, sencManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI) ;
		sencManager.registerListener(this, sencManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI) ;
	}

	@Override
	protected void onPause() {
		super.onPause();
		sencManager.unregisterListener(this) ;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//センサーの精度が変わったら呼び出されます
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//センサーの値が変わったら呼び出される
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			gravity = event.values.clone() ;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			geomagnetic = event.values.clone() ;
			break;
		default:
			break;
		}
		
		if( gravity != null && geomagnetic != null ){
			SensorManager.getRotationMatrix(rotationMatrix, I, gravity, geomagnetic) ;
			//なんか調整が必要っぽい
			SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, outRotation) ;
			SensorManager.getOrientation(outRotation, attitude) ;
			
			//式は(int)(attitude[0] * RAD2DEG)と一緒
			azimuthValue.setText(Integer.toString( (int)Math.toDegrees(attitude[0]) )) ;//方向
			pitchValue.setText(Integer.toString( (int)Math.toDegrees(attitude[1]) )) ; //飛行機で言う機首の上げ下げ
			rollValue.setText(Integer.toString( (int)Math.toDegrees(attitude[2]) )) ; //飛行機で言う主翼の先端
			//詳しく載ってるttp://shitappaprogramer.seesaa.net/article/229118272.html
		}
	}

}
