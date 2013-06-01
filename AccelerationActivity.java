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

	float[] currentAttitude = new float[3] ;

	TextView azimuthValue ;
	TextView pitchValue ;
	TextView rollValue ;
	TextView news ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_acceleration);

		azimuthValue = (TextView)findViewById(R.id.AzimuthValue) ;
		pitchValue = (TextView)findViewById(R.id.PitchValue) ;
		rollValue = (TextView)findViewById(R.id.RollValue) ;
		news = (TextView)findViewById(R.id.NEWS) ;

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
			//--------------------なんか調整が必要っぽい--------------------//
			//背面カメラを下に地面に水平に、ポートレイト状態で持ったとき
			SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, outRotation) ;
			SensorManager.getOrientation(outRotation, attitude) ;

			//式は(int)(attitude[0] * RAD2DEG)と一緒
			//azimuthValue.setText(Integer.toString( (int)Math.toDegrees(attitude[0]) )) ;//方向
			/**
			 * もし方位角にしたいなら + 360.0fを付けよう なぜならgetOrientationでかえってくるラジアン？だっけかな
			 * そいつが-π~πの範囲で入ってるからなんだとさ
			 **/
			double azimus = 0.0;
			if(attitude[0] < 0){
				azimus = Math.toDegrees(attitude[0]) + 360 ;
			}
			else{
				azimus = Math.toDegrees(attitude[0]) ;
			}
			azimuthValue.setText(Integer.toString( (int)azimus )) ;
			//ピッチとロールの表示にはローパスフィルタを噛ませる
			currentAttitude[1] = (float) (attitude[1] * 0.1 + currentAttitude[1] * 0.9) ;
			currentAttitude[2] = (float) (attitude[2] * 0.1 + currentAttitude[2] * 0.9) ;

			pitchValue.setText(Integer.toString( (int)Math.toDegrees(currentAttitude[1]) )) ; //飛行機で言う機首の上げ下げ -90~90度まで
			rollValue.setText(Integer.toString( (int)Math.toDegrees(currentAttitude[2]) )) ; //飛行機で言う主翼の先端 -180~180度まで
			//詳しく載ってるttp://shitappaprogramer.seesaa.net/article/229118272.html

//			Log.v("磁界", Float.toString(geomagnetic[0])) ;
//			Log.i("磁界", Float.toString(geomagnetic[1])) ;
//			Log.e("磁界", Float.toString(geomagnetic[2])) ;
			
			if( azimus >= 338 || azimus <= 22 ){
				news.setText("北") ;
			}
			else if( azimus >= 23 && azimus <= 67 ){
				news.setText("北東") ;
			}
			else if( azimus >= 68 && azimus <= 112 ){
				news.setText("東") ;
			}
			else if( azimus >= 113 && azimus <= 157 ){
				news.setText("南東") ;
			}
			else if( azimus >= 158 && azimus <= 202 ){
				news.setText("南") ;
			}
			else if( azimus >= 203 && azimus <= 247 ){
				news.setText("南西") ;
			}
			else if( azimus >= 248 && azimus <= 292 ){
				news.setText("西") ;
			}
			else if( azimus >= 293 && azimus <= 337 ){
				news.setText("北西") ;
			}
		}
	}

}
