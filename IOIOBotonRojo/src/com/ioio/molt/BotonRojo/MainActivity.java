/*
 * 
 * @autor: Juan Guillermo Molt 
 * @versión: 1.0
 * @fecha: 26/11/2013
 * 
*/

package com.ioio.molt.BotonRojo;

import java.io.IOException;

import com.ioio.molt.BotonRojo.*;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalInput.Spec.Mode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Esta es la actividad principal de IOIOBotonRojo.
 * 
 * La aplicacion envia un SMS de advertencia a otro equipo, cuando el boton es activado.
 * Ademas reproduce un sonido de alerta, mientras se envia el SMS, el sonido puede ser desactivado
 * manteniendo presionado el boton por un rato.
 * IOIOBotonRojo example.
 */
public class MainActivity extends IOIOActivity {
	private ToggleButton button_;

	//private SoundPool soundPool;
	//private int soundID;
	
	private MediaPlayer sound1;
	boolean loaded = false;
	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_ = (ToggleButton) findViewById(R.id.button);
	    View view = findViewById(R.id.textView1);
	    // view.setOnTouchListener(this);
	    // Set the hardware buttons to control the music
	    //this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	    // Load the sound
	    //soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
	    /*soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
	      @Override
	      public void onLoadComplete(SoundPool soundPool, int sampleId,
	          int status) {
	        loaded = true;
	      }
	    });
	    soundID = soundPool.load(this, R.raw.sound1, 1);*/
	    
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led;
		private DigitalInput pulsador;

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
			
			pulsador = ioio_.openDigitalInput(7, Mode.PULL_UP);
			led = ioio_.openDigitalOutput(46);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException 
		{

			boolean wasMovement = false;
			try {
				wasMovement = !pulsador.read();
				
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block 
				e1.printStackTrace();
			}
			
			
			if (wasMovement == true)
			{

				wasMovement = false;
				
				sound1 = MediaPlayer.create(MainActivity.this, R.raw.sound3);
				sound1.start();
				
				sendSMS();
				toast("Mensaje Enviado");
			
				// aca comienza el loop de encendido de la alarma
				//////////////////////////////////////////////////
				
				for (int i=0;i<40;i++)
				{
					
					led.write(true); 
					try {
						Thread.sleep(400);
					}
					
					catch (InterruptedException e) {	
					}
					
					if (wasMovement == true)
					{
						sound1.stop();	
					}
					
					try {
						wasMovement = !pulsador.read();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					led.write(false);
					try {
						Thread.sleep(400);
					} 
				
					catch (InterruptedException e) {
					}
					
					if (wasMovement == true)
					{
						sound1.stop();	
					}
				}
				// aca termina el loop de encendido de la alarma
				///////////////////////////////////////////////
				
			sound1.stop();	
			}
			
			if (wasMovement == false && button_.isChecked()) 
			{
				led.write(button_.isChecked());
				//sendSMS();
			}
			
			if (wasMovement == false && !button_.isChecked()) 
			{
				led.write(button_.isChecked());
			}
			
			try
			{
				Thread.sleep(90);
			} 
			
			catch (InterruptedException e) 
			{
				
			}
			
			
		}
	}

	private void sendSMS() 
	{
		String number = "72161142";
	    String message = "Sistema activado! avise si recibio este mensaje :P";
		Context context = getApplicationContext();
		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);                
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(number, null, message, pi, null);  
		//toast("SMS Sent");
	}
	
	/*private void play() 
	{
    // Getting the user sound settings
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    float actualVolume = (float) audioManager
        .getStreamVolume(AudioManager.STREAM_MUSIC);
    float maxVolume = (float) audioManager
        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    float volume = actualVolume / maxVolume;
    // Is the sound loaded already?
    if (loaded) {
      soundPool.play(soundID, volume, volume, 1, 0, 1f);
      //Log.e("Test", "Played sound");
      //toast("Played sound");
      }
    } */
    
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
}