
/*
 * 
 * @autor: Juan Guillermo Molt 
 * @versión: 1.0
 * @fecha: 23/11/2013
 * 
*/

package com.ioio.molt.presion;

import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

import com.ioio.molt.presion.R;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.exception.ConnectionLostException;
//import ioio.lib.util.AbstractIOIOActivity; //revisar
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * This is the main activity of TempSensor application.
 * 
 * Muestra los datos del sensor de presion de FlexiForce
 * es una modificacion de la aplicacion TempLogger del libro 
 * Making Android Accesories de Simon Monk. Pero se cambio todo el sistema
 * de lectura, ya que se usa para leer la entrada analoga en el pin 45.
 */
public class MainActivity extends IOIOActivity implements OnCheckedChangeListener {
	
	private final static long SAMPLE_PERIOD = 10000; // 10 seconds
	private static final int PLUS_PIN = 44;
	private static final int GND_PIN = 46;
	private static final int INPUT_PIN = 41;	
	
	private TextView temperature_;
	private RadioButton radioF_;
	private ToggleButton logButton_;
	private TextView lastLogLine_;

	private long lastSampleTime_;
	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        temperature_ = (TextView)findViewById(R.id.temp);
        //radioF_ = (RadioButton)findViewById(R.id.radio_f);
        logButton_ = (ToggleButton)findViewById(R.id.logButton);
        logButton_.setOnCheckedChangeListener(this);
        lastLogLine_ = (TextView)findViewById(R.id.lastLogLine);	    
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	// aca empieza el loop
	
	class Looper extends BaseIOIOLooper {
		private DigitalOutput plusPin_;
		private DigitalOutput gndPin_;
		private AnalogInput inputPin_;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			//gndPin_ = ioio_.openDigitalOutput(GND_PIN, Mode.NORMAL, false); // gnd supply to temp sensor
			//plusPin_ = ioio_.openDigitalOutput(PLUS_PIN, Mode.NORMAL, true); // positive supply to temp sensor
			inputPin_ = ioio_.openAnalogInput(INPUT_PIN);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException 
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			try {
				float v = inputPin_.getVoltage();
				String units = "Lbs";
				float temp = v; 
				//if (radioF_.isChecked()) {
				//	temp = temp * 9.0f / 5.0f + 32.0f; 
				//	units = "F";
				//}
				// round to 1 dp

				temp = temp * 100.00f;
				temp = Math.round(temp * 10) / 10.0f;
				temp = temp/3.3f;
					
				if (temp > 17)
				updateTempField(temp, units);
				
				else updateTempField(0, units);

				
				long now = System.currentTimeMillis();
				if (now > lastSampleTime_ + SAMPLE_PERIOD) {
					logTemp(temp, units);
					lastSampleTime_ = now;
				}
				
				Thread.sleep(100); //se agrego sleep thread
			
			} catch (Exception e) {
				toast(e.getMessage());
			}
		}

		private void logTemp(float temp, String units) {
			if (logButton_.isChecked()) {
				Date now = new Date();
				String filename = "temp_" + DateFormat.format("yyyy_MM_dd", now).toString() + ".csv";
				CharSequence time = DateFormat.format("hh:mm:ss", now);
				String line = "" + time + ", " + temp + ", " + units + "\n";
				updateLastLogLine(line);
				appendToFile(filename, line);
			}
		}

		private void appendToFile(String filename, String line) {
			File root = Environment.getExternalStorageDirectory();
            try {
				FileOutputStream f = new FileOutputStream(new File(root, filename), true);
				f.write(line.getBytes());
				f.close();
			} catch (Exception e) {
				toast(e.getMessage());
			}
		}
	}
	
	//aca termina el loop
	
	private void updateTempField(final float temp, final String units) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				temperature_.setText("" + temp  + " " + units);
			}
		});
	}
	
	private void updateLastLogLine(final String line) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lastLogLine_.setText(line);
			}
		});
	}
	
	private void toast(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, message, duration);
				toast.show();
			}
		});
	}
	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			toast("Logging Started");
		}
	}
}