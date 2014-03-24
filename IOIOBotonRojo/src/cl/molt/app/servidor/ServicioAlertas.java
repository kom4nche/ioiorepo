/*
 * 
 * @autor: Juan Guillermo Molt 
 * @versión: 1.0
 * @fecha: 26/11/2013
 * 
*/

package cl.molt.app.servidor;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.DigitalInput.Spec.Mode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import ioio.lib.util.android.IOIOService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;



public class ServicioAlertas extends IOIOService {
	
	public static final String TAG = "BotonRojo";
	static final String patente = "RA7097";
	private static String tipo = "A"; 

	private MediaPlayer sound1;
	boolean loaded = false;
	
	Intent intent;

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private DigitalOutput led;
			private DigitalInput pulsador;

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				pulsador = ioio_.openDigitalInput(7, Mode.PULL_UP);
				led = ioio_.openDigitalOutput(46);
			}

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
				
				boolean wasMovement = false;
				try {
					
					wasMovement = !pulsador.read();
					
				} catch (InterruptedException e1) {

					e1.printStackTrace();
				}
				
				
				if (wasMovement == true)
				{

					wasMovement = false;
					
					if (intent == null)
					{
						intent = new Intent(getApplicationContext(), ServicioUbicacion.class);
						startService(intent); 
					}

					tipo = "P";

		    		TareaAlerta tarea = new TareaAlerta();
		    		tarea.execute(patente, tipo
		    				               );
		    		
		    		//sendSMS(tipo);
				
					// aca comienza el loop de encendido de la alarma
					//////////////////////////////////////////////////
					
					for (int i=0;i<3;i++)
					{
						
						led.write(true); 
						try {
							Thread.sleep(400);
						}
						
						catch (InterruptedException e) {	
						}
						
						if (wasMovement == true)
						{
							//sound1.stop();	
						}
						
						try {
							wasMovement = !pulsador.read();

						} catch (InterruptedException e1) {
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
							//sound1.stop();	
						}
					}
					// aca termina el loop de encendido de la alarma
					///////////////////////////////////////////////
					
				//sound1.stop();	
				}
				
				try
				{
					Thread.sleep(90);
				} 
				
				catch (InterruptedException e) 
				{
					
				}
				
				
			}
			
			
		};
	}


		@Override
		public void onStart(Intent intent, int startId) {
			super.onStart(intent, startId);
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			if (intent != null && intent.getAction() != null
					&& intent.getAction().equals("stop")) {
				// User clicked the notification. Need to stop the service.
				nm.cancel(1);
				stopSelf();
			} else {
				// Service starting. Create a notification.
				Notification notification = new Notification(
						R.drawable.ic_launcher, "Servicio Alertas Encendido",
						System.currentTimeMillis());
				notification
						.setLatestEventInfo(this, "Servicio Alertas", "Click para apagar",
								PendingIntent.getService(this, 0, new Intent(
										"stop", null, this, this.getClass()), 0));
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				nm.notify(1, notification);
			}
		}
	
    private class TareaAlerta extends AsyncTask<String,Integer,String>
	{
		@Override
        protected String doInBackground(String... params) 
		{
            String msg = "";
            
            try 
            {
                registroAlerta(params[0], params[1]);
                
            } 
            catch (Exception ex) 
            {
            	Log.d(TAG, "Error registro en Servidor:" + ex.getMessage());
            }
            
            return msg;
        }

	}
	
	private boolean registroAlerta(String patente, String tipo)
	{
		boolean reg = false;
		
		final String NAMESPACE = "http://tesis.mobi/";
		final String URL= "http://tesis.mobi/LOADServer/server.php";
		final String METHOD_NAME = "RegistroAlerta";
		final String SOAP_ACTION = "http://tesis.mobi/RegistroAlerta";

		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		
		request.addProperty("patente", patente); 
		request.addProperty("tipo", tipo); 

		SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.dotNet = true; 

		envelope.setOutputSoapObject(request);

		HttpTransportSE transporte = new HttpTransportSE(URL);

		try 
		{
			transporte.call(SOAP_ACTION, envelope);

			//SoapPrimitive resultado_xml =(SoapPrimitive)envelope.getResponse();
			Object response = envelope.getResponse();
			String res = response.toString();
			
			if(res.equals("1"))
			{
				Log.d(TAG, " - Registro ubicacion OK.");
				reg = true;
			}
		} 
		catch (Exception e) 
		{
			Log.d(TAG, "Error registro en mi servidor: " + e.getCause() + " || " + e.getMessage());
		} 
		
		return reg;
	}
	

	private void sendSMS(String origen) 
	{
	    String mensaje = "INDETERMINADO";
		if (origen == "P") mensaje = "PUERTAS";
		if (origen == "V") mensaje = "VENTANAS";
		String number = "83472088";
	    String message = "Alarma activada! Origen activación: "+mensaje;
		Context context = getApplicationContext();
		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, ServicioAlertas.class), 0);                
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(number, null, message, pi, null);  
		//toast("SMS Sent");
		//Toast.makeText(this,"sms enviado",Toast.LENGTH_SHORT).show();
	}
    

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}